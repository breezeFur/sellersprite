# 01 基础图：节点、边与共享状态

## 学习目标

学完本章后，你应该能够：

- 区分 `StateGraph`、`CompiledGraph` 和一次实际运行。
- 从零定义状态策略、节点和普通边。
- 解释节点为什么返回状态增量，而不是完整状态。
- 说明 `ReplaceStrategy` 与 `AppendStrategy` 的合并结果。
- 沿着 Alibaba Graph 源码说出一个节点从执行到输出的关键步骤。

## 先建立心智模型

Graph 可以先理解为“带共享状态的流程图”：

```text
输入 Map
   |
   v
START -> input -> transform -> output -> END
              共享 OverAllState
```

图中有三类东西：

- 节点：做工作，读取整体状态并返回局部更新。
- 边：规定一个节点完成后去哪里。
- 状态策略：规定局部更新怎样与旧状态合并。

`StateGraph` 是还在装配的图定义；调用 `compile()` 后得到 `CompiledGraph`；调用 `invoke()` 或订阅 `stream()` 才会发生一次运行。这三者不要混为一谈。

## 示例要完成什么

`BasicGraphLessonTest` 构建下面的线性图：

```text
START -> input -> transform -> output -> END
```

输入：

```text
input = " hello "
```

最终状态：

```text
processed = "HELLO"
result    = "HELLO!"
trace     = ["input", "transform", "output"]
```

## 代码拆解

### 第一步：声明状态键的合并规则

```java
StateGraph graph = GraphBuilderFactory.newGraph("basic-lesson", () -> Map.of(
        GraphStateKeys.INPUT, new ReplaceStrategy(),
        GraphStateKeys.PROCESSED, new ReplaceStrategy(),
        GraphStateKeys.RESULT, new ReplaceStrategy(),
        GraphStateKeys.TRACE, new AppendStrategy()));
```

策略可以理解为每个键自己的 reducer：

```text
mergedValue = strategy(oldValue, newValue)
```

`ReplaceStrategy` 永远采用新值，适合“当前处理结果”；`AppendStrategy` 把新元素追加到列表，适合轨迹、消息或证据集合。

如果没有为某个键注册策略，当前版本运行时默认使用 `REPLACE`。显式声明仍然更好，因为它把状态契约写在图定义处。

### 第二步：加入只返回增量的节点

`input` 节点只追加轨迹：

```java
graph.addNode("input", node_async(state -> Map.of(
        GraphStateKeys.TRACE, List.of("input"))));
```

它没有把原来的 `input`、其他键或完整 Map 再返回一遍。运行时会保留旧状态，只合并这一小份 delta。

`transform` 节点读取输入并产生新键：

```java
graph.addNode("transform", node_async(state -> Map.of(
        GraphStateKeys.PROCESSED,
        state.value(GraphStateKeys.INPUT, String.class)
                .orElseThrow()
                .trim()
                .toUpperCase(),
        GraphStateKeys.TRACE, List.of("transform"))));
```

`state.value(key, type)` 返回 `Optional<T>`。类型不匹配时会得到空值，所以必需状态应明确抛错，不能静默给一个看似成功的默认结果。

`output` 节点再读取 `processed`：

```java
graph.addNode("output", node_async(state -> Map.of(
        GraphStateKeys.RESULT,
        state.value(GraphStateKeys.PROCESSED, String.class).orElseThrow() + "!",
        GraphStateKeys.TRACE, List.of("output"))));
```

节点只与状态契约耦合，不需要直接调用上一个节点。

### 第三步：用边描述顺序

```java
graph.addEdge(StateGraph.START, "input");
graph.addEdge("input", "transform");
graph.addEdge("transform", "output");
graph.addEdge("output", StateGraph.END);
```

`START` 和 `END` 是框架的虚拟节点。入口边不可缺失；最后一个业务节点也必须明确通向 `END`。

注意：对同一来源多次调用 `addEdge(source, differentTarget)`，当前版本会累积多个目标并形成并行分支，不是“后一次覆盖前一次”。普通线性图每个来源只应有一个普通后继。

### 第四步：编译并执行

```java
CompiledGraph compiled = graph.compile();
OverAllState state = compiled
        .invoke(Map.of(GraphStateKeys.INPUT, " hello "))
        .orElseThrow();
```

编译先检查入口和边引用，再生成执行计划。它不会证明所有节点一定可达，也不会替你证明所有可能路径都能到 `END`；这些仍需要测试。

## 状态逐步变化

| 完成位置 | 本次节点返回 | 合并后的关键状态 |
| --- | --- | --- |
| 初始输入 | — | `input=" hello "` |
| `input` | `trace=[input]` | `input` 保留，`trace=[input]` |
| `transform` | `processed=HELLO`、`trace=[transform]` | `processed=HELLO`、`trace=[input, transform]` |
| `output` | `result=HELLO!`、`trace=[output]` | `result=HELLO!`、`trace=[input, transform, output]` |

这张表是理解 Graph 的关键：节点返回值和节点完成后的整体状态不是同一个东西。

## 框架源码下钻

以下结论来自 `spring-ai-alibaba-graph-core 2.0.0-M1.1` 的源码。

### `node_async` 并不会自动换线程

`AsyncNodeAction.node_async` 把同步 `NodeAction` 的结果放进 `CompletableFuture`：

```java
result.complete(syncAction.apply(state));
```

因此名字里的 async 表示统一的异步接口，不代表这个同步 Lambda 自动在线程池执行。真正并行要到第 07 课配置并行节点和 Executor。

### `compile()` 做了什么

`StateGraph.compile(config)` 的核心只有两步：

```java
validateGraph();
return new CompiledGraph(this, config);
```

`StateGraph` 是定义器，`CompiledGraph` 是验证并转换后的执行计划。

### 节点增量在哪里合并

运行链为：

```text
GraphRunner
  -> MainGraphExecutor
  -> NodeExecutor 调 action.apply(...)
  -> OverAllState 按 KeyStrategy 合并 delta
  -> 计算下一节点
  -> 保存 Checkpoint 并产生 NodeOutput
```

`OverAllState.updateState` 会逐键查找策略；没找到时使用 `KeyStrategy.REPLACE`。`ReplaceStrategy.apply(old,new)` 直接返回 `newValue`；`AppendStrategy` 则构建或扩展列表。

源码还说明 `OverAllState.data()` 只是不可修改的 Map 视图，内部嵌套 List/Map 不一定深不可变。节点应遵守“只读 state，返回 delta”，不要直接修改嵌套对象或主动调用 `updateState()` 绕过执行器。

### `invoke()` 的本质

`CompiledGraph.invoke(inputs, config)` 实际上是：

```java
stream(inputs, config).last().map(NodeOutput::state).block()
```

也就是说框架只有一套流式执行主链；`invoke()` 只是替你订阅流、等待结束并取最后状态。

## 常见误解

- “节点返回新状态”：不准确，节点返回的是状态增量。
- “`node_async` 会并行”：不会，它只是适配成 Future 接口。
- “`compile()` 后图已经跑过”：没有，编译只生成并验证执行计划。
- “未注册状态键不能写”：可以写，默认覆盖，但语义不够显式。
- “`NodeOutput.state()` 是本节点返回的 Map”：不是，它是合并后的整体状态视图。

## 自检问题

1. `trace` 如果误用 `ReplaceStrategy`，最终会是什么？
2. 为什么节点不应该复制并返回整个旧状态？
3. `StateGraph` 和 `CompiledGraph` 的生命周期有什么区别？
4. 为什么一个键没注册策略仍能运行？这种做法有什么维护成本？
5. 从 `invoke()` 开始，口头说出节点 delta 被合并的调用链。

## 课后 Test 练习

创建：

```text
src/test/java/cyou/yuanbaomao/graphlearning/lesson/basic/BasicGraphPracticeTest.java
```

只在测试中构建：

```text
START -> trim -> normalize -> publish -> END
```

题目要求：

- 输入 `input = "  sku-42  "`。
- `trim` 去除两侧空格并写回 `processed`。
- `normalize` 读取 `processed`，转成大写后再次写回同一个键。
- `publish` 写入 `result = "ITEM:SKU-42"`。
- 三个节点都向 `trace` 追加自己的节点名。
- `processed` 使用 `ReplaceStrategy`，`trace` 使用 `AppendStrategy`。
- 断言最终 `processed`、`result` 和严格轨迹顺序。
- 额外用 `stream()` 断言节点序列包含 `START`、三个业务节点和 `END`。

限制：不得修改 `src/main`，不得复制 `BasicGraphLessonTest` 后只改字符串。

运行：

```powershell
mvn -pl sellersprite-graph-learning -Dtest=BasicGraphPracticeTest test
```

完成后告诉 Codex：“我完成了第 01 课练习，请检查 `BasicGraphPracticeTest`。”检查时你需要解释两次写入 `processed` 为什么没有变成列表。

下一课：[条件路由与有界循环](02-routing-loop.md)。

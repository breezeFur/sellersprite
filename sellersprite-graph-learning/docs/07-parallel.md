# 07 并行分支：扇出、状态隔离与汇聚

## 学习目标

学完本章后，你应该能够：

- 使用多目标边表达并行扇出，并让分支汇聚到同一节点。
- 配置并行 Executor 和 `ALL_OF/ANY_OF` 聚合策略。
- 解释并行分支拿到的状态快照及其浅拷贝边界。
- 为多个分支写入同一状态键选择安全的 KeyStrategy。
- 编写不依赖分支完成顺序的稳定测试。

## 先建立心智模型

并行图包含两个结构动作：

```text
扇出 fan-out                     汇聚 fan-in
                 -> taskA -
START -> prepare -> taskB --> aggregate -> END
                 -> taskC -
```

`prepare` 完成后三个分支可同时执行；`aggregate` 何时开始由聚合策略决定。

并行不是“普通边画三次就完事”。必须同时考虑：线程池、分支失败、共享状态合并、顺序不确定和外部副作用。

## 示例代码拆解

### 第一步：定义并行安全的状态策略

```java
StateGraph graph = GraphBuilderFactory.newGraph("parallel-workflow", () -> Map.of(
        GraphStateKeys.RESULT, new ReplaceStrategy(),
        GraphStateKeys.TRACE, new AppendStrategy()));
```

三个任务都写 `trace`，所以需要追加策略。如果它们都用 Replace 写同一个键，最终值会依赖合并顺序，通常不是想要的语义。

### 第二步：创建扇出边

```java
graph.addEdge(PREPARE, List.of(TASK_A, TASK_B, TASK_C));
```

同一来源拥有多个普通目标时，当前版本编译器识别为并行结构。

### 第三步：声明共同汇聚点

```java
graph.addEdge(List.of(TASK_A, TASK_B, TASK_C), AGGREGATE);
```

每个分支必须能汇聚到同一个目标。否则框架无法可靠知道哪一处代表本轮并行结束。

### 第四步：配置执行器与聚合策略

```java
RunnableConfig.builder()
        .threadId(threadId)
        .defaultParallelExecutor(executor)
        .addParallelNodeAggregationStrategy(
                AGGREGATE,
                NodeAggregationStrategy.ALL_OF)
        .build();
```

注意聚合策略配置使用的是汇聚节点 ID `aggregate`，不是扇出来源 `prepare`。

测试自己创建 Executor，就必须在 `finally` 中关闭；图不拥有调用方线程池的生命周期。

## 框架源码下钻

以下结论来自 `spring-ai-alibaba-graph-core 2.0.0-M1.1`。

### 编译器会改写并行图

简单并行结构会被转换为近似：

```text
prepare
  -> __PARALLEL__(prepare)
       内部同时执行 taskA/taskB/taskC Action
  -> aggregate
```

主执行器不是依次访问三个任务；编译器创建内部 `ParallelNode`，把分支 Action 放进一个并行执行单元。

内部节点属于实现细节，业务图仍只使用 `addEdge` 公共 API。

### 每个分支拿到什么状态

提交分支前框架调用 `state.snapShot()`，让各分支获得顶层 Map 的副本。这可以避免直接覆盖顶层状态，但它是浅拷贝：嵌套 List、Map 或业务对象仍可能被共享。

因此分支必须把输入状态当只读数据，只通过返回 delta 表达结果，不能直接改嵌套集合。

### Executor 的选择顺序

```text
当前并行节点专用 Executor
  -> defaultParallelExecutor
  -> 框架静态默认线程池
```

生产中优先显式提供受监控线程池，而不是无限依赖框架默认池。

### ALL_OF 的准确语义

默认 `ALL_OF` 使用 `CompletableFuture.allOf` 等待全部分支成功，再按分支定义顺序 join 和合并结果，而不是按实际完成先后顺序合并。

这意味着：

- 任一分支异常，整个并行节点失败。
- Append 结果按分支定义顺序累计，测试仍不应依赖真实完成时序。
- Replace 冲突时后合并分支覆盖前分支，最好避免这种含糊设计。

### ANY_OF 是“第一个成功”

`ANY_OF` 会忽略先失败的分支，等待第一个成功结果；只有全部失败才整体失败。首个成功后框架尝试 `cancel(true)` 其他 Future。

取消不保证已经运行的外部调用真正停止，因此落败分支仍可能产生副作用。ANY_OF 分支应无副作用、可取消或具备幂等保护。

当前条件并行的内部实现与普通 ParallelNode 不完全相同，不应默认它也支持所有 `ANY_OF` 和并发限制配置。

## 示例结果怎样断言

最终 trace 必须包含：

```text
prepare, taskA, taskB, taskC, aggregate
```

稳定断言方式：

- `prepare` 是第一个业务轨迹。
- `aggregate` 是最后一个业务轨迹。
- 中间三个任务各出现一次，不限定完成次序。
- 最终 result 为 aggregated。

即使当前 ALL_OF 合并常按定义顺序呈现任务轨迹，也不应把测试写成对线程调度时序的承诺。

## 常见误解

- “多条边会按添加顺序串行执行”：同一来源多目标会形成并行。
- “ALL_OF 的合并顺序就是完成顺序”：源码按分支定义顺序收集结果。
- “状态快照是深复制”：当前只是顶层复制。
- “ANY_OF 会强制停止所有其他任务”：Future 取消不保证外部副作用停止。
- “多个分支可以随意覆盖同一键”：必须先定义确定的合并语义。

## 自检问题

1. 为什么需要同时声明扇出和共同汇聚点？
2. 聚合策略用来源节点还是汇聚节点配置？
3. 三个分支写同一个 Replace 键可能产生什么语义问题？
4. ALL_OF 和 ANY_OF 遇到首个失败时分别怎样处理？
5. 浅状态快照对嵌套可变集合意味着什么？

## 课后 Test 练习

创建：

```text
src/test/java/cyou/yuanbaomao/graphlearning/lesson/parallel/ParallelPracticeTest.java
```

使用 `ParallelWorkflow`，要求：

- 创建三线程 Executor，并保证在 `finally` 关闭。
- 使用唯一 threadId 和 `ALL_OF` 配置执行图。
- 断言最终 result 为 `aggregated`。
- 断言业务轨迹第一个是 `PREPARE`，最后一个是 `AGGREGATE`。
- 断言 `TASK_A/TASK_B/TASK_C` 各出现且只出现一次。
- 不对三个任务之间的完成次序写固定断言。
- 额外断言 aggregate 只出现一次，证明汇聚节点没有被每个分支各触发一遍。

加分题：在测试中自建两分支图，让两条分支分别写相同的 Append 键，验证 `ALL_OF` 合并结果；然后说明如果换成 Replace，哪个结果会保留以及为什么这种设计不够稳健。

运行：

```powershell
mvn -pl sellersprite-graph-learning -Dtest=ParallelPracticeTest test
```

完成后找 Codex 检查，并解释你的断言为什么不依赖线程调度时序。

下一课：[子图组合](08-subgraph.md)。

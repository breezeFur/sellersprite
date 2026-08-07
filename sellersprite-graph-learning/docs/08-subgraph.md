# 08 子图：把一段流程当成一个节点

## 学习目标

学完本章后，你应该能够：

- 解释为什么子图是“流程边界”，而不只是代码抽取。
- 把一个 `CompiledGraph` 注册为父图节点。
- 画出父图视角和子图视角的两层执行路径。
- 说明父子图状态键、合并策略和 Checkpoint 为什么必须兼容。
- 从 Alibaba Graph 源码中找到子图节点如何转换为可执行 Action。

## 先建立心智模型

普通节点把一个 Java 函数包装进图；子图节点把另一张已经可执行的图包装进图。因此父图看到一个节点，子图内部仍然拥有自己的 `START`、业务节点和 `END`。

```text
父图视角：START -> validation -> task -> finish -> END
                         |
                         v
子图内部：        START -> validateInput -> normalize -> END
```

子图适合表达有独立含义的阶段，例如“输入校验”“证据采集”“人工审核”。如果只是为了少写两个私有方法，不值得引入父子图的状态与恢复复杂度。

## 示例代码拆解

源码：`SubgraphWorkflow`。

### 第一步：先构建子图

```java
StateGraph validation = GraphBuilderFactory.newGraph(
        "validation-subgraph",
        () -> Map.of(GraphStateKeys.TRACE, new AppendStrategy()));
```

子图只声明自己会写入的 `trace`。随后加入两个节点：

```java
validation.addNode("validateInput", node_async(state -> Map.of(
        GraphStateKeys.TRACE, List.of("validateInput"))));
validation.addNode("normalize", node_async(state -> Map.of(
        GraphStateKeys.TRACE, List.of("normalize"))));
```

每个节点返回的仍是“状态增量”。子图没有获得特殊的状态模型，它继续使用前七章的 `OverAllState + KeyStrategy`。

### 第二步：把子图连完整并编译

```java
validation.addEdge(StateGraph.START, "validateInput");
validation.addEdge("validateInput", "normalize");
validation.addEdge("normalize", StateGraph.END);
return validation.compile();
```

必须先把子图编译成 `CompiledGraph`。编译会验证入口、目标节点和边结构；父图不接收一张还没验证的半成品图。

### 第三步：在父图中注册子图节点

```java
CompiledGraph validationSubgraph = buildValidationSubgraph();
parent.addNode(VALIDATION, validationSubgraph);
```

这行和注册普通 `node_async(...)` 的写法不同，但在父图连边时没有区别：

```java
parent.addEdge(StateGraph.START, VALIDATION);
parent.addEdge(VALIDATION, TASK);
```

父图只知道 `validation` 完成后去 `task`。`validateInput -> normalize` 的内部路径由子图自己管理。

## 状态怎样穿过父子图

本例最终轨迹是：

```text
[validateInput, normalize, task, finish]
```

这说明子图执行产生的状态会回到父图，并继续按父图的策略参与合并。设计真实子图时要回答三个问题：

1. 子图需要读取父图哪些键？
2. 子图向父图输出哪些键？
3. 同名键在父子图中的 Java 类型和合并语义是否一致？

如果父图把 `messages` 当列表追加，而子图把同名键当字符串覆盖，组合虽然可能编译成功，运行时语义却已经错误。

当前版本还有一个更隐蔽的边界：注册 `CompiledGraph` 子图时，框架不会把子图的全部 `KeyStrategy` 自动并入父图；子图结束时返回的又是完整终态。如果进入子图前父图已经在某个 Append 键中存了数据，子图终态可能包含那份旧数据，回到父图后再次 Append，造成重复。本例在子图前还没有写 `trace`，所以不会重复。真实设计应优先用专用输入/输出键交换数据，或为共享键选择明确的覆盖语义并用测试固定结果。

## 框架源码下钻

以下分析针对 `spring-ai-alibaba-graph-core 2.0.0-M1.1`。

### `StateGraph.addNode` 做了什么

`StateGraph` 对 `CompiledGraph` 有专门重载：

```java
public StateGraph addNode(String id, CompiledGraph subGraph)
```

它不会把子图“展开并复制”到父图源码里，而是创建内部的 `SubCompiledGraphNode`。这个内部节点保存子图引用，并提供自己的 Action 工厂。

### `SubCompiledGraphNodeAction` 做了什么

内部类 `SubCompiledGraphNodeAction` 实现 `AsyncNodeActionWithConfig`。其核心过程可以概括为：

```text
接收父图 OverAllState
  -> 为子图准备 RunnableConfig
  -> 调用 subGraph.graphResponseStream(state, subGraphConfig)
  -> 把子图 Flux 作为本节点的状态结果交回父图执行器
```

因此子图不是同步黑盒。框架仍然可以把子图流式输出接回父图的 Reactor 执行链。

`SubCompiledGraphNode` 自己只为承载子图 Flux 的内部键注册 `ReplaceStrategy`，不会把子图所有状态策略复制到父图。这就是共享 Append 键需要格外谨慎的源码原因。

### 为什么子图 `threadId` 会派生

源码在父子图共用同一个 Saver 时，会把子图运行 ID 派生为近似：

```text
父 threadId + 子图节点标识
```

这样同一 Saver 中父图和子图的 Checkpoint 不会相互覆盖。恢复时，`GraphRunnerContext` 还会识别 `ResumableSubGraphAction`，把恢复请求重新导向子图。

这也是为什么子图不只是方法抽取：它拥有独立的执行上下文和恢复边界。

> `SubCompiledGraphNode`、`SubCompiledGraphNodeAction` 位于框架内部实现中。学习它们是为了理解行为，业务代码不要直接依赖这些类。

## 执行路径推演

调用：

```java
SubgraphWorkflow.build().invoke(Map.of())
```

可以按以下顺序推演：

1. 父图执行 `START`，下一节点为 `validation`。
2. `validation` 对应的 Action 启动子图流。
3. 子图依次执行 `validateInput`、`normalize`，`trace` 追加两次。
4. 子图到达自己的 `END`，最终状态返回父图。
5. 父图继续执行 `task`、`finish`。
6. 父图 `END` 输出的状态包含父子两层轨迹。

## 自检问题

先口头回答，再继续做题：

1. 为什么父图注册的是 `CompiledGraph`，不是未经编译的节点列表？
2. 父图眼里的 `validation` 和流中可见的子图节点是什么关系？
3. 父子图共用 Saver 时，为什么不能直接共用完全相同的运行身份？
4. 哪些场景应该拆子图，哪些场景保留普通私有方法更简单？

## 课后 Test 练习

创建：

```text
src/test/java/cyou/yuanbaomao/graphlearning/lesson/subgraph/SubgraphPracticeTest.java
```

题目：只在测试类中创建一张“订单处理父图”和一张“地址处理子图”。

要求：

- 子图路径为 `START -> validateAddress -> normalizeAddress -> END`。
- 父图路径为 `START -> address(子图) -> createOrder -> END`。
- 父子图共同向 `trace` 追加节点名。
- `createOrder` 写入 `result = "created"`。
- 最终断言 `result`，并严格断言完整轨迹顺序为 `validateAddress`、`normalizeAddress`、`createOrder`。
- 所有实现都放在 `SubgraphPracticeTest` 内，不修改 `SubgraphWorkflow`。

运行：

```powershell
mvn -pl sellersprite-graph-learning -Dtest=SubgraphPracticeTest test
```

完成后告诉 Codex：“我完成了第 08 课练习，请检查 `SubgraphPracticeTest`。”你还需要解释父图和子图分别有几个 `START/END`，以及为什么最终只得到一份连续的业务轨迹。

下一课：[Agent 节点与结构化输出](09-agent-node.md)。

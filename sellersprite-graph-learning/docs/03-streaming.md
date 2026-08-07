# 03 流式执行：观察图步骤与节点内部数据流

## 学习目标

学完本章后，你应该能够：

- 解释 `stream()` 为什么是 Graph 的主要执行入口。
- 区分“节点级执行事件流”和“节点内部 chunk 流”。
- 正确读取 `NodeOutput` 的节点名与完整状态快照。
- 解释 Reactor 冷流、订阅和 `blockLast()` 在测试中的作用。
- 知道当前版本对原始 `Flux` 状态落盘的边界。

## 两种容易混淆的“流”

### 第一层：图步骤流

即使节点内部没有流式模型调用，`CompiledGraph.stream()` 也会逐步发出：

```text
START -> inspect -> finish -> END
```

每一步是一个 `NodeOutput`。这类流适合显示“当前执行到哪个节点”。

### 第二层：节点内部 chunk 流

节点还可以在自己返回的状态增量中放入 `Flux`，例如模型 token 或分块结果：

```text
START
  -> answer 节点 chunk A
  -> answer 节点 chunk B
  -> answer 节点 chunk C
  -> answer 节点完成
  -> END
```

这类流适合实时展示节点内部生成内容。两者共享同一个外部 `Flux<NodeOutput>`，但事件语义不同。

## 现有示例拆解：观察节点级执行

`StreamingLessonTest` 创建观察器：

```java
GraphOutputObserver observer = new GraphOutputObserver();
```

然后订阅图流：

```java
ReviewWorkflow.build()
        .stream(ReviewWorkflow.initialState(60))
        .doOnNext(observer)
        .blockLast();
```

逐段理解：

- `stream(...)` 只创建一个 Reactor `Flux`。
- `doOnNext(observer)` 注册每次输出到达时的旁路观察。
- `blockLast()` 在测试线程等待完整执行，同时触发订阅。
- 执行结束后才检查观察器，避免在流尚未完成时断言。

60 分不需要修复，因此节点序列为：

```text
__START__, inspect, finish, __END__
```

## `NodeOutput` 中有什么

主要读取：

```java
output.node();
output.state();
output.isSTART();
output.isEND();
```

`state()` 是该输出时刻合并后的整体状态，不只是本节点返回的 delta。比如 `finish` 只写 `result` 和一条 trace，但它的输出状态仍包含之前的 score、attempt 和 decision。

`GraphOutputObserver.result()` 使用最后一个输出构造测试结果。如果尚无输出，它明确抛错，避免把“没有执行”误判为空状态。

## 框架源码下钻

以下结论来自 `spring-ai-alibaba-graph-core 2.0.0-M1.1`。

### `stream()` 如何启动运行时

```text
CompiledGraph.stream
  -> streamFromInitialNode
  -> new GraphRunner(compiledGraph, config)
  -> GraphRunner.run(overallState)
  -> MainGraphExecutor / NodeExecutor
```

`GraphRunner.run()` 使用 `Flux.defer`。它是冷流：每次订阅才创建新的 `GraphRunnerContext` 并真正执行。只把 Flux 赋给变量而不订阅，节点不会运行。

### 为什么 `invoke()` 也依赖 stream

`invoke()` 对同一个流执行：

```java
stream(inputs, config)
        .last()
        .map(NodeOutput::state)
        .block();
```

所以 `stream()` 不是附加日志功能，而是核心执行模型；`invoke()` 是只关心终态时的便捷包装。

### 节点输出为何保持顺序

主执行器发出当前节点后，用 Reactor `concatWith(Flux.defer(...))` 继续下一轮调度。`concatWith` 保证前一段完成后才连接后一段，因此线性图的节点级输出有确定顺序。

### 节点内部怎样流式输出

节点的 Java 签名没有变，仍返回：

```text
CompletableFuture<Map<String, Object>>
```

只是 Map 的某个 value 可以是 `Flux<?>`。`NodeExecutor` 识别这个 value，把每个元素包装为 `StreamingOutput`，再在流结束后合并普通字段并继续主图。

当前版本的重要边界：

- 一个节点最好只放一个原始 Flux；实现只选择第一个流字段处理。
- `Flux.just("A", "B", "C")` 会发出三个 chunk，但框架不会自动把最后一个字符串当成该状态键的最终值。
- 如果需要最终答案，应由节点同时返回普通字段，例如 `answer="ABC"`，或使用模型 `ChatResponse` 的专用聚合协议。
- `StreamingOutput.chunk()` 已弃用；非消息对象练习可以读取 `getOriginData()`，并结合 `getOutputType()` 区分流中事件。
- 普通节点完成输出在当前实现中也可能使用 `StreamingOutput` 子类，不能只用 `instanceof` 判断是否为 chunk。

## 用 20 分观察中间状态

复用 `ReviewWorkflow`，节点顺序为：

```text
START, inspect, repair, inspect, repair, inspect, finish, END
```

关键快照：

| 输出节点 | score | attempt | 说明 |
| --- | ---: | ---: | --- |
| 第一次 `repair` | 40 | 1 | 修复 delta 已合并 |
| 第二次 `repair` | 60 | 2 | 已达到通过线 |
| 最后 `inspect` | 60 | 2 | decision 已变为 PASS |
| `END` | 60 | 2 | 完整终态 |

这可以证明 `NodeOutput.state()` 是合并后状态，也可以用来调试条件循环。

## 什么时候可以 `blockLast()`

适合：

- JUnit 测试。
- 后台专用任务线程需要等待完整流程。
- 命令行一次性程序。

不应机械使用：

- Web 请求线程中的 SSE 接口。
- 本应端到端保持响应式的服务链。
- 只想旁路消费事件、无需等待结果的场景。

## 常见误解

- “stream 只用于 LLM token”：普通图每个节点也会产生流事件。
- “拿到 Flux 就已执行”：冷流必须订阅。
- “NodeOutput.state 是 delta”：它是该时刻的整体状态。
- “任意原始 Flux 最后一项会自动进状态”：当前版本不会。
- “异步流意味着节点乱序”：线性主图用 concat 保持步骤顺序。

## 自检问题

1. 为什么只写 `Flux<NodeOutput> outputs = graph.stream(...)` 不会执行？
2. `invoke()` 和 `stream()` 是否使用两套执行器？
3. 节点级事件流与节点内部 chunk 流分别解决什么问题？
4. 为什么不能把简单 `Flux<String>` 的最后一项默认当作最终状态？
5. 如何从一次 20 分执行的中间快照证明修复确实发生两次？

## 课后 Test 练习

创建：

```text
src/test/java/cyou/yuanbaomao/graphlearning/lesson/streaming/StreamingPracticeTest.java
```

练习分为必做和进阶两部分。

### 必做：节点级快照

- 以 20 分运行 `ReviewWorkflow`。
- 用 `GraphOutputObserver` 收集完整输出。
- 严格断言节点序列为 `START, inspect, repair, inspect, repair, inspect, finish, END`。
- 找到两次 `repair` 输出，分别断言 `score/attempt` 为 `40/1` 和 `60/2`。
- 断言最后结果为 `PASS`。

### 进阶：节点内部 chunk

只在同一个测试类中构建 `START -> answer -> END`：

- `answer` 返回两个状态项：`chunks = Flux.just("A", "B", "C")` 和 `result = "ABC"`。
- 收集所有输出，筛选 `node="answer"` 且 `getOriginData()` 为字符串的 `StreamingOutput`。
- 断言三个原始 chunk 顺序为 A、B、C。
- 断言最终 `END` 状态包含 `result="ABC"`。
- 不要断言 `chunks` 最终自动等于 C；解释为什么。

运行：

```powershell
mvn -pl sellersprite-graph-learning -Dtest=StreamingPracticeTest test
```

完成后找 Codex 检查。你需要能够分别指出哪个断言验证“图步骤流”，哪个断言验证“节点内部流”。

下一课：[Checkpoint 与失败恢复](04-checkpoint.md)。

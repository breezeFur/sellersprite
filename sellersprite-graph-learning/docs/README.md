# Alibaba Graph 系统学习课程

这不是 API 速查表，而是一套以文档为主、源码为辅、JUnit 练习收尾的课程。目标不是让你照着示例抄出一个 Graph，而是让你能够解释状态为什么这样变化、框架下一步为什么走到这个节点，以及暂停、恢复、并行时内部发生了什么。

## 学完后你应该具备的能力

- 能从零定义状态、节点、普通边和条件边，并判断应该使用哪种 `KeyStrategy`。
- 能画出一次执行的节点路径和状态变化，不依赖调试器猜流程。
- 能解释 `StateGraph`、`CompiledGraph`、`GraphRunner`、`OverAllState`、`RunnableConfig` 各自负责什么。
- 能选择 `invoke()` 或 `stream()`，并正确消费 `NodeOutput`。
- 能设计可恢复的 `threadId`，读懂 Checkpoint 的当前节点、下一节点和状态快照。
- 能区分编译期中断与动态中断，正确注入人工反馈后恢复。
- 能处理并行分支的状态合并和无序完成，而不写出依赖竞态的测试。
- 能把子图、AI Agent 和项目业务放回同一套图执行模型中理解。

## 课程使用的源码版本

项目依赖的是：

```xml
<artifactId>spring-ai-alibaba-graph-core</artifactId>
<version>2.0.0-M1.1</version>
```

每章的“框架源码下钻”都以 Maven 本地仓库中的同版本 `sources.jar` 为依据。文档会引用源码中的类名和关键逻辑，但不会把内部实现当作稳定公共 API。以后升级版本时，应重新核对这些章节。

建议把框架看成四层：

```text
你写的工作流
  StateGraph + NodeAction + EdgeAction
          |
          v
图定义与编译
  StateGraph -> CompiledGraph
          |
          v
运行时调度
  GraphRunner -> MainGraphExecutor -> NodeExecutor
          |
          v
状态与持久化
  OverAllState + KeyStrategy + CheckpointSaver
```

前两章先使用公共 API 建立正确模型，第三章开始逐步查看运行时源码。源码下钻的目的，是解释现象和边界，不是鼓励业务代码依赖 `internal` 包。

## 学习顺序

| 阶段 | 课程 | 掌握重点 |
| --- | --- | --- |
| 图的基本语言 | [01 基础图](01-basic-graph.md) | 状态、节点、边、编译与执行 |
| 控制流 | [02 条件路由与循环](02-routing-loop.md) | 路由键、有界循环、递归上限 |
| 可观察执行 | [03 流式执行](03-streaming.md) | Reactor、`NodeOutput`、`invoke` 的本质 |
| 可恢复执行 | [04 Checkpoint](04-checkpoint.md) | `threadId`、快照、失败恢复 |
| 人工介入一 | [05 编译期中断](05-interrupt-resume.md) | 固定暂停点、恢复输入 |
| 人工介入二 | [06 动态中断](06-dynamic-interrupt.md) | `InterruptableAction`、状态更新元数据 |
| 并发执行 | [07 并行分支](07-parallel.md) | 扇出、汇聚、执行器、合并策略 |
| 结构化组合 | [08 子图](08-subgraph.md) | 父子图边界、状态与恢复 |
| AI 接入 | [09 Agent 节点](09-agent-node.md) | 模型适配、结构化输出、确定性路由 |
| 业务落地 | [10 项目集成](10-project-integration.md) | 教学能力到市场调研运行时的映射 |

必须按顺序学习。Checkpoint、中断、并行和子图都建立在前面学过的状态合并与执行路径之上。

## 每章怎么学

每章固定经过五步：

1. 先读“心智模型”，尝试不用代码复述概念。
2. 按“代码拆解”逐段阅读项目示例，不要一次吞完整个类。
3. 看“框架源码下钻”，理解公共 API 背后的关键执行路径。
4. 回答章内自检问题，再完成课后 Test；不要先看现有测试照抄。
5. 单独运行你的练习测试，成功后找 Codex 检查，再进入下一章。

## 练习规则

练习统一放在原课程 package 下，类名使用文档指定的 `*PracticeTest`。例如第一课创建：

```text
src/test/java/cyou/yuanbaomao/graphlearning/lesson/basic/BasicGraphPracticeTest.java
```

完成标准：

- 只修改或新增测试代码，不改 main 下的教学实现，除非题目明确要求。
- 不使用 `@Disabled`，不删除验收断言，不通过 `sleep` 或固定线程顺序制造“通过”。
- 测试方法名说明行为，例如 `shouldAppendTraceWhileReplacingProcessedValue`。
- 使用 AssertJ 写可读断言；异常路径同时断言异常类型和核心消息。
- 能向 Codex解释每个状态键的策略、预期节点路径以及断言为什么成立。

找 Codex 检查时，可以直接说：

```text
我完成了第 03 课练习，请检查 StreamingPracticeTest。
```

检查会覆盖：运行结果、断言强度、是否真正验证本课知识、代码可读性，以及你对执行过程的解释。

## 常用命令

运行全部基础课程和你的练习：

```powershell
mvn -pl sellersprite-graph-learning test
```

只运行一课原始测试和练习测试：

```powershell
mvn -pl sellersprite-graph-learning -Dtest=BasicGraphLessonTest,BasicGraphPracticeTest test
```

运行项目集成课程：

```powershell
mvn -pl sellersprite-graph-learning -am -Pproject-integration test
```

## 贯穿全课程的核心对象

| 对象 | 一句话职责 | 不负责什么 |
| --- | --- | --- |
| `StateGraph` | 定义节点、边和状态策略 | 不直接执行工作流 |
| `CompiledGraph` | 保存验证、编译后的可运行结构 | 不承载某次业务运行身份 |
| `OverAllState` | 保存并合并节点之间共享的数据 | 不决定下一个节点 |
| `AsyncNodeAction` | 读取整体状态，异步返回状态增量 | 不直接改图结构 |
| `AsyncEdgeAction` | 根据状态返回路由键 | 不产生业务结果状态 |
| `RunnableConfig` | 描述某次运行的身份和环境 | 不是持久化业务状态本身 |
| `NodeOutput` | 描述一个可观察的节点输出及其状态 | 不只是节点的局部返回值 |
| `CheckpointSaver` | 按运行身份保存执行快照 | 不自动保证外部副作用幂等 |

从 [第 01 课：基础图](01-basic-graph.md) 开始。

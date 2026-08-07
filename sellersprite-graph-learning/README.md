# Alibaba Graph 学习脚手架

这个模块把 Alibaba Graph 的通用能力拆成可运行的 JUnit 课程。基础 lesson 使用字符串、数字、列表和 Fake Agent，不要求连接数据库或配置模型；integration 包再把能力映射到当前项目的市场调研 Graph。

## 课程入口

请从 [课程文档目录](docs/README.md) 开始，不要先通读完整实现。每章按“心智模型 → 示例代码拆解 → Alibaba Graph 同版本源码下钻 → 自检问题 → Test 练习”组织；完成练习并通过检查后再进入下一章。

| 课程 | 入口 | 重点 |
| --- | --- | --- |
| 基础图 | [`BasicGraphLessonTest`](docs/01-basic-graph.md) | StateGraph、节点、边、invoke |
| 条件循环 | [`RoutingLoopLessonTest`](docs/02-routing-loop.md) | AsyncEdgeAction、条件边、循环上限 |
| 流式执行 | [`StreamingLessonTest`](docs/03-streaming.md) | stream、NodeOutput、观察器 |
| Checkpoint | [`CheckpointLessonTest`](docs/04-checkpoint.md) | MemorySaver、threadId、resume |
| 中断恢复 | [`InterruptLessonTest`](docs/05-interrupt-resume.md) | interruptBefore、人工状态更新 |
| 动态中断 | [`DynamicInterruptLessonTest`](docs/06-dynamic-interrupt.md) | InterruptableAction、InterruptionMetadata |
| 并行分支 | [`ParallelLessonTest`](docs/07-parallel.md) | 并行边、Executor、聚合 |
| 子图 | [`SubgraphLessonTest`](docs/08-subgraph.md) | CompiledGraph 作为节点 |
| Agent 节点 | [`AgentLessonTest`](docs/09-agent-node.md) | Fake Agent、结构化结果校验 |
| 项目集成 | [`ResearchGraphRuntimeContractTest`](docs/10-project-integration.md) | Graph 配置、执行器、真实 AI Agent 契约 |

> 版本提示：在当前 `2.0.0-M1.1` 中，编译期 `interruptBefore` 暂停时，`stream()` 的最后一个可见输出是中断前已完成的节点；暂停目标通过 `lastStateOf(config).next()` 判断。恢复仍使用同一 `threadId` 和 `RunnableConfig.builder(config).resume()`，人工状态通过恢复调用的输入 Map 合并；`addStateUpdate` 主要服务于实现 `InterruptableAction` 的动态中断节点。

## 运行

```powershell
mvn -pl sellersprite-graph-learning test
```

启用项目集成依赖时使用：

```powershell
mvn -pl sellersprite-graph-learning -am -Pproject-integration test
```

真实模型适配器 `SpringAiLearningAgent` 默认不会在测试中调用。它故意把 JSON 解析留给结构化输出课程，避免把模型协议伪装成通用 Graph 能力。

## 推荐阅读顺序

先按编号读课程文档；文档要求查看代码片段时，再打开对应工作流和原始测试。完成该章 `*PracticeTest` 后找 Codex 检查，通过后进入下一章。十章结束后再通读 `common` 中的观察器与结果对象，以及 `integration/research/ResearchGraphLearningMap`。

基础课程只需掌握 `StateGraph`、`OverAllState`、`KeyStrategy`、`AsyncNodeAction`、`AsyncEdgeAction`、`CompiledGraph` 和 `RunnableConfig`。Checkpoint、并行、子图和 Agent 都在同一套状态模型上扩展。

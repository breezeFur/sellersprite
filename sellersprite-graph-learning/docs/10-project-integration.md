# 10 项目集成：把课程概念放回市场调研运行时

## 学习目标

学完本章后，你应该能够：

- 从生产代码中识别 Graph 定义、节点适配、运行配置、Checkpoint 和 AI Agent 边界。
- 解释教学示例为什么刻意不直接依赖市场调研实体。
- 沿着 `ResearchGraphExecutor` 追踪一次任务如何进入 Graph、如何恢复和如何结束。
- 说明数据库 Saver 与 `workflowVersion:jobId` 组成的运行身份解决了什么问题。
- 编写轻量契约测试，防止跨模块关键入口被无意改变。

## 从课程地图开始

`ResearchGraphLearningMap` 保存了通用能力与生产阶段的对应关系：

| 市场调研能力 | 对应课程 | 迁移的核心思想 |
| --- | --- | --- |
| 输入与阶段校验 | `lesson.basic` | 节点读取状态、写入状态增量 |
| 多来源采集 | `lesson.parallel` | 独立任务扇出并在下游汇聚 |
| 数据质量处理 | `lesson.routing` | 根据状态通过、修复或失败 |
| AI 分析 | `lesson.agent` | 模型适配与确定性工作流解耦 |
| 过程进度 | `lesson.streaming` | 消费节点级输出并向外发布 |
| 失败续跑 | `lesson.checkpoint` | 持久化状态、节点和下一节点 |

这张表表达概念映射，不表示生产图当前一定逐项采用同样的演示结构。学习代码和生产代码的复杂度、稳定性要求不同。

## 生产执行链总览

```text
调度器抢占 ResearchExecutionLease
          |
          v
ResearchGraphExecutor.submit
          |
          v
根据 workflowVersion:jobId 构造 RunnableConfig
          |
          +-- 有历史快照 -> resume()
          |
          v
CompiledGraph.stream(initialState, config)
          |
          v
ResearchWorkflowNodes.execute(state, phase)
          |
          v
ResearchWorkflowStepService 执行业务阶段
          |
          v
NodeOutput 流 -> 日志/状态服务 -> 成功或失败处理
```

## 生产代码拆解

### 第一层：`ResearchGraphConfiguration` 定义图

它先声明 Graph 需要持久化的运行状态：

```java
StateGraph stateGraph = new StateGraph("market-research", () -> Map.of(
        STATE_JOB_ID, new ReplaceStrategy(),
        STATE_WORKFLOW_VERSION, new ReplaceStrategy(),
        STATE_EXECUTION_TOKEN, new ReplaceStrategy(),
        STATE_LAST_NODE, new ReplaceStrategy()));
```

这些键使用覆盖策略，因为它们表示当前唯一身份或最新节点，不是要累计的历史列表。

随后根据 `ResearchPhase.values()` 注册节点并串联：

```java
for (ResearchPhase phase : phases) {
    stateGraph.addNode(
            phase.getNodeCode(),
            node_async(state -> nodes.execute(state, phase)));
}
```

这展示了一个重要设计：Graph 负责阶段顺序，`ResearchWorkflowNodes` 负责把通用状态适配给业务服务。

### 第二层：`ResearchWorkflowNodes` 是框架与业务的防腐层

节点适配器从 `OverAllState` 取出 `jobId` 和 `executionToken`，校验后调用：

```java
stepService.execute(jobId, executionToken, phase);
return Map.of(STATE_LAST_NODE, phase.getNodeCode());
```

业务服务不需要接收 `OverAllState`，因此不会与 Graph 框架到处耦合。将来替换编排方式时，核心业务步骤仍然可以复用。

### 第三层：生产环境使用 `MysqlSaver`

```java
MysqlSaver.builder()
        .dataSource(dataSource)
        .createOption(createOption)
        .build();
```

课程用 `MemorySaver` 是为了零配置；生产任务可能跨请求、跨线程甚至跨进程恢复，因此必须使用持久化 Saver。

编译时设置：

```java
CompileConfig.builder()
        .saverConfig(SaverConfig.builder().register(checkpointSaver).build())
        .releaseThread(false)
        .build();
```

`releaseThread(false)` 保留执行历史，后续重试才能查到快照。

### 第四层：`ResearchGraphExecutor` 决定新执行还是恢复

运行身份是：

```java
String threadId = lease.workflowVersion() + ":" + lease.jobId();
```

加入 `workflowVersion` 很关键：同一个业务任务在工作流结构升级后，不能盲目拿旧图的“下一节点”交给新图恢复。

执行器先查询：

```java
if (graph.lastStateOf(initialConfig).isEmpty()) {
    return initialConfig;
}
return RunnableConfig.builder(initialConfig).resume().build();
```

这正是第 04、05 课的生产版本：相同 `threadId` 找到历史 Checkpoint，再用 resume 元数据告诉运行时从快照继续。

### 第五层：生产执行消费 `NodeOutput` 流

```java
graph.stream(initialState, runnableConfig)
        .doOnNext(output -> log.debug("... node={}", output.node()))
        .blockLast();
```

任务运行在线程池中，所以这里允许执行线程等待完整流程；Web 请求线程则不应机械照搬 `blockLast()`。最后一个输出为空被视为明确错误，而不是假成功。

### 第六层：报告子图通过端口调用 Curation

`DefaultResearchAnalysisStageAdapter` 实现项目自己的 `ResearchAnalysisStagePort`。报告子图只调用
`runInitial(jobId, parentExecutionToken)`；适配器负责用父 Graph 的执行令牌启动初次分析，并把工作交给现有
Curation 执行链。模型编排仍不进入 Graph 状态，Graph 只观察该阶段成功返回或抛出异常。

## 框架源码下钻：生产恢复怎样进入 GraphRunnerContext

在 `2.0.0-M1.1` 中，`GraphRunnerContext` 用以下条件区分恢复与新执行：运行配置带有人类反馈元数据，或指定了 Checkpoint ID。

恢复初始化会：

1. 从 Saver 取出与运行配置匹配的 Checkpoint。
2. 把 `nextNodeId` 设为快照记录的下一节点。
3. 将快照状态合并进新的 `OverAllState`。
4. 记录从哪个节点恢复，然后交回主执行器继续调度。

生产执行器只需要稳定提供 `threadId + resume()`；具体读取快照和恢复下一节点由框架完成。

## 为什么教学模块必须与业务隔离

如果第一课就引入租约、数据库、22 个阶段、SSE 和真实模型，你很难判断失败来自 Graph 概念还是业务依赖。教学模块使用字符串、数字和 Fake Agent，是为了把变量控制住。

正确学习路径是：

```text
先在小图中证明概念 -> 能解释框架源码 -> 再在生产链路中识别同一概念
```

而不是把生产类复制一份叫“教学示例”。

## 自检问题

1. 为什么 `ResearchWorkflowStepService` 不直接接收 `OverAllState`？
2. 为什么生产 `threadId` 同时包含工作流版本和任务 ID？
3. `MemorySaver` 和 `MysqlSaver` 的公共职责相同，运行保证有什么不同？
4. `blockLast()` 为什么在后台任务线程里可接受，在 Web 请求中却要谨慎？
5. 从 `ResearchGraphExecutor.submit` 开始，口头说出一次恢复执行的完整路径。

## 课后 Test 练习

创建：

```text
src/test/java/cyou/yuanbaomao/graphlearning/integration/research/ResearchGraphContractPracticeTest.java
```

本题需要启用 `project-integration` profile。目标是写一个不启动 Spring、不连接数据库的跨模块契约测试。

要求：

- 使用反射加载 `ResearchGraphConfiguration`、`ResearchGraphExecutor`、`ResearchWorkflowNodes` 和 `DefaultResearchAnalysisStageAdapter`。
- 断言配置类存在返回类型为 `CompiledGraph` 的 `marketResearchGraph` 方法。
- 断言配置类存在返回类型为 `MysqlSaver` 的 `marketResearchCheckpointSaver` 方法。
- 断言 `ResearchGraphExecutor.submit` 只有一个参数，参数简单类名为 `ResearchExecutionLease`，返回类型为 `void`。
- 断言 `ResearchWorkflowNodes` 公开 `STATE_JOB_ID` 与 `STATE_EXECUTION_TOKEN` 字段。
- 断言真实分析阶段适配器中包含 `runInitial` 方法。
- 测试只验证边界契约，不调用私有方法，不创建数据库或真实模型。

运行：

```powershell
mvn -pl sellersprite-graph-learning -am -Pproject-integration -Dtest=ResearchGraphContractPracticeTest "-Dsurefire.failIfNoSpecifiedTests=false" test
```

完成后找 Codex 检查。除了测试通过，你还需要选其中一个断言说明：如果这个契约被改坏，教学模块与生产模块之间会出现什么具体影响。

课程完成后回到 [课程目录](README.md)，按检查流程逐课补齐练习。十题全部通过后，再让 Codex进行一次综合掌握度复盘。

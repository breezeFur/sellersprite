# 09 Agent 节点：让不确定的模型服从确定的图

## 学习目标

学完本章后，你应该能够：

- 解释 Alibaba Graph 为什么不需要知道节点里是不是大模型。
- 为模型调用定义小而稳定的输入、输出契约。
- 使用 Fake Agent 测试路由，不让单元测试依赖网络和模型随机性。
- 在模型结果进入状态和条件边之前完成结构化校验。
- 区分“模型做判断”和“业务规则做最终路由”。

## 先建立心智模型

对 Graph 来说，AI 节点仍然只是一个普通节点：

```text
OverAllState -> AsyncNodeAction -> CompletableFuture<Map<String, Object>>
```

框架不关心这个 Action 内部是字符串处理、数据库查询，还是 `ChatClient` 调用。所谓“Agent 节点”，是我们在应用层给普通节点接入了一个 AI 端口。

正确的职责分工是：

```text
模型：根据文本生成 classification + confidence
应用适配器：把模型响应转换成 AiAgentResult
Graph 节点：校验结果并写入状态
普通 Java 规则：根据 confidence 产生 APPROVED/REJECTED
条件边：按照决定选择下一节点
```

这样模型不能用一段不可控文本直接决定任意目标节点。

## 示例代码拆解

### 第一步：定义最小端口

```java
@FunctionalInterface
public interface LearningAiAgent {
    AiAgentResult execute(AiAgentRequest request);
}
```

Graph 依赖这个接口，不依赖 `ChatClient`。输入和输出使用 record：

```java
public record AiAgentRequest(String prompt) {}
public record AiAgentResult(String classification, double confidence) {}
```

契约越小，越容易替换实现、制造边界输入和编写稳定测试。

### 第二步：用 Fake 隔离外部模型

```java
public final class FakeLearningAiAgent implements LearningAiAgent {
    private final Function<AiAgentRequest, AiAgentResult> handler;

    @Override
    public AiAgentResult execute(AiAgentRequest request) {
        return handler.apply(request);
    }
}
```

测试可以精确指定 Agent 返回 0.79、0.80、空分类或越界置信度。相比 Mockito，这个 Fake 还可以复用于多个课程场景，并把关注点留在 Graph 行为上。

### 第三步：节点调用并校验 Agent

`AgentReviewWorkflow.classify` 的逻辑是：

```java
String input = state.value(GraphStateKeys.INPUT, String.class)
        .orElseThrow(() -> new IllegalStateException("缺少 AI 输入"));
AiAgentResult result = agent.execute(new AiAgentRequest(input));
```

拿到结果后，先检查分类非空、置信度位于 `[0, 1]`，再写入状态。校验发生在条件路由之前，因此坏数据不会污染后续决策。

### 第四步：确定性规则做决定

```java
return Map.of(
        GraphStateKeys.DECISION,
        confidence >= 0.8 ? APPROVED : REJECTED,
        GraphStateKeys.TRACE, List.of(SCORE));
```

阈值是业务规则，不藏在 Prompt 中。它可以测试、审计和版本化。

### 第五步：真实模型只是另一个适配器

`SpringAiLearningAgent` 使用：

```java
chatClient.prompt()
        .system("只输出 JSON，字段为 classification 和 confidence。")
        .user(request.prompt())
        .call()
        .entity(AiAgentResult.class);
```

`.entity(...)` 解决“反序列化为类型”的问题，但不能证明业务值合理。因此 Graph 节点仍要做非空和范围校验。

## 状态与路径推演

Fake 返回：

```text
classification = "safe"
confidence = 0.98
```

状态变化：

| 节点 | 读取 | 写入 |
| --- | --- | --- |
| `classify` | `input` | `classification=safe`、`confidence=0.98` |
| `score` | `confidence` | `decision=APPROVED` |
| 条件边 | `decision` | 不写状态，路由到 `finish` |
| `finish` | `decision` | `result=APPROVED` |

路径为：

```text
START -> classify -> score -> finish -> END
```

虽然 `APPROVED` 和 `REJECTED` 都进入同一个 `finish`，条件边仍有教学意义：未来可以轻易把两种决定连到不同处理节点。

## 框架源码下钻

以下分析针对 `spring-ai-alibaba-graph-core 2.0.0-M1.1`。

### `AsyncNodeAction` 才是框架边界

源码中的 `AsyncNodeAction` 本质是：

```java
Function<OverAllState, CompletableFuture<Map<String, Object>>>
```

`node_async` 只是把同步的 `NodeAction` 包成已经完成或异常完成的 `CompletableFuture`。这说明：

- Graph 没有强制节点必须调用 LLM。
- 同步节点也被统一纳入异步调度接口。
- 节点失败会进入异常完成的 Future，再由运行时向 Reactor 错误信号传播。

### 运行时怎样处理 Agent 节点

`NodeExecutor` 只会调用：

```text
action.apply(overallState, runnableConfig)
  -> 等待 Future
  -> 合并返回的状态增量
  -> 根据边求下一节点
  -> 输出 NodeOutput 并写 Checkpoint
```

它不会解析 Prompt、JSON 或置信度。这些都是应用层契约。理解这一点后，你就不会把所有 Agent 逻辑塞进 Graph 框架配置，也不会误以为更换模型需要重写图执行器。

## 生产设计要点

- Prompt 约束、JSON Schema 和 Java 校验要同时存在，三者用途不同。
- 模型返回失败要明确抛错；不要把空结果偷偷变成“拒绝”或“通过”。
- 记录模型、Prompt 版本和证据引用，但不要在日志中泄露敏感完整输入。
- 节点重试时要考虑模型调用费用和幂等记录。
- 将高风险动作放在确定性规则或人工中断之后，不让模型输出直接触发副作用。

## 自检问题

1. 为什么 `LearningAiAgent` 接口比直接在工作流里注入 `ChatClient` 更容易测试？
2. `.entity(AiAgentResult.class)` 成功后，为什么还要检查置信度范围？
3. 阈值 0.8 应该放在 Prompt、Agent 适配器还是 Graph 规则节点？为什么？
4. Alibaba Graph 如何知道这是一个 AI 节点？答案对架构设计有什么启发？

## 课后 Test 练习

创建：

```text
src/test/java/cyou/yuanbaomao/graphlearning/lesson/agent/AgentPracticeTest.java
```

题目：测试置信度阈值边界，并证明 Graph 把原始输入原样交给 Agent。

要求：

- 使用 `FakeLearningAiAgent`，禁止调用真实模型。
- 用 `AtomicReference<AiAgentRequest>` 捕获 Agent 收到的请求。
- 场景一返回 `classification="manual-review"`、`confidence=0.79`，断言最终为 `REJECTED`。
- 场景二返回同一分类、`confidence=0.80`，断言最终为 `APPROVED`。
- 两个场景都断言 `classification` 和 `confidence` 被保留在最终状态中。
- 至少一次断言捕获到的 `prompt` 与传入 Graph 的 `input` 完全相同。
- 可使用 `@ParameterizedTest`，也可以写两个命名清晰的 `@Test`。

运行：

```powershell
mvn -pl sellersprite-graph-learning -Dtest=AgentPracticeTest test
```

完成后找 Codex 检查，并解释为什么 0.79 与 0.80 的结果不同、这个差异由模型还是业务代码决定。

下一课：[映射到市场调研项目](10-project-integration.md)。

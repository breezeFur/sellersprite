# 06 动态中断：由节点在运行时决定是否暂停

## 学习目标

学完本章后，你应该能够：

- 区分编译期固定中断与运行时动态中断。
- 实现同时具备执行和中断能力的 Action。
- 解释 `interrupt -> apply -> interruptAfter` 三个时机。
- 使用 `addStateUpdate` 在动态节点恢复前合并人工状态。
- 从 `InterruptionMetadata` 读取暂停节点、状态和自定义说明。

## 先建立心智模型

第 05 课的暂停点写在编译配置中，每次到达都会触发。动态中断则由节点根据本次运行配置或状态判断：

```text
START -> prepare -> dynamicReview
                       |
              interrupt() 有元数据
               /                 \
            暂停                  继续 apply()
             |                        |
          人工反馈                    v
             +--------------------> finish -> END
```

适用场景包括：只有高风险数据才人工审核、只有缺少授权时暂停、工具调用需要用户批准。

## 示例代码拆解

### 第一步：一个类实现两个协议

```java
public final class DynamicReviewAction
        implements AsyncNodeAction, InterruptableAction {
```

`AsyncNodeAction` 定义节点真正执行什么；`InterruptableAction` 定义执行前后是否暂停。

### 第二步：正常节点动作

```java
@Override
public CompletableFuture<Map<String, Object>> apply(OverAllState state) {
    return CompletableFuture.completedFuture(Map.of(
            GraphStateKeys.TRACE, List.of("dynamicReview")));
}
```

如果不中断，Action 像普通节点一样返回状态增量。

### 第三步：执行前动态判断

```java
@Override
public Optional<InterruptionMetadata> interrupt(
        String nodeId, OverAllState state, RunnableConfig config) {
    if (config.metadata(RunnableConfig.HUMAN_FEEDBACK_METADATA_KEY).isPresent()) {
        return Optional.empty();
    }
    return Optional.of(InterruptionMetadata.builder(nodeId, state).build());
}
```

本示例采用最小规则：没有恢复/人工反馈元数据就暂停；有元数据就继续。真实业务通常还会检查风险状态、审批内容和权限。

### 第四步：保留中断能力地注册节点

```java
graph.addNode(
        REVIEW,
        AsyncNodeActionWithConfig.of(new DynamicReviewAction()));
```

适配器会保留原 Action 的 `InterruptableAction` 能力，运行时可以识别。

### 第五步：恢复前注入状态更新

```java
RunnableConfig resume = RunnableConfig.builder(config)
        .resume()
        .addStateUpdate(Map.of(GraphStateKeys.APPROVED, true))
        .build();
```

动态节点执行器会先把 `STATE_UPDATE` 元数据合并进当前状态，再调用 `interrupt()`。随后 `resume()` 提供的反馈标记让本例不再暂停，`apply()` 才真正执行。

## 框架源码下钻

以下结论来自 `spring-ai-alibaba-graph-core 2.0.0-M1.1`。

### 真正被执行器识别的接口

`NodeExecutor` 判断：

```text
action instanceof InterruptableAction
```

源码中还有名字相似的 `InterruptableActionWithConfig`，但在该版本主执行链没有实际调用者，不应作为本课程主要 API。

### 三段式调用顺序

```text
interrupt(nodeId, state, config)
  -> 返回 empty 才执行 apply(state, config)
  -> apply 完成后调用 interruptAfter(...)
```

动态前置中断时，当前 Action 尚未执行，也不会凭空创建一个“节点已完成”的 Checkpoint。它依赖上一节点已保存且 next 指向当前动态节点的快照。

### `addStateUpdate` 在哪里生效

执行器检测到动态 Action 后先读取 `RunnableConfig.STATE_UPDATE_METADATA_KEY`，如果值是 Map，就按图策略合并进当前状态，然后才调用 `interrupt()`。

所以 hook 能看到刚注入的人工状态。普通静态 interruptBefore 节点不经过这段专用逻辑，不能混用概念。

### `InterruptionMetadata` 不只是标记

它继承 `NodeOutput`，包含：

- 暂停节点 ID。
- 用于展示的 `OverAllState`。
- 自定义 metadata。
- 工具反馈和自动批准工具信息。

因此动态暂停时，`stream().blockLast()` 可以得到节点为 `dynamicReview` 的 `InterruptionMetadata`，与第 05 课静态中断看到上一成功节点不同。

### 执行后中断 `interruptAfter`

它收到：

```text
stateBeforeMerge + actionResult + config
```

如果决定暂停，运行时会先合并 actionResult、计算下一节点并保存 Checkpoint，再返回中断元数据。恢复时不会重跑当前节点。

注意 hook 展示的 metadata state 可能是合并前视图，而真正恢复快照是合并后状态。这两份信息用途不同。

## 本示例恢复为 false 时会怎样

如果恢复配置包含：

```java
.resume()
.addStateUpdate(Map.of(APPROVED, false))
```

流程会继续，因为 `interrupt()` 判断的是是否存在反馈标记；`finish` 再读取 `approved=false`，输出 `rejected`。

这说明：

- “是否继续执行”由中断 hook 决定。
- “继续后得到什么业务结果”由状态和普通节点决定。

二者不应混为同一个布尔开关。

## 常见误解

- “动态中断只是动态配置 interruptBefore”：不是，它是 Action hook。
- “返回 InterruptionMetadata 后 apply 已执行”：前置 interrupt 时尚未执行。
- “addStateUpdate 会自动适用于所有节点”：当前专门用于可中断 Action 路径。
- “resume 就等于批准”：本示例 resume 只表示已有反馈，批准或拒绝在 approved 状态中。
- “hook 返回 empty 表示节点结束”：它只表示不要暂停，接下来才执行 apply。

## 自检问题

1. `DynamicReviewAction` 为什么要同时实现两个接口？
2. 首次暂停时，`apply()` 是否已经追加 dynamicReview 轨迹？
3. `addStateUpdate` 在调用 interrupt 前还是后合并？
4. 为什么 `resume + approved=false` 仍会继续执行但最终拒绝？
5. interruptAfter 暂停后恢复时，当前节点是否重跑？

## 课后 Test 练习

创建：

```text
src/test/java/cyou/yuanbaomao/graphlearning/lesson/interrupt/DynamicInterruptPracticeTest.java
```

使用 `DynamicInterruptWorkflow`：

- 首次执行后断言输出是 `InterruptionMetadata`。
- 断言暂停节点为 `REVIEW`。
- 断言暂停状态中还没有 `APPROVED`。
- 使用同一 threadId，配置 `resume()` 与 `addStateUpdate(APPROVED=false)`。
- 断言恢复后最终 `result="rejected"`。
- 严格断言最终轨迹为 `PREPARE, REVIEW, FINISH`。
- 对比第 05 课，写一个注释说明为什么本题首次输出节点是 REVIEW，而静态中断题是 GENERATE。

运行：

```powershell
mvn -pl sellersprite-graph-learning -Dtest=DynamicInterruptPracticeTest test
```

完成后找 Codex 检查，并解释恢复标记与 approved 业务值各自负责什么。

下一课：[并行分支与汇聚](07-parallel.md)。

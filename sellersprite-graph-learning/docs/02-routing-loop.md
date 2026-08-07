# 02 条件路由与循环：让状态决定下一步

## 学习目标

学完本章后，你应该能够：

- 区分路由标签与目标节点 ID。
- 使用 `addConditionalEdges` 表达 if/else 和循环。
- 解释为什么条件边能看到当前节点刚写入的状态。
- 用业务状态限制重试次数，而不是依赖框架保护上限。
- 测试完整节点轨迹，而不只断言最终结果。

## 先建立心智模型

普通边回答“固定去哪里”；条件边回答“根据当前状态选择去哪里”。循环不是特殊节点类型，只是某个路由目标指回前面的节点。

```text
START -> inspect -- PASS ------> finish -> END
              |
              +-- FAIL -------> finish
              |
              +-- RETRY -> repair -> inspect
```

条件 Action 返回的是逻辑标签，例如 `RETRY`。`Map.of(RETRY, REPAIR)` 再把标签映射到真实节点。不要默认标签与节点名是同一个字符串。

## 示例规则

`ReviewWorkflow` 的状态包括：

- `score`：当前分数。
- `attempt`：已经修复的次数。
- `decision`：本轮检查决定。
- `result`：流程最终结果。
- `trace`：节点轨迹。

业务规则：

```text
score >= 60              -> PASS
score < 60 且 attempt>=3 -> FAIL
其他                      -> RETRY
```

每次 `repair` 把分数加 20，把尝试次数加 1。

## 代码拆解

### 第一步：注册节点

```java
graph.addNode(INSPECT, node_async(ReviewWorkflow::inspect));
graph.addNode(REPAIR, node_async(ReviewWorkflow::repair));
graph.addNode(FINISH, node_async(ReviewWorkflow::finish));
```

`inspect` 只计算决定：

```java
String decision = score >= PASS_SCORE
        ? PASS
        : attempts >= MAX_ATTEMPTS ? FAIL : RETRY;
return Map.of(
        GraphStateKeys.DECISION, decision,
        GraphStateKeys.TRACE, List.of(INSPECT));
```

这一步没有自己调用 `repair`。节点只写事实，边负责控制流。

### 第二步：声明条件边的映射

```java
graph.addConditionalEdges(
        INSPECT,
        AsyncEdgeAction.edge_async(ReviewWorkflow::route),
        Map.of(PASS, FINISH, RETRY, REPAIR, FAIL, FINISH));
```

可以分成两部分理解：

```text
route(state) 返回 RETRY
mapping.get(RETRY) 得到 repair
```

如果 Action 返回了映射中不存在的标签，编译阶段通常无法预知，运行到这里会抛出缺少映射目标的异常。

### 第三步：让修复节点回到检查节点

```java
graph.addEdge(REPAIR, INSPECT);
```

这一条普通边就是循环。是否继续循环仍由下一次 `inspect` 的条件边决定。

### 第四步：用状态实现有界循环

```java
return Map.of(
        GraphStateKeys.SCORE, score + SCORE_STEP,
        GraphStateKeys.ATTEMPT, attempts + 1,
        GraphStateKeys.TRACE, List.of(REPAIR));
```

尝试次数放在 `OverAllState`，Checkpoint 恢复后仍然存在。如果只用 Java 局部变量计数，跨节点和跨恢复都不可靠。

## 用 20 分手工推演

| 执行节点 | 进入节点时 | 节点写入 | 下一步 |
| --- | --- | --- | --- |
| `inspect` | `score=20, attempt=0` | `decision=RETRY` | `repair` |
| `repair` | `20, 0` | `score=40, attempt=1` | `inspect` |
| `inspect` | `40, 1` | `decision=RETRY` | `repair` |
| `repair` | `40, 1` | `score=60, attempt=2` | `inspect` |
| `inspect` | `60, 2` | `decision=PASS` | `finish` |
| `finish` | `decision=PASS` | `result=PASS` | `END` |

轨迹应为：

```text
inspect, repair, inspect, repair, inspect, finish
```

## 框架源码下钻

以下结论来自 `spring-ai-alibaba-graph-core 2.0.0-M1.1`。

### 条件边如何存入图

`StateGraph.addConditionalEdges` 把条件 Action 与标签映射封装进内部 `EdgeCondition`。`AsyncEdgeAction` 会被转换成能返回 `Command` 的 Action，但公共课程只需要返回字符串标签。

### 为什么条件边看到的是新状态

`NodeExecutor` 完成节点 Future 后，顺序是：

```text
合并节点 delta
  -> 根据当前节点和合并后状态计算 nextNode
  -> 保存输出与 Checkpoint
```

因此计数节点写入 `count + 1` 后，紧随其后的条件边读到的是新 count。这决定了循环边界是否会出现 off-by-one。

内部路由逻辑可以概括为：

```text
label = condition.apply(state)
targetNodeId = mappings.get(label)
```

标签不存在时运行失败；mapping 的 value 在编译时会检查是否指向真实节点或 `END`。

### async 不等于并发路由

`edge_async` 和 `node_async` 一样，只是 Future 接口适配。当前内部路由会等待条件 Future 的结果，再决定目标节点，不会让多个条件目标自动并行。

### 框架递归上限不是业务规则

`CompileConfig` 有递归上限，当前默认值为 100。运行时按调度轮次计数，它不等同于“业务最多重试 100 次”，而且触发保护时不应被当作正常业务失败分支。

正确做法是同时拥有：

- 业务上限：`attempt >= MAX_ATTEMPTS`，决定明确的 `FAIL`。
- 框架上限：防止代码缺陷导致无限图执行。

## 常见误解

- “条件 Action 返回目标节点”：它返回标签，映射再决定节点。
- “循环需要 Loop 节点”：不需要，边指回旧节点就是循环。
- “路由在节点状态合并前执行”：当前执行链是先合并，再路由。
- “设置 recursionLimit 就完成了重试设计”：它只是最后防线。
- “只断言最终 PASS 就证明路由正确”：可能走错多次仍碰巧得到 PASS，必须断言轨迹和次数。

## 自检问题

1. `RETRY` 与 `repair` 分别是什么，为什么最好不要混成一个概念？
2. 初始分数 59 会经过哪些节点，最终 attempt 是多少？
3. 如果 `repair` 忘记增加 attempt，可能发生什么？
4. 条件边为什么能读取到 `inspect` 刚写入的 decision？
5. 业务上限与框架递归上限分别保护什么？

## 课后 Test 练习

创建：

```text
src/test/java/cyou/yuanbaomao/graphlearning/lesson/routing/RoutingLoopPracticeTest.java
```

只在测试中构建计数图：

```text
START -> work -- again --> work
               \- done --> finish -> END
```

要求：

- 初始 `count=0`。
- `work` 每次把 count 加 1，并向 trace 追加 `work`。
- 条件 Action 在合并后的 `count < 3` 时返回标签 `again`，否则返回 `done`。
- mapping 将 `again` 映射到节点 `work`，将 `done` 映射到节点 `finish`。
- `finish` 写入 `result="completed"` 并追加轨迹。
- 断言最终 `count=3`、结果和完整轨迹 `work, work, work, finish`。
- 使用 `stream()` 额外断言最后一个输出 `isEND()` 为 true。

加分题：再建一张缺少 `done` 映射的图，断言运行时明确失败，并解释为什么编译阶段无法知道 Action 将返回 `done`。

运行：

```powershell
mvn -pl sellersprite-graph-learning -Dtest=RoutingLoopPracticeTest test
```

完成后找 Codex 检查，并逐轮说出条件边读到的 count。

下一课：[流式执行与输出观察](03-streaming.md)。

# 05 编译期中断：在固定审核点暂停和恢复

## 学习目标

学完本章后，你应该能够：

- 使用 `interruptBefore` 声明固定人工审核点。
- 区分“最后完成节点”和“等待执行节点”。
- 解释静态中断为什么依赖 Checkpoint。
- 使用相同 `threadId` 和 `resume()` 注入人工状态后继续。
- 区分本章的恢复输入与下一章的 `addStateUpdate`。

## 先建立心智模型

固定审核点像一扇每次都要经过的门：

```text
START -> generate -> [暂停：下一节点 review]
                         |
                    人工给出 approved
                         v
                      review -> finish -> END
```

`interruptBefore(review)` 的意思是：上游已经完成、下一步准备执行 review 时暂停。它不是在 review 执行一半时暂停。

## 示例代码拆解

### 第一步：正常定义节点与边

```java
graph.addEdge(StateGraph.START, GENERATE);
graph.addEdge(GENERATE, REVIEW);
graph.addEdge(REVIEW, FINISH);
graph.addEdge(FINISH, StateGraph.END);
```

`review` 在图结构中仍是普通节点。中断能力属于编译配置，不需要修改节点实现。

### 第二步：配置固定暂停点

```java
return graph.compile(CompileConfig.builder()
        .saverConfig(SaverConfig.builder()
                .register(new MemorySaver())
                .build())
        .interruptBefore(REVIEW)
        .releaseThread(false)
        .build());
```

中断必须配合 Saver。暂停后下一次请求可能来自另一个时间点，运行位置必须能保存和重新读取。

编译时框架会验证 `REVIEW` 是否真实存在，拼错节点名会尽早失败。

### 第三步：首次执行到暂停点

```java
NodeOutput lastVisible = graph.stream(Map.of(), initialConfig).blockLast();
```

在当前版本，静态 `interruptBefore` 的暂停信号不会作为普通业务 `NodeOutput` 留在 `stream()` 结果中，因此 `blockLast()` 得到的是最后一个已完成节点 `generate`。

等待执行的节点要从快照读取：

```java
graph.lastStateOf(initialConfig)
        .orElseThrow()
        .next(); // review
```

### 第四步：带人工结果恢复

```java
RunnableConfig resumeConfig = RunnableConfig.builder(initialConfig)
        .resume()
        .build();

NodeOutput result = graph.stream(
        Map.of(GraphStateKeys.APPROVED, true),
        resumeConfig)
        .blockLast();
```

恢复输入 Map 会与快照状态合并，然后从 `review` 继续。`finish` 读取 `approved`，产生 approved 或 rejected。

## 框架源码下钻

以下结论来自 `spring-ai-alibaba-graph-core 2.0.0-M1.1`。

### 中断检查发生在哪里

`generate` 完成时，执行器已经：

```text
合并 generate 的 delta
  -> 计算 nextNodeId=review
  -> 保存 checkpoint(generate, review)
```

下一轮主执行器检查：

```text
interruptsBefore 是否包含 nextNodeId
```

命中后返回中断元数据，不调用 review Action。

所以快照语义恰好是：

```text
node=generate 已完成
next=review 待执行
```

### 为什么恢复不会马上再次暂停

恢复初始化会把 `currentNodeId` 置为 null，把 `nextNodeId` 设为快照下一节点。源码中的 `shouldInterruptBefore` 对“没有前一个当前节点”的恢复场景直接返回 false，于是 review 可以真正执行。

### 静态中断怎样修改状态

本章普通 `review` 节点不是 `InterruptableAction`。最简单的方式是像示例一样，把人工状态作为恢复调用的输入 Map。

也可以先调用：

```java
graph.updateState(snapshot.config(), Map.of(GraphStateKeys.APPROVED, true));
```

再用返回配置恢复。不要把下一章的 `addStateUpdate()` 当作所有中断的通用状态注入方式；那个元数据由动态中断节点执行器专门消费。

### 补充：interruptAfter

框架还支持在节点完成后暂停。普通模式下，节点结果已合并、下一节点已计算并保存快照，恢复时直接去下一节点。

高级配置 `interruptBeforeEdge(true)` 可以让 interruptAfter 暂停在条件边计算之前，允许人工修改状态后重新决定路由。本课程先掌握 `interruptBefore`，不要把两种时机混在一个练习里。

## 状态与输出推演

首次运行：

| 事件 | 已执行节点 | 快照 next | 可见最后输出 |
| --- | --- | --- | --- |
| 到达暂停点 | `generate` | `review` | `generate` |

恢复批准：

```text
输入 approved=true
  -> review 追加轨迹
  -> finish 写 result=approved
  -> END
```

最终业务轨迹：

```text
generate, review, finish
```

## 常见误解

- “暂停输出的 node 就是 review”：静态中断时最后可见业务输出是 generate。
- “last output 和 next 一样”：前者已完成，后者待执行。
- “恢复可以换 threadId”：换 ID 后 Saver 找不到原运行。
- “resume 会携带人工结果”：不会，结果来自输入 Map 或显式 updateState。
- “interruptBefore 会执行一部分 review”：不会，Action 尚未调用。

## 自检问题

1. 为什么首次 `blockLast().node()` 是 generate？
2. 怎样准确判断当前等待哪个节点？
3. 中断之前为什么必须先保存上一节点 Checkpoint？
4. 恢复时框架怎样避免再次命中同一个 interruptBefore？
5. 本章输入 Map 与下一章 `addStateUpdate` 有什么不同？

## 课后 Test 练习

创建：

```text
src/test/java/cyou/yuanbaomao/graphlearning/lesson/interrupt/InterruptPracticeTest.java
```

使用 `InterruptWorkflow` 覆盖拒绝路径：

- 首次运行后断言最后可见输出节点为 `GENERATE`。
- 断言最新快照的 `next()` 为 `REVIEW`。
- 使用同一图、同一 threadId 和 `resume()`。
- 恢复输入传入 `approved=false`。
- 断言最终输出 `isEND()` 为 true。
- 断言最终 `result="rejected"`。
- 严格断言轨迹为 `GENERATE, REVIEW, FINISH`，证明审核节点恢复后实际执行。

运行：

```powershell
mvn -pl sellersprite-graph-learning -Dtest=InterruptPracticeTest test
```

完成后找 Codex 检查，并同时指出“最后完成节点”和“等待执行节点”。

下一课：[动态中断与反馈元数据](06-dynamic-interrupt.md)。

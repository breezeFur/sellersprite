# 04 Checkpoint：保存执行位置并从失败处恢复

## 学习目标

学完本章后，你应该能够：

- 说明 Checkpoint 保存的不只是状态，还包括当前节点和下一节点。
- 正确设计 `threadId`，避免不同运行共享历史。
- 解释失败节点为什么会从上一成功节点的快照恢复。
- 区分“继承旧状态后从 START 重跑”和“真正从断点恢复”。
- 说清 `MemorySaver` 的能力边界以及生产环境为什么需要持久化 Saver。

## 先建立心智模型

把 Checkpoint 想成一张书签：

```text
Checkpoint = 当前状态 + 刚完成的节点 + 下一节点 + 快照 ID
```

例如：

```text
nodeId     = prepare
nextNodeId = unstableTask
state      = {input=hello, trace=[prepare]}
```

它表达：“`prepare` 已完成；恢复时不要再做 prepare，从 `unstableTask` 继续。”

## 示例执行过程

`CheckpointWorkflow`：

```text
START -> prepare -> unstableTask -> finish -> END
```

`unstableTask` 第一次调用故意失败，第二次成功。理想路径是：

```text
首次：START -> prepare -> unstableTask 抛错
恢复：unstableTask -> finish -> END
```

最终轨迹中 `prepare` 只出现一次。

## 代码拆解

### 第一步：同一张图持有同一个 Saver

```java
MemorySaver saver = new MemorySaver();
```

Saver 实例是快照实际存放的位置。恢复时如果重新 `build()` 一张图，就会创建新的 `MemorySaver`，旧快照自然找不到。

### 第二步：编译时注册 Saver

```java
return graph.compile(CompileConfig.builder()
        .saverConfig(SaverConfig.builder().register(saver).build())
        .releaseThread(false)
        .build());
```

`releaseThread(false)` 表示流程结束后仍保留该线程的 Checkpoint。课程需要在结束后查看历史，所以不能自动释放。

当前 `StateGraph.compile()` 的默认配置本身也会注册 `MemorySaver`，但课程显式写出配置，是为了让你看见 Saver 的生命周期与策略。

### 第三步：用 threadId 标识一次逻辑运行

```java
RunnableConfig config = RunnableConfig.builder()
        .threadId("checkpoint-thread-1")
        .build();
```

`threadId` 不是 Java 线程名，而是 Graph 运行身份。同一个任务恢复必须使用同一值；不同任务必须使用不同值。

### 第四步：制造一次失败

```java
private static Map<String, Object> unstableTask(
        OverAllState state, AtomicInteger attempts) {
    if (attempts.incrementAndGet() == 1) {
        throw new IllegalStateException("模拟一次性任务失败");
    }
    return Map.of(GraphStateKeys.TRACE, List.of(UNSTABLE_TASK));
}
```

失败发生在节点返回 delta 之前，因此本次没有新状态可合并，也不会产生“unstableTask 已完成”的 Checkpoint。

### 第五步：标记为恢复执行

```java
RunnableConfig resumeConfig = RunnableConfig.builder(initialConfig)
        .resume()
        .build();
```

`resume()` 不保存状态，也不把快照塞进配置。它只加入恢复元数据，让运行时进入恢复分支；真正的状态仍由同一个 Saver 根据 `threadId` 找到。

## 框架源码下钻

以下结论来自 `spring-ai-alibaba-graph-core 2.0.0-M1.1`。

### Checkpoint 的四个核心字段

源码中的 `Checkpoint` 保存：

```text
id, state, nodeId, nextNodeId
```

新快照 ID 使用 UUID。`StateSnapshot.next()` 实际来自 Checkpoint 的 `nextNodeId`，所以它是判断暂停点和恢复位置的可靠入口。

### 快照在什么时候写入

执行器会在 `START` 和每个成功节点之后：

1. 合并节点状态增量。
2. 计算下一节点。
3. 创建包含 `nodeId/state/nextNodeId` 的 Checkpoint。
4. 产生本节点输出。

失败节点没有完成第 1–4 步，因此恢复的依据是“上一个成功快照的 nextNodeId 指向失败节点”，不是“异常发生后保存了一份失败快照”。

`END` 自身不需要再产生下一步快照；最后一个业务节点的快照通常已经指向 `END`。

### MemorySaver 怎样组织数据

内部结构近似：

```java
Map<String, LinkedList<Checkpoint>> checkpointsByThread;
```

- 一级 key 是 `threadId`。
- 没设置 `threadId` 时使用全局 `"$default"`。
- 新快照 `push` 到链表头部，因此历史顺序是最新在前。
- 配置有 `checkPointId` 时精确查找，没有时返回当前线程最新快照。
- 一个 `ReentrantLock` 保护整个内存 Saver。

因此课程练习必须使用唯一 threadId。多个运行都不传 ID，会意外共享 `$default`。

### 运行时怎样选择恢复分支

`GraphRunnerContext` 在配置含恢复元数据或 Checkpoint ID 时调用 `initializeFromResume`：

```text
saver.get(config)
  -> nextNodeId = checkpoint.nextNodeId
  -> overallState 合并 checkpoint.state
  -> currentNodeId = null
  -> 继续主执行器
```

`currentNodeId=null` 表示当前不是从 START 新开，而是准备执行快照记录的下一节点。

### 一个容易混淆的版本行为

相同 threadId 已有历史时，即使新调用没有 `resume()`，`getInitialState()` 也可能读取最新状态并与本次输入合并，但执行位置仍从 `START` 开始。这是“带旧状态重跑”，不是真恢复。

要恢复位置，必须明确使用恢复配置或快照配置。

## MemorySaver 为什么不能直接用于生产

- JVM 重启后全部丢失。
- 重建图或重建 Saver 实例后旧数据不可见。
- 无法让另一个服务实例接管任务。
- 没有业务级保留、清理和审计策略。

生产市场调研图使用 `MysqlSaver`，公共职责相同，但数据可以跨线程、请求和进程存在。

无论 Saver 存在哪里，外部副作用仍要幂等。Checkpoint 不会撤销已经发送的消息、HTTP 请求或文件写入。

## 自检问题

1. `nodeId=prepare, nextNodeId=unstableTask` 分别代表什么？
2. 为什么失败后最新快照不是 `unstableTask`？
3. `resume()` 自己保存了哪些状态？
4. 为什么恢复时不能重新构建图？
5. 同 threadId 不加 resume 再执行，与真正恢复有什么区别？

## 课后 Test 练习

创建：

```text
src/test/java/cyou/yuanbaomao/graphlearning/lesson/checkpoint/CheckpointPracticeTest.java
```

使用 `CheckpointWorkflow` 完成：

- 首次执行断言抛出“模拟一次性任务失败”。
- 读取最新 `StateSnapshot`，断言 `node()` 为 `PREPARE`、`next()` 为 `UNSTABLE_TASK`。
- 断言失败后的快照轨迹只有 `PREPARE`。
- 使用同一图、同一 threadId 和 `resume()` 恢复。
- 断言最终 `result="completed"`。
- 严格断言最终轨迹为 `PREPARE, UNSTABLE_TASK, FINISH`，证明 prepare 没有重跑。
- 断言不稳定节点总尝试次数为 2。

每次测试使用不同且有意义的 threadId，避免历史相互污染。

运行：

```powershell
mvn -pl sellersprite-graph-learning -Dtest=CheckpointPracticeTest test
```

完成后找 Codex 检查，并用快照的 `node/next` 解释恢复位置。

下一课：[编译期中断与人工恢复](05-interrupt-resume.md)。

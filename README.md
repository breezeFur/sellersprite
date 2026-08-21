# sellersprite-service

SellerSprite Open API 全栈集成服务。项目已同步 `D:\develop\yuanbaomao-scaffold-vendored` 的 0.2.0 脚手架能力，后端基于 JDK 21、Spring Boot 4.1、Spring AI 2.0 和 Maven 多模块，前端基于 Vue 3、TypeScript、Vite 与 Element Plus。

项目通过内部 `/api/sellersprite/**` 代理封装卖家精灵当前公开的 44 个业务接口和 1 个次数查询接口，并提供带权限控制的 API 调试工作台；AI 聊天同时注册 5 个只读 SellerSprite `@Tool`。

## 主要能力

- 统一响应、异常处理、分页、MDC `traceId`、UUIDv7、MyBatis-Plus 审计字段和操作日志。
- 内存访问令牌、HttpOnly 刷新 Cookie、令牌轮换与复用检测，以及功能/接口两级 RBAC。
- 用户、部门、角色、菜单、接口资源、字典、缓存、登录日志、操作日志和 AI 调用日志管理。
- AI 多轮对话、SSE 流式输出、JDBC Chat Memory、会话设置、失败重试和 Prompt 审计。
- 九个业务域、45 个固定 SellerSprite 操作的强类型 Service、Controller 和 Vue 调试工作台。
- 5 个只读 AI 工具：次数查询、ASIN 详情、产品研究、关键词研究和市场研究。
- 市场调研 v5 三阶段 Graph、Top20 人工选择关卡、七加三证据展示、Curation 流式分析和五文件交付。

## 模块说明

| 模块 | 职责 |
| --- | --- |
| `sellersprite-common` | 公共框架适配、统一结果、鉴权拦截、数据库实体、Mapper 与 DAO。 |
| `sellersprite-system` | 认证、仪表盘、用户、部门、角色、权限、字典和运维管理。 |
| `sellersprite-api` | SellerSprite Client、契约模型、九域 Service 与 45 个内部代理端点。 |
| `sellersprite-ai` | AI 聊天、会话、记忆、Prompt 审计、SellerSprite `@Tool` 和市场调研 Curation Agent。 |
| `sellersprite-research` | 市场调研业务域、Mock/Remote 数据源、不可变证据、Excel、分析运行和持久化事件流。 |
| `sellersprite-research-graph` | Spring AI Alibaba Graph 固定工作流、数据库 Dispatcher、执行租约和 checkpoint 恢复。 |
| `sellersprite-mcp-server` | 独立 SellerSprite API MCP 服务，以 Streamable HTTP 或 stdio 暴露当前 45 个 API 工具。 |
| `sellersprite-server` | Spring Boot 启动入口和模块装配。 |
| `sellersprite-web` | Vue 管理台、动态菜单、权限控制、AI 聊天、SellerSprite 调试台和市场调研对话工作区。 |

后端入口为 `sellersprite-server/src/main/java/cyou/yuanbaomao/sellersprite/server/SellerSpriteServiceApplication.java`，前端入口为 `sellersprite-web/src/main.ts`。

## 运行前提

- JDK 21、Maven 3.9+
- Node.js 22.12+、npm
- MySQL 8.x
- 元宝猫基础组件已内置在项目 Maven 文件仓库中，无需预先安装到个人 Maven 本地仓库
- 使用 AI 时提供 OpenAI 兼容模型密钥
- 实际调用卖家精灵上游时提供 `SELLERSPRITE_API_SECRET_KEY`

默认使用本地缓存；切换 Redis 时按 `ybm.cache` 与 Spring Data Redis 标准配置补充连接信息。

## SellerSprite API MCP 服务

`sellersprite-mcp-server` 将 `sellersprite-api` 当前登记的 45 个 SellerSprite 操作逐一映射为 MCP 工具，工具名统一使用 `sellersprite_` 前缀。它只复用现有 API Service、鉴权、字典转换和错误处理，不改变原有 REST 代理与市场调研工作流。

Streamable HTTP 默认监听 8128 端口：

```powershell
$env:SPRING_PROFILES_ACTIVE = "streamable,local"
$env:SELLERSPRITE_API_SECRET_KEY = "<your-secret-key>"
.\mvnw.cmd -pl sellersprite-mcp-server spring-boot:run
```

stdio 模式：

```powershell
$env:SPRING_PROFILES_ACTIVE = "stdio,local"
$env:SELLERSPRITE_API_SECRET_KEY = "<your-secret-key>"
.\mvnw.cmd -pl sellersprite-mcp-server spring-boot:run
```

运行 MCP 服务仍需要当前项目的数据库字典配置，因为 API Client 会把稳定字典标签转换为 SellerSprite 官方参数值；密钥和数据库连接应通过本地 profile 或环境变量注入，不要写入 Git 跟踪文件。

## 数据库初始化与升级

### 全新数据库

执行完整结构脚本：

```bash
mysql -uroot -p -e "source sql/schema.sql"
```

默认数据库名为 `sellersprite_service`。脚本会创建初始管理员 `admin / 123456`，首次登录后必须立即修改密码。
脚本同时初始化 SellerSprite 接口所需的站点、尺寸、排序、流量类型和重量单位等系统字典。

> 警告：不要对已有数据库重复执行 `schema.sql`。它会把 `admin` 密码重置为 `123456`。

### 已有数据库

项目保留最终结构脚本和必要的增量迁移。已有数据库升级前必须先备份。升级到 0.2.0 前，按以下顺序执行迁移，再启动新版应用：

1. `sql/migrations/20260815_rename_track_id_to_trace_id.sql`：重命名链路标识列和索引，不修改已有数据。
2. `sql/migrations/20260815_seed_sellersprite_dictionaries.sql`：补充 SellerSprite 系统字典，可重复执行。
3. `sql/migrations/20260815_add_market_research_dataset_lookup_index.sql`：补充市场调研数据集查询索引，可重复执行。

其他结构差异应在维护窗口内对照 `sql/schema.sql` 手工生成差异 SQL，不要直接重复执行整份脚本。

市场调研由 Spring AI Alibaba Graph 编排，官方 `GRAPH_THREAD`、`GRAPH_CHECKPOINT` 表负责控制流恢复。业务表中，`market_research_job`、`market_research_node_execution`、`market_research_dataset` 和 `market_research_artifact` 保存数据任务、节点审计、不可变证据和附件；`market_research_analysis_run` 与 `market_research_event` 保存独立的 Curation 分析生命周期和可重放事件。系统不再依赖 Spring Batch 元数据表。

开发阶段重建数据库时可直接重新创建 `sellersprite_service` 后执行最新 `sql/schema.sql`；脚本包含当前人工关卡、分析运行和事件流锁所需的最终结构。

应用启动后，管理员在“接口资源”页面依次执行“同步接口目录”和“同步菜单接口绑定”，即可按前端固定清单关联工作台的 45 个代理端点与市场调研端点。

## 本地配置

`config/application.yml` 只保存非敏感默认值，默认激活 `local` profile。将数据库、模型和 SellerSprite 密钥放入不提交 Git 的 `config/application-local.yml` 或环境变量：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/sellersprite_service?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: ${DB_PASSWORD}
  ai:
    model:
      chat: openai
    openai:
      api-key: ${AI_OPENAI_API_KEY}
      base-url: ${AI_OPENAI_BASE_URL:https://api.openai.com}
      chat:
        completions-path: ${AI_OPENAI_CHAT_COMPLETIONS_PATH:/v1/chat/completions}
        model: ${AI_CHAT_MODEL:gpt-5.5}

sellersprite:
  api:
    secret-key: ${SELLERSPRITE_API_SECRET_KEY}
```

不要将密码、访问令牌或密钥写入 Git 跟踪文件。未配置卖家精灵密钥时，内部代理和 AI 工具会返回明确的 `SELLERSPRITE_NOT_CONFIGURED`，不会构造伪成功结果。

## 市场调研 v6 缓存与人工关卡 Graph

现行工作流版本为 `market-research-v6-cache-insights`。父 Graph 固定编排 `screeningGraph -> productSelectionGate -> deepDiveGraph -> finalAnalysisGraph -> finalizeArtifacts`，其中三个业务阶段都是可独立恢复的子 Graph。阶段一完成后，父 Graph 通过 `interruptBefore` 停在商品选择关卡，任务进入 `WAITING_INPUT` 并释放执行租约；提交选择后任务重新排队，用新 execution token 显式更新 checkpoint 状态，再按 ENTER 或 ABANDON 条件边继续。

阶段一只采集商品、市场销售趋势、需求趋势和细分市场数据，形成 `US`、`行业销售趋势`、`行业需求及趋势`、`细分市场现状`、`细分市场退货率`、`竞品品牌`、`商品集中度` 七张证据表。每张表的确定性统计、AI 简析和阶段一总结都会持久化并通过 SSE 推送；Top20 商品按阶段一 `evidence.products` 的默认顺序固化为不可变候选，用户可选择一个或多个 ASIN 进入阶段二，也可直接放弃市场。

阶段二针对选中 ASIN 采集评论、关键词、销量趋势和 Keepa 经营趋势，形成 `评价`、`VOC`、`Keywords`、`ASIN销售趋势`、`ASIN运营趋势` 五张证据表并输出逐表分析和阶段总结。Keywords 用于判断宣传获客成本、竞争强度和投放难度，不会在缺少证据时虚构预算、ACOS 或 ROI。阶段三只读取已持久化的十二张证据表及前两阶段结论，生成一份最终 Markdown，不再次采集或逐表重跑模型。

最终报告采用破坏性的十二章新契约：按上述十二张证据表固定顺序逐章输出章节评分、置信度、核心结论、主要风险和决策建议，不再附带 Sheet 行数、会话信息或重复数据表。阶段一报告同样直接使用七张证据表的真实表名，不显示“Sheet 一”等内部编号。完整数字留在证据数据模块；行业销售趋势以销量折线图展示，需求趋势和所选竞品 ASIN 高频关键词也由服务端从持久化证据确定性计算。报告 Agent 在生成对应章节时调用 `generateResearchReportChart` 工具，把工具返回的 Mermaid Markdown 原样写入同一份 `summary_delta/summary` 在线报告，通用 Markdown 组件直接渲染为图；不再由执行器预先推送独立 `report_chart` 事件。断线客户端仍按 `sequenceNo` 恢复同一份报告增量，PDF 使用同一图表规格生成静态快照，AI 只解释图表，不生成或修改图表数值。

SellerSprite 接口入参定义在 `sellersprite-api` 各业务包的 `model.dto`，响应字段定义在 `model.vo`；证据表并不是 DTO/VO，而由 `ResearchEvidenceCatalog` 固定中文表头、`ResearchEvidenceService` 完成原始字段映射和确定性派生。正式事实表只接纳可追溯原始字段及可复算的份额、差值、环比、累计占比和波动率；AI 用户画像、场景、动机和机会判断继续保存在逐表分析与阶段总结中，不伪装成原始事实列。

跨任务 SellerSprite 响应以 operation 和完整有效请求哈希写入 Redis，键前缀为 `sellersprite:research:source:v1:`。历史类目统计不设置过期时间，当前月市场数据和 ASIN 趋势默认缓存 24 小时；创建页的产品类目树与搜索结果默认缓存 7 天，并在过期后的首次读取时懒刷新。应用启动后会按活跃 REMOTE 任务去重检查过去 24 个完整月份的市场销售趋势缓存，只串行回源缺失月份，检查结束前暂缓 Graph Dispatcher 抢占任务。`sellersprite.research.source-cache.enabled=false` 时完全绕过 Redis；Redis 故障也只按缓存 miss 处理并直接回源。

运行阶段以数据库持久化和页面展示为主：SSE 发送状态、增量结论、有限预览和数据集引用，完整表格通过分页 REST 查询，运行中不依赖 Excel。用户选择 ENTER 后，终态节点一次性发布阶段一原始 Excel、阶段一八 Sheet 市场初筛 Excel、阶段二原始 Excel、阶段二五 Sheet Excel 和最终 Markdown，共恰好 5 个文件；ABANDON 只发布阶段一的 2 个 Excel，并以 `ABANDONED` 正常结束。

仓库配置默认使用 `REMOTE`。本地可以切换为 `MOCK` 验证数据 Graph；启用阶段 AI 仍需要在本地非跟踪配置或环境变量中提供 OpenAI 兼容模型配置。Mock 只用于调研工作流，不新增或替换现有 `/api/sellersprite/**` 接口。

创建任务示例：

```http
POST /api/market-research/jobs
Content-Type: application/json

{
  "reportName": "美容仪美国站市场调研",
  "marketplace": "US",
  "nodeIdPath": "172282:281407",
  "month": "2026-07",
  "keyword": "facial cleansing device",
  "seedAsins": ["B0MOCK0001"],
  "analysisGoal": "判断市场进入机会、竞争强度和退货风险",
  "collectionConfig": {
    "collectMarketSalesTrend": {
      "monthCount": 12
    },
    "collectReviews": {
      "pagination": {
        "startPage": 1,
        "pageSize": 10,
        "targetCountPerAsin": 20
      }
    }
  }
}
```

`collectionConfig` 是任务级 JSON，但内部按采集节点 ID 使用强类型配置，参数边界就是现有 SellerSprite 外部接口请求 DTO。阶段一消费商品、销售趋势、需求趋势和细分市场配置；阶段二消费评论和关键词情报配置。任务根字段 `marketplace`、`month`、`keyword`、`nodeIdPath` 及编排分页字段会在调用前覆盖 DTO 副本，阶段二的 `selectedAsins` 再覆盖评论和流量词 DTO，并进入数据集 `requestHash`。未显式填写的节点参数使用后端默认值，其中销售趋势为 12 个月，评论为每个选中 ASIN 20 条，上限也是 20 条。

任务、Excel 和拓扑接口：

- `GET /api/market-research/jobs/{jobId}`
- `GET /api/market-research/jobs/{jobId}/nodes`
- `POST /api/market-research/jobs/{jobId}/cancel`
- `POST /api/market-research/jobs/{jobId}/retry`
- `GET /api/market-research/jobs/{jobId}/artifacts/{artifactId}/download`
- `GET /api/market-research/workflow`
- `GET /api/market-research/jobs/{jobId}/product-selection`
- `POST /api/market-research/jobs/{jobId}/product-selection`
- `GET /api/market-research/jobs/{jobId}/evidence?stageCode={SCREENING|DEEP_DIVE}`
- `GET /api/market-research/jobs/{jobId}/evidence/{datasetCode}?current=1&size=50`

事件、分析和追问接口：

- `GET /api/market-research/jobs/{jobId}/stream?afterSequence={sequence}`
- `GET /api/market-research/jobs/{jobId}/analyses`
- `GET /api/market-research/jobs/{jobId}/analysis`
- `POST /api/market-research/analysis-runs/{analysisRunId}/retry`
- `POST /api/market-research/analysis-runs/{analysisRunId}/cancel`
- `POST /api/market-research/jobs/{jobId}/messages`

执行市场调研管理台权限迁移后，管理台通过 `/research/market-report` 提供强类型采集参数表单、后端 Graph 拓扑、阶段一七表与总结、Top20 多选关卡、阶段二三表、流式 AI 结论和终态下载。创建成功的 `jobId` 会保留在页面 query 中；刷新时页面建立任务 SSE，首个 `snapshot` 聚合帧恢复任务详情、节点状态、产物和持久化事件，后续 `events` 帧从最后一个 `sequenceNo` 续接。页面根据后端 `allowedActions` 和阶段状态渲染，不再推断固定的采集/证据/报告三元组。

切换远端前，先完成上面的市场调研相关数据库迁移，并在本地配置中启用 `REMOTE`：

```yaml
sellersprite:
  research:
    source-mode: REMOTE
    dispatcher-enabled: true
    dispatch-batch-size: 20
    poll-interval-ms: 2000
    max-attempts: 3
    lease-duration-ms: 60000
    heartbeat-interval-ms: 15000
    retry-base-delay-ms: 5000
    retry-max-delay-ms: 300000
    remote-enrichment-asin-limit: 5
    analysis:
      enabled: true
      dispatcher-enabled: true
      poll-interval-ms: 2000
      dispatch-batch-size: 10
      lease-duration-ms: 120000
      heartbeat-interval-ms: 15000
      max-sheets: 10
      max-model-calls: 16
      max-model-input-tokens: 12000
      max-execution-duration-ms: 0
    event-stream:
      timeout-ms: 1800000
      heartbeat-interval-ms: 15000
      replay-batch-size: 500
      live-batch-window-ms: 20
      outbound-queue-capacity: 1024
    checkpoint-initialize-schema: false
    output-directory: ./data/market-research
```

`checkpoint-initialize-schema` 在生产环境保持 `false`，表结构由唯一入口 `schema.sql` 管理；仅隔离测试环境可显式开启。父 Graph 的 thread ID 由工作流版本和 `jobId` 组成。任务 Dispatcher 通过原子抢占、execution token、租约和心跳支持多实例；应用重启或租约过期后从父 Graph checkpoint 恢复。不可变数据集通过请求摘要和 SHA-256 避免重复调用已经完成的采集节点，短暂错误按有界指数退避进入 `RETRY_WAIT`。

`SCREENING`、`DEEP_DIVE` 和 `FINAL_ANALYSIS` 使用三个独立 analysis run，但共享同一个 `conversationId`。阶段运行使用父任务 execution token 做归属校验；后续追问和失败分析重试仍使用独立分析运行、租约与心跳，运行中的分析可协作式取消。

每次分析执行尝试受 `max-sheets` 和 `max-model-calls` 限制；上下文压缩也计入真实模型调用。`max-execution-duration-ms` 默认为 `0`，表示不限制分析总执行时长；配置为正数时，时长预算会在模型、事件和报告边界协作检查，不会强制中断底层 HTTP 调用，单次请求仍由 `spring.ai.openai.timeout` 兜底。同一分析运行的自动重试次数由 `max-attempts` 限制，数据库中的模型调用数和事件数跨尝试累计，不会在重试时清零。超限会记录明确错误码并结束该次运行，不会生成降级成功报告。

除心跳外，Graph、Agent 增量、选择关卡、报告附件和统一终态事件都先写入 `market_research_event`。阶段 AI 复用 `sheet`、`sheet_think_delta`、`sheet_think`、`summary_delta`、`summary`，并在 payload 中带 `stageCode` 和可用的 `datasetCode`；关卡与阶段事件为 `product_selection_required`、`product_selection_submitted`、`stage_completed`、`market_abandoned`。事件只承载列信息、行数、有限 preview 和数据集引用，不把完整大表塞进 SSE。

同一任务在落库前通过独立锁行串行化；`ResearchSseEventPublisher` 在事务内登记按序提交票据，并在 `afterCommit` 中确认票据，避免并发事务的提交回调逆序通知活动连接。确认后 Publisher 只把事件加入当前连接的有界内存队列；虚拟写线程按 `live-batch-window-ms` 短窗口聚合并写入 `SseEmitter`。服务端支持 `afterSequence` 与 `Last-Event-ID`；连接建立时先发送 `snapshot` 或持久化 `events` 聚合帧追赶，随后只接收主动推送。前端使用 Bearer fetch-SSE 鉴权、按序号去重和断线重连。当前主动推送 Hub 是单 JVM 内存组件，多实例部署前必须接入 Redis Pub/Sub 或消息总线做提交后跨实例广播。

远端模式通过现有 `AccountService`、`ProductService`、`MarketService`、`AsinService`、`KeywordService`、`TrafficService` 和 `ReviewService` 采集数据，复用统一认证、超时和异常转换。市场商品节点会依次采集产品、选市场、市场统计、需求趋势、上架时间与趋势、价格分布、商品/品牌/卖家集中度，以及受限种子 ASIN 的详情和销量趋势；关键词节点会采集关键词研究、趋势、挖掘和受限种子 ASIN 的流量词。配额响应也会完整落入不可变数据集，不再只做非空检查。

阶段一市场初筛 Excel 必须且只能包含上述七张证据表和一张 `阶段一总结`，共 8 个 Sheet；阶段二 Excel 必须且只能包含 `评价`、`VOC`、`Keywords`，共 3 个 Sheet。每页只包含稳定业务字段，不写“证据范围”、来源、局限、状态或 `原始.*` 列。发布前会重新打开文件并校验 Sheet 数、顺序和表头。Curation 和页面查询都读取同一批持久化证据，不从 Excel 反向解析数据。

采集子图按 `collectionConfig` 的起始页、页大小和目标数量自动翻页。默认采集商品 100 条、细分市场 50 条、每个选中 ASIN 评论 20 条，销售趋势回溯 12 个月；ASIN 详情、销量趋势和流量词扩展数量可在对应节点参数中覆盖。原始数据 Excel 按阶段内数据集生成多 Sheet，表头直接使用外部响应的顶层字段名。五类终态产物分别为 `STAGE1_RAW_WORKBOOK`、`STAGE1_EVIDENCE_WORKBOOK`、`STAGE2_RAW_WORKBOOK`、`STAGE2_EVIDENCE_WORKBOOK` 和 `AI_ANALYSIS_REPORT`，默认保存到 `./data/market-research/{jobId}/`，数据库只记录受控存储键、文件大小和 SHA-256。

人工联调建议先使用管理员账号。普通角色需要管理员执行“同步接口目录”和“同步菜单接口绑定”，再为该角色授权市场调研菜单，否则接口权限拦截器会返回无权访问。Remote 复用 `sellersprite.api.base-url` 和 `SELLERSPRITE_API_SECRET_KEY`；若实际网关与默认值不同，在本地配置或环境变量中覆盖后再测试。

SellerSprite 常用环境变量：

| 环境变量 | 默认值 | 说明 |
| --- | --- | --- |
| `SELLERSPRITE_API_ENABLED` | `true` | 是否启用上游调用。 |
| `SELLERSPRITE_API_BASE_URL` | `https://api.sellersprite.com` | 上游网关，只应在受控环境覆盖。 |
| `SELLERSPRITE_API_SECRET_KEY` | 无 | 官方 `secret-key`，禁止提交。 |
| `SELLERSPRITE_API_CONNECT_TIMEOUT` | `20s` | 建立连接超时。 |
| `SELLERSPRITE_API_READ_TIMEOUT` | `60s` | 读取响应超时。 |
| `SELLERSPRITE_RESEARCH_REMOTE_ENRICHMENT_ASIN_LIMIT` | `5` | 单任务执行 ASIN 详情、销量趋势和流量词扩展的最大去重种子数，范围 0-20。 |
| `SELLERSPRITE_RESEARCH_ANALYSIS_ENABLED` | `true` | 是否启用读取持久化证据的 Curation 分析阶段。 |
| `SELLERSPRITE_RESEARCH_ANALYSIS_DISPATCHER_ENABLED` | `true` | 是否启用数据库轮询型分析 Dispatcher。 |
| `SELLERSPRITE_RESEARCH_ANALYSIS_MAX_SHEETS` | `10` | 每次分析执行尝试允许处理的最大 Sheet 数。 |
| `SELLERSPRITE_RESEARCH_ANALYSIS_MAX_MODEL_CALLS` | `16` | 每次分析执行尝试允许的真实模型调用上限，包含上下文压缩。 |
| `SELLERSPRITE_RESEARCH_ANALYSIS_MAX_EXECUTION_DURATION_MS` | `0` | 每次分析执行尝试的协作式时长预算，单位毫秒；`0` 表示不限制。 |
| `SELLERSPRITE_RESEARCH_EVENT_STREAM_REPLAY_BATCH_SIZE` | `500` | SSE 每批回放的最大持久化事件数。 |

## 启动

启动后端：

```bash
mvn -pl sellersprite-server -am spring-boot:run -Dspring-boot.run.arguments="--spring.config.additional-location=optional:file:../config/"
```

命令会加载项目根目录下的 `config/application.yml` 及对应 profile 配置；请先按“本地配置”章节准备 `config/application-local.yml` 或所需环境变量。

启动前端：

```bash
cd sellersprite-web
npm install
npm run dev
```

默认地址：

- 前端：`http://localhost:5173`
- 后端：`http://localhost:8092`
- 健康检查：`http://localhost:8092/actuator/health`

Vite 默认将 `/api` 代理到 `http://localhost:8092`，可通过 `SELLERSPRITE_API_TARGET` 覆盖。浏览器只调用内部固定代理路径，不接收上游 URL 或 `secret-key`。

## Docker 部署

先在项目根目录手动打包后端可执行 JAR：

```bash
./mvnw -B -ntp -pl sellersprite-server -am clean package -DskipTests
```

当前产物为 `sellersprite-server/target/sellersprite-server-0.2.0.jar`。项目根目录的 `Dockerfile` 使用 Eclipse Temurin 21 JRE Alpine 镜像，并直接复制该产物构建运行镜像：

```bash
docker build -t sellersprite-server:latest .
```

宿主机的配置目录挂载到容器 `/app/config`，日志目录挂载到 `/app/logs`：

```bash
docker run -d \
  --name sellersprite-server \
  --restart unless-stopped \
  --memory=1g \
  -p 8092:8092 \
  -v /opt/sellersprite/config:/app/config:ro \
  -v /opt/sellersprite/logs:/app/logs \
  -e SPRING_PROFILES_ACTIVE=server \
  sellersprite-server:latest
```

`/opt/sellersprite/config` 应包含完整的部署配置，例如 `application.yml` 和 `application-server.yml`。生产环境的数据源、Redis、模型密钥和 SellerSprite API 密钥仍应通过环境变量或配置中心注入。

## 鉴权与权限同步

登录接口 `POST /api/auth/login` 返回访问令牌，并设置 HttpOnly 刷新 Cookie。访问令牌只保存在前端内存中，请求受保护接口时发送：

```http
Authorization: Bearer <access-token>
```

访问令牌固定有效两天，刷新令牌固定有效三十天；刷新 Cookie 名称为 `${spring.application.name}_refresh_token`。登录、刷新和退出是静态公共端点，其余 `/api/**` 默认需要有效会话，并按数据库中的功能、接口和角色授权校验。

## SellerSprite API 工作台

工作台路由为 `/sellersprite/workbench`，功能码为 `sellersprite.workbench`，权限码为 `sellersprite:workbench:view`。页面提供：

- 九域与 45 个固定操作的检索、选择和示例参数
- GET Query、JSON Body、multipart 数组和显式文件字段转换
- 请求防重复提交、参数错误、业务错误码、`traceId`、耗时和完成时间
- 成功、空响应、失败状态和格式化 JSON 复制

完整请求/响应模型以 Controller、DTO/VO 和 `docs/sellersprite-api-contract.json` 为准。外部 Client 统一发送官方要求的 `secret-key`、`Content-Type` 和唯一 `x-request-id`；不会臆造 HMAC、摘要或响应验签。

契约生成命令：

```bash
python tools/generate_sellersprite_contracts.py
python tools/generate_sellersprite_endpoints.py
```

## Spring AI 工具

`SellerSpriteAiTools` 通过 Spring AI `@Tool` 暴露以下只读工具，并在同步聊天、流式聊天和消息重试入口注册同一实例：

| 工具名 | 能力 |
| --- | --- |
| `sellersprite_get_account_visits` | 查询账户剩余调用次数。 |
| `sellersprite_get_asin_detail` | 查询 ASIN 详情。 |
| `sellersprite_research_products` | 执行产品研究。 |
| `sellersprite_research_keywords` | 执行关键词研究。 |
| `sellersprite_research_markets` | 执行市场研究。 |

工具只接收业务参数并委派既有 Service，不暴露上游 URL、请求头或密钥。工具异常沿用 SellerSprite 业务错误，不降级为虚假数据；Advisor 日志会隐藏工具参数正文。

## 构建与验证

后端全量测试并完成打包：

```bash
./mvnw clean verify --no-transfer-progress
```

Windows 下可执行完整隔离验证。脚本会使用空 Maven 本地仓库和无认证 settings，确认构建没有借用当前电脑已安装的 `cyou.yuanbaomao` 构件：

```powershell
.\scripts\verify-vendored-build.ps1
```

前端门禁：

```bash
cd sellersprite-web
npm run lint
npm run test:unit
npm run build
npm run test:e2e
```

## 内置 Maven 文件仓库

`maven-repository` 使用标准 Maven 2 布局，只收录当前应用实际依赖的私有二进制闭包：

- `yuanbaomao-base:0.2.0`
- `yuanbaomao-web-starter:0.2.0`
- `yuanbaomao-mybatis-starter:0.2.0`
- `yuanbaomao-cache-starter:0.2.0`
- `yuanbaomao-log-starter:0.2.0`
- `yuanbaomao-dict-starter:0.2.0`

根 `pom.xml` 不再继承私有 `yuanbaomao-scaffold-parent`，而是通过 `file://${maven.multiModuleProjectDirectory}/maven-repository` 解析上述构件。第三方开源依赖仍从 Maven Central 或团队镜像下载，因此该方案不是完全离线构建。

仓库目录只在构建时充当依赖来源，不是应用资源。`sellersprite-server` 完成 Spring Boot `repackage` 后，六个运行依赖会进入可执行 JAR 的 `BOOT-INF/lib`，POM、校验文件和整个 `maven-repository` 目录不会被打包。

升级内置组件时必须同步替换主 JAR及发布生成的扁平 POM，重新生成 `.sha1`、`.md5` 与 `SHA256SUMS`，更新根 POM 的 `yuanbaomao-*.version`，最后执行 `scripts/verify-vendored-build.ps1`。不要复制 `.lastUpdated`、`_remote.repositories`、sources 或 javadoc 文件。

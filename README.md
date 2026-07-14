# sellersprite-service

SellerSprite Open API 全栈集成服务。项目已同步 `D:\develop\scaffold` 当前脚手架能力，后端基于 JDK 21、Spring Boot 4.1、Spring AI 2.0 和 Maven 多模块，前端基于 Vue 3、TypeScript、Vite 与 Element Plus。

项目通过内部 `/api/sellersprite/**` 代理封装卖家精灵当前公开的 44 个业务接口和 1 个次数查询接口，并提供带权限控制的 API 调试工作台；AI 聊天同时注册 5 个只读 SellerSprite `@Tool`。

## 主要能力

- 统一响应、异常处理、分页、MDC `trackId`、UUIDv7、MyBatis-Plus 审计字段和操作日志。
- 内存访问令牌、HttpOnly 刷新 Cookie、令牌轮换与复用检测，以及功能/接口两级 RBAC。
- 用户、部门、角色、菜单、接口资源、字典、缓存、登录日志、操作日志和 AI 调用日志管理。
- AI 多轮对话、SSE 流式输出、JDBC Chat Memory、会话设置、失败重试和 Prompt 审计。
- 九个业务域、45 个固定 SellerSprite 操作的强类型 Service、Controller 和 Vue 调试工作台。
- 5 个只读 AI 工具：次数查询、ASIN 详情、产品研究、关键词研究和市场研究。
- 固定 US 站市场调研 Batch、八阶段进度跟踪和 Excel 报告管理台。

## 模块说明

| 模块 | 职责 |
| --- | --- |
| `sellersprite-common` | 公共框架适配、统一结果、鉴权拦截、数据库实体、Mapper 与 DAO。 |
| `sellersprite-system` | 认证、仪表盘、用户、部门、角色、权限、字典和运维管理。 |
| `sellersprite-api` | SellerSprite Client、契约模型、九域 Service 与 45 个内部代理端点。 |
| `sellersprite-ai` | AI 聊天、会话、记忆、Prompt 审计和 SellerSprite `@Tool`。 |
| `sellersprite-research` | Spring Batch 市场调研工作流、Mock/Remote 数据源、快照和 Excel 报告。 |
| `sellersprite-server` | Spring Boot 启动入口和模块装配。 |
| `sellersprite-web` | Vue 管理台、动态菜单、权限控制、AI 聊天、SellerSprite 调试台和市场调研报告页。 |

后端入口为 `sellersprite-server/src/main/java/com/yuanbaomao/sellersprite/server/SellerSpriteServiceApplication.java`，前端入口为 `sellersprite-web/src/main.ts`。

## 运行前提

- JDK 21、Maven 3.9+
- Node.js 22.12+、npm
- MySQL 8.x
- `yuanbaomao-scaffold-parent:0.1.0-SNAPSHOT` 及其 Starter 可从本地仓库或制品仓库解析
- 使用 AI 时提供 OpenAI 兼容模型密钥
- 实际调用卖家精灵上游时提供 `SELLERSPRITE_API_SECRET_KEY`

默认使用本地缓存；切换 Redis 时按 `ybm.cache` 与 Spring Data Redis 标准配置补充连接信息。

## 数据库初始化与升级

### 全新数据库

执行完整结构脚本：

```bash
mysql -uroot -p -e "source sql/schema.sql"
```

默认数据库名为 `sellersprite_service`。脚本会创建初始管理员 `admin / 123456`，首次登录后必须立即修改密码。

> 警告：不要对已有数据库重复执行 `schema.sql`。它会把 `admin` 密码重置为 `123456`。

### 已有数据库

仅执行尚未应用的迁移，顺序如下：

1. `sql/migrations/20260710_add_sellersprite_web_console.sql`
2. `sql/migrations/20260713_upgrade_scaffold_console.sql`
3. `sql/migrations/20260713_add_sellersprite_workbench.sql`
4. `sql/migrations/20260713_add_spring_batch_metadata.sql`
5. `sql/migrations/20260713_add_market_research_batch_report.sql`
6. `sql/migrations/20260714_add_market_research_console.sql`

`20260710_add_sellersprite_web_console.sql` 是一次性结构迁移，不要重复执行。`20260713_upgrade_scaffold_console.sql` 包含 `ALTER TABLE ... MODIFY COLUMN`，大表可能锁表或重建，应安排维护窗口。工作台迁移可安全重跑；若执行时接口目录尚未同步，它会先写入菜单和角色授权，接口关联写入 0 行。

市场调研需要先执行 Spring Batch 6.0.4 官方 MySQL 元数据表迁移，再执行三张业务表迁移和管理台菜单迁移；应用配置为 `spring.batch.jdbc.initialize-schema=never`，不会在生产环境自动建表。菜单迁移可安全重跑，接口目录尚未同步时会先创建菜单和管理员授权。

应用启动后，管理员在“接口资源”页面依次执行“同步接口目录”和“同步菜单接口绑定”，即可按前端固定清单关联工作台的 45 个代理端点与市场调研的 3 个任务端点；也可以在接口目录同步后重跑对应菜单迁移。

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

## 市场调研 Batch 报告

第一版不使用 AI，按固定 8 步工作流依次完成参数校验、配额采集、市场与商品采集、关键词采集、评论采集、数据整理、Excel 生成和校验发布。默认数据源是 `MOCK`，无需网络和 SellerSprite 密钥即可跑通完整流程；Mock 只用于调研工作流，不新增或替换现有 `/api/sellersprite/**` 接口。

创建任务示例：

```http
POST /api/market-research/jobs
Content-Type: application/json

{
  "reportName": "美容仪美国站市场调研",
  "keyword": "facial cleansing device",
  "seedAsins": ["B0MOCK0001"]
}
```

进度和下载接口：

- `GET /api/market-research/jobs/{jobId}`
- `GET /api/market-research/jobs/{jobId}/download`

执行 `20260714_add_market_research_console.sql` 后，管理台通过 `/research/market-report` 提供创建、八阶段轮询、失败信息和 Excel 下载。创建成功的 `jobId` 会保留在页面 query 中，刷新后可恢复当前任务；第一版没有任务列表、取消或业务重试接口。

切换远端前，先完成上面的三份市场调研相关数据库迁移，并在本地配置中启用 `REMOTE`：

```yaml
sellersprite:
  research:
    source-mode: REMOTE
    recovery-enabled: true
    output-directory: ./data/market-research
    template-location: classpath:research/templates/market-research-v1.xlsx
```

远端模式通过现有 `AccountService`、`ProductService`、`KeywordService` 和 `ReviewService` 采集数据，复用统一认证、超时和异常转换。配额响应也会完整落入快照，不再只做非空检查。

报告共 16 个页签：前 11 个保留用户模板并填入当前能明确映射的数据，后面固定追加 `原始数据索引`、`原始_配额`、`原始_市场商品`、`原始_关键词`、`原始_评论`。原始页以当前已接入接口返回的完整响应字段为准，不受模板列名限制；`items` 数组逐条展开成行，嵌套对象使用 `item.xxx` 或 `response.xxx` 路径，其他数组保留为 JSON。重复记录和原始顺序不去重，显式 `null` 标记为 `<NULL>`，超长文本拆分为连续列，大整数按文本保存，避免 Excel 截断或损失精度。

“全字段”目前指上述 4 类已接入接口在本次请求中返回的所有字段，不代表自动调用 SellerSprite 的全部接口，也不代表自动抓取全部分页。第一版远端采样范围仍是商品第 1 页最多 50 条、关键词第 1 页最多 15 条、每个种子 ASIN 的评论第 1 页最多 10 条；页码、页大小、总数和来源快照元数据都会写入原始页，便于后续决定是否扩展全量分页。`VOC` 和评论归因区域会明确标注“第一版未启用 AI”，不会生成伪分析。生成文件默认保存到 `./data/market-research/{jobId}/`，数据库只记录受控存储键、文件大小和 SHA-256。

人工联调建议先使用管理员账号。普通角色需要管理员执行“同步接口目录”，再为该角色授权 3 个 `/api/market-research/jobs` 路由，否则接口权限拦截器会返回无权访问。Remote 复用 `sellersprite.api.base-url` 和 `SELLERSPRITE_API_SECRET_KEY`；若实际网关与默认值不同，在本地配置或环境变量中覆盖后再测试。

SellerSprite 常用环境变量：

| 环境变量 | 默认值 | 说明 |
| --- | --- | --- |
| `SELLERSPRITE_API_ENABLED` | `true` | 是否启用上游调用。 |
| `SELLERSPRITE_API_BASE_URL` | `https://api.sellersprite.com` | 上游网关，只应在受控环境覆盖。 |
| `SELLERSPRITE_API_SECRET_KEY` | 无 | 官方 `secret-key`，禁止提交。 |
| `SELLERSPRITE_API_CONNECT_TIMEOUT` | `5s` | 建立连接超时。 |
| `SELLERSPRITE_API_READ_TIMEOUT` | `30s` | 读取响应超时。 |

## 启动

启动后端：

```bash
mvn -pl sellersprite-server -am spring-boot:run
```

启动前端：

```bash
cd sellersprite-web
npm install
npm run dev
```

默认地址：

- 前端：`http://localhost:5173`
- 后端：`http://localhost:8089`
- 健康检查：`http://localhost:8089/actuator/health`

Vite 默认将 `/api` 代理到 `http://localhost:8089`，可通过 `SELLERSPRITE_API_TARGET` 覆盖。浏览器只调用内部固定代理路径，不接收上游 URL 或 `secret-key`。

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
- 请求防重复提交、参数错误、业务错误码、`trackId`、耗时和完成时间
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

后端全量测试：

```bash
mvn test --no-transfer-progress
```

前端门禁：

```bash
cd sellersprite-web
npm run lint
npm run test:unit
npm run build
npm run test:e2e
```

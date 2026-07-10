# sellersprite-service

SellerSprite Open API 集成服务。项目基于 JDK 21、Spring Boot 4.1 和 Maven 多模块构建，在保留系统管理、会话鉴权、统一 Web/数据访问基础设施及 AI 聊天能力的基础上，完整封装 SellerSprite 当前公开接口。

当前仓库仅包含后端服务，不包含 Web 前端。

## 主要能力

- **统一基础设施**：统一响应与异常处理、分页模型、请求上下文、MDC `trackId`、UUIDv7、OpenAPI、MyBatis-Plus 审计字段填充和操作日志。
- **会话鉴权**：访问令牌、HttpOnly 刷新 Cookie、刷新令牌轮换与复用检测、会话查询和退出登录。
- **系统管理**：用户、部门、角色、功能、接口、字典等管理能力，代码按业务域组织。
- **AI 聊天**：接入 OpenAI 兼容接口，支持多轮对话、JDBC Chat Memory、完整聊天历史、会话重命名/删除和 Prompt 调用审计。
- **SellerSprite 接口**：统一 Client、官方认证头、请求 ID、超时和异常映射，按九个业务域提供 45 个强类型 Service 与 Controller 方法。
- **可替换 Starter**：通过 `yuanbaomao-scaffold-parent` 统一依赖，并消费 Web、MyBatis、字典、缓存和日志 Starter。

## 技术栈

| 分类 | 组件 |
| --- | --- |
| 运行环境 | JDK 21、Maven 3.9+ |
| 应用框架 | Spring Boot 4.1.0 |
| AI | Spring AI 2.0.0、OpenAI ChatModel、JDBC Chat Memory |
| 数据访问 | MyBatis-Plus、MyBatis-Plus-Join、MySQL 8 |
| 缓存 | 默认本地缓存，可按 Starter 配置切换 Redis |
| API 文档 | SpringDoc OpenAPI、Swagger UI |
| 工程组织 | Maven 多模块、Lombok |

## 模块说明

| 模块 | 职责 |
| --- | --- |
| `sellersprite-common` | 统一响应扩展、鉴权拦截、通用数据库实体、Mapper、DAO 和共享框架适配。 |
| `sellersprite-system` | 认证、用户、部门、角色、权限与字典等系统管理业务。 |
| `sellersprite-ai` | AI 聊天、会话管理、模型上下文记忆和 Prompt 审计。 |
| `sellersprite-api` | SellerSprite Client、44 个业务接口和次数查询接口的 DTO/VO、Service 与分类 Controller。 |
| `sellersprite-server` | Spring Boot 启动入口、运行时资源和模块装配。 |

应用入口为 `sellersprite-server/src/main/java/com/yuanbaomao/sellersprite/server/SellerSpriteServiceApplication.java`。

## 目录结构

```text
sellersprite-service/
├── config/                 # 外置配置
├── sellersprite-ai/            # AI 业务模块
├── sellersprite-api/           # SellerSprite Open API 集成模块
├── sellersprite-common/        # 公共框架与数据访问模块
├── sellersprite-server/        # 应用启动模块
├── sellersprite-system/        # 系统管理模块
├── sql/
│   ├── schema.sql          # 全量数据库结构
│   └── migrations/         # 存量数据库升级脚本
└── pom.xml                 # Maven 聚合工程
```

## 运行前提

- JDK 21
- Maven 3.9 或更高版本
- MySQL 8.x
- `yuanbaomao-scaffold-parent:0.1.0-SNAPSHOT` 及其 Web、MyBatis、字典、缓存、日志 Starter 已安装到本地 Maven 仓库，或可从配置的制品仓库下载
- OpenAI 兼容模型密钥；不使用 AI 能力时可将 `sellersprite.ai.chat.enabled` 设为 `false`
- SellerSprite `secret-key`；不调用 SellerSprite 接口时可以留空，实际调用前会返回明确的未配置错误

默认使用本地缓存，启动基础服务时不强制依赖 Redis。

## 快速启动

### 1. 初始化数据库

全新环境执行完整结构脚本：

```bash
mysql -uroot -p -e "source sql/schema.sql"
```

默认数据库名为 `sellersprite_service`。`schema.sql` 不创建默认用户，请按部署环境的初始化流程创建首个管理账号。

已有数据库按需执行 `sql/migrations/` 下尚未应用的脚本，不要对同一结构重复执行迁移。

### 2. 创建本地配置

项目跟踪 `config/application.yml` 作为非敏感默认配置，并可选导入不会提交到 Git 的 `config/application-local.yml`。本地至少需要补充数据源信息；启用 AI 时还需提供模型密钥：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/sellersprite_service?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: ${DB_PASSWORD}
  ai:
    openai:
      api-key: ${AI_OPENAI_API_KEY}

sellersprite:
  api:
    secret-key: ${SELLERSPRITE_API_SECRET_KEY}
```

生产环境应通过环境变量或配置中心注入密码和密钥，不要将敏感值写入 Git 跟踪文件。

### 3. 启动服务

```bash
mvn -pl sellersprite-server -am spring-boot:run
```

默认访问地址：

- 服务：`http://localhost:8089`
- 健康检查：`http://localhost:8089/actuator/health`
- Swagger UI：`http://localhost:8089/swagger-ui.html`
- OpenAPI JSON：`http://localhost:8089/v3/api-docs`

可通过环境变量 `SERVER_PORT` 覆盖默认端口。

## 鉴权说明

登录接口 `POST /api/auth/login` 返回访问令牌，并通过响应头写入 HttpOnly 刷新 Cookie。调用受保护接口时携带：

```http
Authorization: Bearer <access-token>
```

刷新、退出和当前会话接口位于 `/api/auth/refresh`、`/api/auth/logout`、`/api/auth/session`。默认访问令牌有效期为 15 分钟，刷新令牌有效期为 14 天；具体值以 `sellersprite.auth` 配置为准。

除 `sellersprite.auth.public-paths` 配置的路径外，`/api/**` 默认要求有效访问令牌。

## API 概览

| 路径前缀 | 能力 |
| --- | --- |
| `/api/auth` | 登录、刷新、退出、当前会话 |
| `/api/users` | 用户创建、详情、分页查询 |
| `/api/depts` | 部门创建与查询 |
| `/api/roles` | 角色创建、查询与用户角色绑定 |
| `/api/permissions` | 系统功能和接口管理 |
| `/api/system/dicts` | 字典类型、字典项和字典查询 |
| `/api/ai/chat` | 发送 AI 聊天消息；不传 `conversationId` 时自动创建会话 |
| `/api/ai/conversations` | 会话分页、详情、重命名与删除 |
| `/api/sellersprite/account` | SellerSprite 当前可用调用次数 |
| `/api/sellersprite/products` | 查竞品、选产品和产品类目 |
| `/api/sellersprite/asins` | ASIN 详情、优惠、销量、预测和 Keepa 趋势 |
| `/api/sellersprite/keywords` | 关键词研究、挖掘、ABA、谷歌趋势和出单词 |
| `/api/sellersprite/traffic` | 关键词反查、关联流量、统计和流量来源 |
| `/api/sellersprite/markets` | 市场研究及十四类市场统计分析 |
| `/api/sellersprite/reviews` | 评论查询 |
| `/api/sellersprite/trademarks` | 全球商标范围、详情、列表和统计 |
| `/api/sellersprite/tools` | 图片文字识别 |

接口请求、响应模型及完整参数以 Swagger UI 为准。

## SellerSprite Open API

外部网关默认为 `https://api.sellersprite.com`。统一 Client 按官方规则发送 `secret-key`、`x-request-id` 和正确的内容类型；官方当前没有声明 HMAC、MD5、时间戳摘要或响应验签，因此实现不会臆造签名字段。内部 `/api/sellersprite/**` 路由沿用脚手架访问令牌鉴权，不接受调用者传入外部密钥或目标 URL。

常用环境变量：

| 环境变量 | 默认值 | 说明 |
| --- | --- | --- |
| `SELLERSPRITE_API_ENABLED` | `true` | 是否启用 SellerSprite 外部调用 |
| `SELLERSPRITE_API_BASE_URL` | `https://api.sellersprite.com` | SellerSprite 外部网关，只应在受控环境覆盖 |
| `SELLERSPRITE_API_SECRET_KEY` | 无 | 官方 `secret-key`，禁止提交到 Git |
| `SELLERSPRITE_API_CONNECT_TIMEOUT` | `5s` | 建立连接超时 |
| `SELLERSPRITE_API_READ_TIMEOUT` | `30s` | 读取响应超时 |

官方文档没有提供 OpenAPI schema。项目保留了结构化契约快照 `docs/sellersprite-api-contract.json`，并由生成器维护 DTO、VO、Service 和 Controller：

```bash
python tools/generate_sellersprite_contracts.py
python tools/generate_sellersprite_endpoints.py
```

契约生成器按官方参数表写入中文 Schema 和字段注释，并保存 38 份官方响应示例用于强类型反序列化测试；官方未提供响应示例的 6 个页面按字段表、方法、路径和编译契约验证。完整接口清单和调用代码示例见 `docs/superpowers/plans/2026-07-10-sellersprite-open-api.md`。

## AI 配置

常用环境变量：

| 环境变量 | 默认值 | 说明 |
| --- | --- | --- |
| `AI_CHAT_ENABLED` | `true` | 是否启用 AI 聊天业务能力 |
| `AI_CHAT_PROVIDER` | `openai` | 会话记录中的模型服务提供方 |
| `AI_CHAT_MODEL` | `gpt-5.5` | 模型名称 |
| `AI_CHAT_MEMORY_WINDOW_SIZE` | `20` | 参与模型上下文的消息窗口大小 |
| `AI_OPENAI_API_KEY` | 无 | OpenAI 兼容接口密钥 |
| `AI_OPENAI_BASE_URL` | `https://yuanbaomao.cyou/v1` | OpenAI 兼容接口地址 |
| `AI_OPENAI_CHAT_COMPLETIONS_PATH` | `/chat/completions` | Chat Completions 路径 |

Spring AI 的窗口记忆存放在 `SPRING_AI_CHAT_MEMORY`，前端完整聊天历史存放在 `ai_conversation` 和 `ai_conversation_message`，模型调用审计存放在 `ai_prompt_record`。生产环境由 `sql/schema.sql` 统一管理这些表，不启用 Starter 自动建表。

## 构建与验证

运行全部测试并完成打包校验：

```bash
mvn clean verify --no-transfer-progress
```

只验证启动模块及其依赖：

```bash
mvn -pl sellersprite-server -am test --no-transfer-progress
```

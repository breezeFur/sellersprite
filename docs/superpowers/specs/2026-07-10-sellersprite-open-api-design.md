# SellerSprite Open API 集成设计

## 背景与事实来源

新项目基于 `D:\develop\scaffold` 当前工作区的真实源码状态复制，包含未提交源码，排除 `.git`、`.idea`、`.codegraph` 和所有 `target`。目标目录为 `D:\develop\sellersprite`。

外部接口以 [SellerSprite Open API](https://open.sellersprite.com/api) 和 [官方 Java Demo](https://gitee.com/cdyunya/sellersprite-api-demo) 为事实来源。2026-07-10 盘点结果为 44 个业务接口，另有 `/v1/visits` 次数查询，共 45 个操作。

官方认证规则只有 `secret-key`、`Content-Type: application/json` 和每次请求唯一的 `x-request-id`。官方文档与 Demo 均未定义 HMAC、MD5、时间戳摘要或响应验签算法，因此本项目不会虚构签名；`SellerSpriteAuthStrategy` 作为扩展点，可在官方未来补充规则时替换。

## 目标

- 保留脚手架现有系统管理、AI、数据库、缓存、统一响应、异常和日志能力。
- 完整改名为 `sellersprite`，不残留本项目旧模块名或旧 Java 包名。
- 所有 SellerSprite 操作只能通过统一 Client 发出请求。
- 所有 Controller 请求和响应采用强类型模型，不使用 `Map` 暴露公共契约。
- 按官方业务语义分包，单个业务域可以独立理解和测试。
- 密钥永不从 Controller 参数接收，永不写入 Git，永不进入日志。

## 非目标

- 不缓存 SellerSprite 查询结果，不落库保存第三方原始数据。
- 不实现官方未声明的重试或限流补偿；避免重复计费和放大限流。
- 不绕过脚手架现有 Token 鉴权，新 Controller 默认仍受 `/api/**` 鉴权保护。
- 不承诺运行真实付费接口测试；没有密钥时使用 Mock HTTP Server 验证协议。

## 项目与模块

```text
sellersprite-service
├── sellersprite-common
├── sellersprite-ai
├── sellersprite-system
├── sellersprite-api
└── sellersprite-server
```

- `sellersprite-common`：保留统一响应、异常、数据库和框架适配。
- `sellersprite-ai`：保留脚手架 AI 能力。
- `sellersprite-system`：保留认证和系统管理能力。
- `sellersprite-api`：拥有 SellerSprite Client、分类接口、DTO/VO、业务服务和 Controller。
- `sellersprite-server`：启动入口并依赖 `sellersprite-api` 完成运行时装配。

基础包统一为 `com.yuanbaomao.sellersprite`。外部父依赖 `com.yuanbaomao:yuanbaomao-scaffold-parent` 是已有制品坐标，不随项目改名。

## Client 设计

`SellerSpriteClient` 基于 Spring `RestClient`，提供带泛型响应类型的 `get` 与 `post`。业务域封装只传递路径、查询参数或请求体以及响应类型，不接触密钥和底层 HTTP 异常。

`SellerSpriteProperties` 配置：

- `enabled`：是否启用外部接口。
- `base-url`：默认 `https://api.sellersprite.com`。
- `secret-key`：必需密钥，只从 `${SELLERSPRITE_API_SECRET_KEY:}` 或本地配置注入。
- `connect-timeout`：默认 5 秒。
- `read-timeout`：默认 30 秒。

`SellerSpriteAuthStrategy` 默认实现统一写入：

```text
Content-Type: application/json;charset=UTF-8
secret-key: <configured secret>
x-request-id: <new UUIDv7 for every attempt>
```

请求 ID 使用项目现有 `IdGenerator`，每次实际 HTTP 调用生成一次。日志只记录请求 ID、HTTP 方法、外部路径、耗时和外部错误码，不记录密钥和完整请求体。

## 响应与错误

外部响应统一解析为 `SellerSpriteResponse<T>`：`code`、`message`、`data`。只有 `code == "OK"` 才返回 `requiredData()`；其余官方错误码统一抛出 `SellerSpriteApiException`，再由项目全局异常处理转换为明确的 `ResultCode`。

需要区分：

- 配置禁用或密钥为空：调用前失败。
- HTTP 非 2xx：记录状态码和请求 ID，抛出上游 HTTP 异常。
- 连接或读取超时：抛出上游超时异常，不自动重试。
- JSON 无法解析或响应为空：抛出上游协议异常。
- `ERROR_PARAM`：转换为上游参数错误。
- `ERROR_SECRET_KEY*`：转换为上游认证错误。
- `ERROR_VISIT_MAX`：转换为上游配额耗尽。
- 其他非 `OK`：转换为通用上游业务错误，保留外部错误码但不泄露内部堆栈。

## 业务域与操作矩阵

| 包 | 操作数 | 外部操作 |
|---|---:|---|
| `account` | 1 | 可用次数查询 |
| `product` | 3 | 查竞品、选产品、查产品类目 |
| `asin` | 7 | ASIN 详情、优惠趋势、详情及优惠趋势、销量趋势、ASIN 销量预测、BSR 销量预测、Keepa 趋势 |
| `keyword` | 9 | 关键词选品、趋势、挖掘、拓展流量词、ABA 周/月/趋势、谷歌趋势、出单词反查 |
| `traffic` | 5 | 关键词反查、关联流量列表、流量词统计、关联流量统计、流量来源 |
| `market` | 14 | 选市场列表、统计及商品/品牌/卖家/需求/上架/评分/价格/A+视频分布 |
| `review` | 1 | 查评论 |
| `trademark` | 4 | 全球商标数据范围、详情、列表、统计 |
| `tool` | 1 | 图片文字识别 |

每个包采用以下边界：

```text
<domain>/controller
<domain>/service
<domain>/service/impl
<domain>/model/dto
<domain>/model/vo
```

Service 实现只调用 `SellerSpriteClient`。Controller 只校验入参、调用 Service，并包装现有 `Result<T>`。

## 数据模型规则

- 请求 DTO 使用官方 JSON 字段名，必要时使用 `@JsonProperty` 明确映射。
- 官方标为必填的字段使用 `@NotBlank`、`@NotNull`、`@NotEmpty` 等约束。
- 市场编码、月份、排序字段等稳定封闭值使用枚举；外部 JSON 仍按官方字符串值序列化。
- 官方的 `Object` 嵌套结构建立命名类型，例如 `SortOrder`、`BadgeVo`、`SubcategoryVo`。
- 官方分页响应使用 `SellerSpritePageVo<T>`，仅在字段结构完全一致时复用。
- 同名但语义或类型不同的字段不强行共用类。
- 金额、比率和可能含小数的统计值使用 `BigDecimal`；计数使用 `Integer` 或 `Long`；Unix 毫秒使用 `Long`。
- 官方文档存在重复字段或示例与表格冲突时，以响应示例 JSON 的实际结构确定嵌套，以表格中文说明补充注释，并在代码注释中记录兼容原因。

## Controller 设计

内部路由统一以 `/api/sellersprite` 开头，按九个业务域建立 Controller，例如：

```text
/api/sellersprite/account/**
/api/sellersprite/products/**
/api/sellersprite/asins/**
/api/sellersprite/keywords/**
/api/sellersprite/traffic/**
/api/sellersprite/markets/**
/api/sellersprite/reviews/**
/api/sellersprite/trademarks/**
/api/sellersprite/tools/**
```

GET 外部接口在内部也使用 GET，超过五个或具有业务含义的查询参数使用 `@ModelAttribute` 强类型请求；POST 外部接口使用 `@Valid @RequestBody`。Controller 不透传 `secret-key`、`x-request-id` 或任意目标 URL。

## 测试策略

- Client 单元测试先验证失败，再实现认证头、每次唯一请求 ID、GET 查询参数、路径变量、POST JSON、`OK` 响应和所有错误类别。
- 每个业务域至少有契约测试，验证所有操作使用预期 HTTP 方法和外部路径。
- DTO 反序列化测试使用官方响应示例，重点覆盖分页、嵌套对象、数组、时间戳和大数字。
- Controller 使用 MockMvc 验证路由、校验、Service 委派和统一响应。
- 建立 45 操作清单测试，防止遗漏或重复路径。
- 最终运行全量 Maven 测试、打包、旧命名扫描、密钥扫描和中文乱码扫描。

## 数据库、配置与 Git

- 默认数据库改为 `sellersprite_service`，更新 `sql/schema.sql`、迁移 SQL、测试数据库和数据库代码生成器。
- 本地 `config/application-local.yml` 仍被 `.gitignore` 忽略，可保留本机连接信息但不得进入提交。
- `.gitignore` 保持 `vibecodingdoc/`、`openspec/`、`.codex/`、`.codegraph/` 和本地配置规则。
- 新项目独立初始化 Git；实现和验证完成后提交，不继承原项目 Git 历史。

## 验收标准

- Maven 模块和 Java 包无本项目旧 `scaffold` 命名残留，外部父 POM 坐标除外。
- `sellersprite-api` 模块覆盖官方当前 45 个操作。
- 所有操作通过统一 Client，分类 Controller 不直接调用 `RestClient`。
- 所有公开出入参数有强类型和中文注释，无公共 `Map` 契约。
- 没有真实密钥进入 Git 或日志。
- 全量测试与打包成功，静态扫描无已知遗漏。

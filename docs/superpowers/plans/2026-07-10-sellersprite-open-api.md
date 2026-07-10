# SellerSprite Open API Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. This project explicitly uses inline execution; do not dispatch subagents.

**Goal:** 从当前脚手架工作区创建完整改名的 `sellersprite` 项目，并通过一个统一 Client 强类型封装 SellerSprite 当前全部 45 个操作和九类 Controller。

**Architecture:** 新增 `sellersprite-api` 业务模块。底层 `SellerSpriteClient` 统一处理认证头、UUIDv7 请求 ID、GET/POST、泛型响应和外部错误；九个业务域 Service 只依赖该 Client，Controller 只做校验、委派和 `Result<T>` 包装。

**Tech Stack:** JDK 21、Maven、多模块 Spring Boot 4.1、Spring `RestClient`、Jackson、Jakarta Validation、SpringDoc、JUnit 5、AssertJ、MockRestServiceServer、MockMvc。

## Global Constraints

- 目标目录固定为 `D:\develop\sellersprite`，源 `D:\develop\scaffold` 不得被修改。
- 基础包固定为 `com.yuanbaomao.sellersprite`。
- 外部父依赖 `com.yuanbaomao:yuanbaomao-scaffold-parent:0.1.0-SNAPSHOT` 必须保留原坐标。
- 默认数据库名固定为 `sellersprite_service`。
- 官方网关固定为 `https://api.sellersprite.com`，允许通过 `SELLERSPRITE_API_BASE_URL` 覆盖。
- 密钥仅通过 `SELLERSPRITE_API_SECRET_KEY` 或被 Git 忽略的本地配置注入。
- 不实现官方未声明的摘要签名，不自动重试计费请求，不记录密钥和完整敏感请求体。
- Controller 默认受现有 `/api/**` Token 鉴权保护。
- 所有公开 DTO/VO 字段必须有中文 `@Schema`，关键兼容逻辑必须有中文注释。

---

## File Structure

```text
sellersprite-api/
├── pom.xml
├── src/main/java/com/yuanbaomao/sellersprite/api/
│   ├── client/
│   │   ├── SellerSpriteClient.java
│   │   ├── SellerSpriteAuthStrategy.java
│   │   ├── DefaultSellerSpriteAuthStrategy.java
│   │   ├── SellerSpriteProperties.java
│   │   ├── SellerSpriteResponse.java
│   │   ├── SellerSpriteOperation.java
│   │   ├── SellerSpriteApiException.java
│   │   └── SellerSpriteApiConfig.java
│   ├── common/model/dto/
│   ├── common/model/vo/
│   ├── account/{controller,service,service/impl,model/vo}/
│   ├── product/{controller,service,service/impl,model/dto,model/vo}/
│   ├── asin/{controller,service,service/impl,model/dto,model/vo}/
│   ├── keyword/{controller,service,service/impl,model/dto,model/vo}/
│   ├── traffic/{controller,service,service/impl,model/dto,model/vo}/
│   ├── market/{controller,service,service/impl,model/dto,model/vo}/
│   ├── review/{controller,service,service/impl,model/dto,model/vo}/
│   ├── trademark/{controller,service,service/impl,model/dto,model/vo}/
│   └── tool/{controller,service,service/impl,model/dto,model/vo}/
└── src/test/java/com/yuanbaomao/sellersprite/api/
```

## Complete Operation Matrix

| 域 | Java 方法 | HTTP | 外部路径 |
|---|---|---|---|
| account | `getVisits` | GET | `/v1/visits` |
| product | `lookupCompetitors` | POST | `/v1/product/competitor-lookup` |
| product | `researchProducts` | POST | `/v1/product/research` |
| product | `listProductNodes` | GET | `/v1/product/node` |
| asin | `getAsinDetail` | GET | `/v1/asin/{marketplace}/{asin}` |
| asin | `getCouponTrend` | GET | `/v1/asin/{marketplace}/{asin}/coupon-trend` |
| asin | `getAsinWithCouponTrend` | GET | `/v1/asin/{marketplace}/{asin}/with-coupon-trend` |
| asin | `getSalesTrend` | GET | `/v1/asin/{marketplace}/{asin}/sales-trend` |
| asin | `predictAsinSales` | GET | `/v1/sales/prediction/asin` |
| asin | `predictBsrSales` | GET | `/v1/sales/prediction/bsr` |
| asin | `getKeepaTrend` | GET | `/v1/keepa/{marketplace}/{asin}` |
| keyword | `researchKeywords` | POST | `/v1/keyword-research` |
| keyword | `getKeywordResearchTrends` | POST | `/v1/keyword-research/trends` |
| keyword | `mineKeywords` | POST | `/v1/keyword/miner` |
| keyword | `extendTrafficKeywords` | POST | `/v1/traffic/extend` |
| keyword | `researchAbaWeekly` | POST | `/v1/aba/research/weekly` |
| keyword | `researchAbaMonthly` | POST | `/v1/aba/research/monthly` |
| keyword | `getAbaKeywordTrends` | POST | `/v1/aba/research/trends` |
| keyword | `getGoogleTrends` | GET | `/v1/google/trends` |
| keyword | `reverseOrderKeywords` | POST | `/v1/keyword-order` |
| traffic | `reverseKeywords` | POST | `/v1/traffic/keyword` |
| traffic | `listRelatedTraffic` | POST | `/v1/traffic/listing/page` |
| traffic | `getKeywordStats` | GET | `/v1/traffic/keyword/stat/{marketplace}/{asin}` |
| traffic | `getListingStats` | GET | `/v1/traffic/listing/stat/{marketplace}/{asin}` |
| traffic | `getTrafficSources` | POST | `/v1/traffic/source` |
| market | `researchMarkets` | POST | `/v1/market/research` |
| market | `getMarketStatistics` | POST | `/v1/market/statistics` |
| market | `getGoodsConcentration` | POST | `/v1/market/goods` |
| market | `getBrandConcentration` | POST | `/v1/market/brand` |
| market | `getSellerLocationDistribution` | POST | `/v1/market/seller/location` |
| market | `getSellerConcentration` | POST | `/v1/market/seller` |
| market | `getSellerTypeDistribution` | POST | `/v1/market/seller/type` |
| market | `getDemandTrend` | POST | `/v1/market/performance` |
| market | `getShelfTimeDistribution` | POST | `/v1/market/shelf/time` |
| market | `getShelfTrendDistribution` | POST | `/v1/market/shelf/trend` |
| market | `getRatingsDistribution` | POST | `/v1/market/ratings` |
| market | `getRatingDistribution` | POST | `/v1/market/rating` |
| market | `getPriceDistribution` | POST | `/v1/market/price` |
| market | `getEbcDistribution` | POST | `/v1/market/ebc` |
| review | `listReviews` | POST | `/v1/review` |
| tool | `recognizeImageText` | POST | `/v1/ocr` |
| trademark | `getBrandRange` | GET | `/v1/global/brand/range` |
| trademark | `getBrandDetail` | GET | `/v1/global/brand/detail` |
| trademark | `listBrands` | POST | `/v1/global/brand/list` |
| trademark | `getBrandStats` | POST | `/v1/global/brand/stats` |

### Task 1: Finish project baseline

**Files:**
- Modify: `pom.xml`
- Modify: `config/application.yml`
- Modify: `sql/schema.sql`
- Modify: `README.md`
- Verify: every existing module and Java source path

**Interfaces:**
- Produces: Maven coordinates `com.yuanbaomao:sellersprite-service:0.1.0-SNAPSHOT`
- Produces: package root `com.yuanbaomao.sellersprite`
- Produces: database `sellersprite_service`

- [x] **Step 1: Copy current workspace and initialize Git**

Copied 204 included files with zero path differences and initialized `master` in the target directory.

- [x] **Step 2: Rename project identifiers and paths**

Protected `yuanbaomao-scaffold-parent`, replaced project-owned names, renamed four module folders, eight Java package roots, two startup-class files and one migration file.

- [x] **Step 3: Run the renamed baseline tests**

Run:

```powershell
mvn test --no-transfer-progress
```

Expected: all copied scaffold tests pass under `com.yuanbaomao.sellersprite`.

### Task 2: Add the module and configuration contract

**Files:**
- Create: `sellersprite-api/pom.xml`
- Create: `sellersprite-api/src/test/java/com/yuanbaomao/sellersprite/api/client/SellerSpritePropertiesTest.java`
- Create: `sellersprite-api/src/main/java/com/yuanbaomao/sellersprite/api/client/SellerSpriteProperties.java`
- Modify: `pom.xml`
- Modify: `sellersprite-server/pom.xml`
- Modify: `config/application.yml`

**Interfaces:**
- Produces: `SellerSpriteProperties` with `enabled`, `baseUrl`, `secretKey`, `connectTimeout`, `readTimeout`

- [x] **Step 1: Write the failing configuration test**

```java
class SellerSpritePropertiesTest {
    @Test
    void shouldExposeSafeDefaults() {
        SellerSpriteProperties properties = new SellerSpriteProperties();
        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getBaseUrl()).isEqualTo("https://api.sellersprite.com");
        assertThat(properties.getConnectTimeout()).isEqualTo(Duration.ofSeconds(5));
        assertThat(properties.getReadTimeout()).isEqualTo(Duration.ofSeconds(30));
    }
}
```

- [x] **Step 2: Run RED**

```powershell
mvn -pl sellersprite-api -Dtest=SellerSpritePropertiesTest test --no-transfer-progress
```

Expected: FAIL because the module and class do not exist.

- [x] **Step 3: Implement configuration and module wiring**

```java
@Data
@Validated
@ConfigurationProperties(prefix = "sellersprite.api")
public class SellerSpriteProperties {
    private boolean enabled = true;
    private String baseUrl = "https://api.sellersprite.com";
    private String secretKey = "";
    private Duration connectTimeout = Duration.ofSeconds(5);
    private Duration readTimeout = Duration.ofSeconds(30);
}
```

```yaml
sellersprite:
  api:
    enabled: ${SELLERSPRITE_API_ENABLED:true}
    base-url: ${SELLERSPRITE_API_BASE_URL:https://api.sellersprite.com}
    secret-key: ${SELLERSPRITE_API_SECRET_KEY:}
    connect-timeout: ${SELLERSPRITE_API_CONNECT_TIMEOUT:5s}
    read-timeout: ${SELLERSPRITE_API_READ_TIMEOUT:30s}
```

- [x] **Step 4: Run GREEN**

Run the same focused command. Expected: PASS.

### Task 3: Implement authentication and Client with TDD

**Files:**
- Test: `sellersprite-api/src/test/java/com/yuanbaomao/sellersprite/api/client/SellerSpriteClientTest.java`
- Create: `sellersprite-api/src/main/java/com/yuanbaomao/sellersprite/api/client/SellerSpriteAuthStrategy.java`
- Create: `sellersprite-api/src/main/java/com/yuanbaomao/sellersprite/api/client/DefaultSellerSpriteAuthStrategy.java`
- Create: `sellersprite-api/src/main/java/com/yuanbaomao/sellersprite/api/client/SellerSpriteClient.java`
- Create: `sellersprite-api/src/main/java/com/yuanbaomao/sellersprite/api/client/SellerSpriteResponse.java`
- Create: `sellersprite-api/src/main/java/com/yuanbaomao/sellersprite/api/client/SellerSpriteApiException.java`

**Interfaces:**
- Produces: `String SellerSpriteAuthStrategy.apply(HttpHeaders headers)`
- Produces: `<T> T SellerSpriteClient.get(...)`
- Produces: `<T> T SellerSpriteClient.post(...)`

- [x] **Step 1: Write failing header and success-response tests**

```java
server.expect(requestTo("https://api.sellersprite.com/v1/visits"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("secret-key", "test-secret"))
        .andExpect(header("x-request-id", "018f-test-request-id"))
        .andRespond(withSuccess("{\"code\":\"OK\",\"message\":\"成功\",\"data\":{\"remaining\":100}}",
                MediaType.APPLICATION_JSON));

VisitsVo result = client.get("/v1/visits", new ParameterizedTypeReference<>() {});
assertThat(result.getRemaining()).isEqualTo(100L);
```

- [x] **Step 2: Run RED**

```powershell
mvn -pl sellersprite-api -Dtest=SellerSpriteClientTest test --no-transfer-progress
```

Expected: FAIL because Client types do not exist.

- [x] **Step 3: Implement authentication and typed execution**

```java
public interface SellerSpriteAuthStrategy {
    String apply(HttpHeaders headers);
}
```

```java
@Override
public String apply(HttpHeaders headers) {
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set(SellerSpriteHeaders.SECRET_KEY, properties.getSecretKey());
    String requestId = idGenerator.nextId();
    headers.set(SellerSpriteHeaders.REQUEST_ID, requestId);
    return requestId;
}
```

```java
public <T> T post(String path, Object request,
        ParameterizedTypeReference<SellerSpriteResponse<T>> responseType) {
    return execute(HttpMethod.POST, path, requestId, () -> restClient.post()
            .uri(path)
            .headers(headers -> requestId.set(authStrategy.apply(headers)))
            .body(request)
            .retrieve()
            .body(responseType));
}
```

- [x] **Step 4: Add explicit error tests and implementation**

Test `ERROR_PARAM`, `ERROR_SECRET_KEY`, `ERROR_SECRET_KEY_OVERDUE`, `ERROR_VISIT_MAX`, HTTP 500, timeout, empty body and malformed JSON. Each test must fail for the intended missing behavior before implementation.

- [x] **Step 5: Run GREEN**

Expected: every Client test passes with no secret values in captured logs.

### Task 4: Lock the 45-operation contract

**Files:**
- Test: `sellersprite-api/src/test/java/com/yuanbaomao/sellersprite/api/client/SellerSpriteOperationTest.java`
- Create: `sellersprite-api/src/main/java/com/yuanbaomao/sellersprite/api/client/SellerSpriteOperation.java`
- Create: `sellersprite-api/src/main/java/com/yuanbaomao/sellersprite/api/client/SellerSpriteDomain.java`

**Interfaces:**
- Produces: enum values with domain, `HttpMethod`, path and Chinese description

- [x] **Step 1: Write the failing inventory test**

```java
@Test
void shouldDeclareExactlyFortyFiveUniqueOperations() {
    List<SellerSpriteOperation> operations = List.of(SellerSpriteOperation.values());
    assertThat(operations).hasSize(45);
    assertThat(operations)
            .extracting(operation -> operation.method() + " " + operation.path())
            .doesNotHaveDuplicates();
    assertThat(operations).extracting(SellerSpriteOperation::domain)
            .containsExactlyInAnyOrderElementsOf(EnumSet.allOf(SellerSpriteDomain.class));
}
```

- [x] **Step 2: Run RED, implement all matrix rows, then run GREEN**

Expected: first run fails because the enum is absent; second run passes only when every table row above is represented exactly once.

### Task 5: Implement shared strong types

**Files:**
- Create: `sellersprite-api/src/main/java/com/yuanbaomao/sellersprite/api/common/model/dto/SortOrder.java`
- Create: `sellersprite-api/src/main/java/com/yuanbaomao/sellersprite/api/common/model/vo/SellerSpritePageVo.java`
- Create: marketplace, month, badge, subcategory and reusable product models under `common/model`
- Test: matching JSON tests under `src/test/java/.../common/model`

**Interfaces:**
- Produces: stable shared request and response components used by domain models

- [x] **Step 1: Write failing official-example JSON tests**

```java
SellerSpritePageVo<ProductSummaryVo> page = objectMapper.readValue(json,
        new TypeReference<SellerSpritePageVo<ProductSummaryVo>>() {});
assertThat(page.getPage()).isEqualTo(1);
assertThat(page.getItems()).singleElement().extracting(ProductSummaryVo::getAsin)
        .isEqualTo("B0DGVP84B5");
```

- [x] **Step 2: Implement models with Chinese schema**

```java
@Data
@Schema(description = "SellerSprite 排序条件")
public class SortOrder {
    @Schema(description = "官方排序字段，例如 total_units 表示月销量")
    private String field;

    @Schema(description = "是否降序排列，默认 true")
    private Boolean desc = true;
}
```

- [x] **Step 3: Run GREEN and retain endpoint-local types for conflicts**

Expected: official examples deserialize without `Map` in public contracts.

### Task 6: Implement nine domain Services

**Files:**
- Create: each domain's `model/dto`, `model/vo`, Service and Service implementation
- Test: one contract test class and official JSON fixtures per domain

**Interfaces:**
- Consumes: `SellerSpriteClient`, `SellerSpriteOperation`, shared strong types
- Produces: every Java method in the complete operation matrix

- [x] **Step 1: Implement product and ASIN via RED-GREEN cycles**

```java
@Override
public CompetitorLookupVo lookupCompetitors(CompetitorLookupRequest request) {
    return client.post(SellerSpriteOperation.PRODUCT_COMPETITOR_LOOKUP, request,
            new ParameterizedTypeReference<SellerSpriteResponse<CompetitorLookupVo>>() {});
}
```

- [x] **Step 2: Implement keyword and traffic via RED-GREEN cycles**

Each of 14 methods must assert its operation enum, serialized request fields and typed response fixture before production code is added.

- [x] **Step 3: Implement all 14 market methods via RED-GREEN cycles**

Use endpoint-specific VO types for each distribution result; do not collapse different distributions into `Map<String, Object>`.

- [x] **Step 4: Implement review, trademark, tool and account via RED-GREEN cycles**

Verify seven operations, including two GET trademark operations, two POST trademark operations, OCR POST, review POST and visits GET.

- [x] **Step 5: Run all domain tests**

```powershell
mvn -pl sellersprite-api test --no-transfer-progress
```

Expected: all 45 operation contracts and official fixture tests pass.

### Task 7: Implement classified Controllers

**Files:**
- Create: nine `*Controller.java` files under domain controller packages
- Test: nine matching `*ControllerTest.java` MockMvc tests

**Interfaces:**
- Consumes: domain Service interfaces
- Produces: `/api/sellersprite/{domain}/**` routes returning `Result<T>`

- [x] **Step 1: Write failing controller tests**

```java
mockMvc.perform(post("/api/sellersprite/products/competitors")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"marketplace\":\"US\",\"page\":1,\"size\":50}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

verify(productService).lookupCompetitors(argThat(request -> "US".equals(request.getMarketplace())));
```

- [x] **Step 2: Implement thin controllers**

```java
@Tag(name = "SellerSprite 产品分析", description = "查竞品、选产品和产品类目接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sellersprite/products")
public class ProductController {
    private final ProductService productService;

    @Operation(summary = "查竞品")
    @PostMapping("/competitors")
    public Result<CompetitorLookupVo> lookupCompetitors(
            @Valid @RequestBody CompetitorLookupRequest request) {
        return Result.success(productService.lookupCompetitors(request));
    }
}
```

- [x] **Step 3: Verify security boundaries**

Search Controller DTOs for `secretKey`, `baseUrl`, `HttpHeaders` and `RestClient`; expected result is zero. Run unauthenticated MockMvc coverage against existing interceptor behavior.

### Task 8: Documentation, verification and commit

**Files:**
- Modify: `README.md`
- Modify: `config/application.yml`
- Modify: `sql/schema.sql`
- Create: `vibecodingdoc/2026-07-10/add-sellersprite-open-api.md`
- Modify: `vibecodingdoc/INDEX.md`

- [x] **Step 1: Document configuration and routes**

Include environment variables, nine route groups, Swagger URL, database initialization and the statement that default auth follows official `secret-key` plus unique request ID.

- [x] **Step 2: Run full verification**

```powershell
mvn test --no-transfer-progress
mvn package -DskipTests --no-transfer-progress
```

Expected: both commands exit 0.

- [x] **Step 3: Run static audits**

```powershell
rg -n "scaffold|Scaffold|SCAFFOLD" . -g '!target/**' -g '!.git/**' -g '!openspec/**' -g '!vibecodingdoc/**' -g '!.codex/**'
rg -n "secret-key\s*[:=]\s*[^$<{]" . -g '!target/**' -g '!.git/**' -g '!config/application-local.yml'
rg -n "锟|闁|閸|鐟|閳|�" . -g '*.java' -g '*.sql' -g '*.yml' -g '*.md' -g '!target/**'
```

Expected: old-name scan only reports `yuanbaomao-scaffold-parent`; secret and mojibake scans report no defects.

- [x] **Step 4: Validate OpenSpec and audit all requirements**

```powershell
openspec validate add-sellersprite-open-api
openspec status --change add-sellersprite-open-api
```

Expected: change valid and every implementation task checked.

- [x] **Step 5: Commit verified work**

```powershell
git add .
git commit -m "feat: integrate SellerSprite Open API"
```

Expected: commit succeeds and ignored local configuration, OpenSpec and `vibecodingdoc` are not staged.

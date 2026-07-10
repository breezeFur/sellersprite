"""Generate SellerSprite domain Services, implementations and Controllers."""

from __future__ import annotations

import re
from dataclasses import dataclass

import generate_sellersprite_contracts as contract_generator


BASE_PACKAGE = contract_generator.BASE_PACKAGE
GENERATED_NOTICE = contract_generator.GENERATED_NOTICE
JAVA_ROOT = contract_generator.JAVA_ROOT


METHOD_NAMES = {
    "ACCOUNT_VISITS": "getVisits",
    "PRODUCT_COMPETITOR_LOOKUP": "lookupCompetitors",
    "PRODUCT_RESEARCH": "researchProducts",
    "PRODUCT_NODE": "listProductNodes",
    "ASIN_DETAIL": "getAsinDetail",
    "ASIN_COUPON_TREND": "getCouponTrend",
    "ASIN_WITH_COUPON_TREND": "getAsinWithCouponTrend",
    "ASIN_SALES_TREND": "getSalesTrend",
    "ASIN_SALES_PREDICTION": "predictAsinSales",
    "BSR_SALES_PREDICTION": "predictBsrSales",
    "ASIN_KEEPA_TREND": "getKeepaTrend",
    "KEYWORD_RESEARCH": "researchKeywords",
    "KEYWORD_RESEARCH_TRENDS": "getKeywordResearchTrends",
    "KEYWORD_MINER": "mineKeywords",
    "KEYWORD_TRAFFIC_EXTEND": "extendTrafficKeywords",
    "ABA_RESEARCH_WEEKLY": "researchAbaWeekly",
    "ABA_RESEARCH_MONTHLY": "researchAbaMonthly",
    "ABA_RESEARCH_TRENDS": "getAbaKeywordTrends",
    "GOOGLE_TRENDS": "getGoogleTrends",
    "KEYWORD_ORDER": "reverseOrderKeywords",
    "TRAFFIC_KEYWORD": "reverseKeywords",
    "TRAFFIC_LISTING_PAGE": "listRelatedTraffic",
    "TRAFFIC_KEYWORD_STAT": "getKeywordStats",
    "TRAFFIC_LISTING_STAT": "getListingStats",
    "TRAFFIC_SOURCE": "getTrafficSources",
    "MARKET_RESEARCH": "researchMarkets",
    "MARKET_STATISTICS": "getMarketStatistics",
    "MARKET_GOODS": "getGoodsConcentration",
    "MARKET_BRAND": "getBrandConcentration",
    "MARKET_SELLER_LOCATION": "getSellerLocationDistribution",
    "MARKET_SELLER": "getSellerConcentration",
    "MARKET_SELLER_TYPE": "getSellerTypeDistribution",
    "MARKET_PERFORMANCE": "getDemandTrend",
    "MARKET_SHELF_TIME": "getShelfTimeDistribution",
    "MARKET_SHELF_TREND": "getShelfTrendDistribution",
    "MARKET_RATINGS": "getRatingsDistribution",
    "MARKET_RATING": "getRatingDistribution",
    "MARKET_PRICE": "getPriceDistribution",
    "MARKET_EBC": "getEbcDistribution",
    "REVIEW_LIST": "listReviews",
    "OCR": "recognizeImageText",
    "GLOBAL_BRAND_RANGE": "getBrandRange",
    "GLOBAL_BRAND_DETAIL": "getBrandDetail",
    "GLOBAL_BRAND_LIST": "listBrands",
    "GLOBAL_BRAND_STATS": "getBrandStats",
}


CONTROLLER_PATHS = {
    "ACCOUNT_VISITS": "/visits",
    "PRODUCT_COMPETITOR_LOOKUP": "/competitors",
    "PRODUCT_RESEARCH": "/research",
    "PRODUCT_NODE": "/nodes",
    "ASIN_DETAIL": "/detail",
    "ASIN_COUPON_TREND": "/coupon-trend",
    "ASIN_WITH_COUPON_TREND": "/with-coupon-trend",
    "ASIN_SALES_TREND": "/sales-trend",
    "ASIN_SALES_PREDICTION": "/sales-prediction",
    "BSR_SALES_PREDICTION": "/bsr-sales-prediction",
    "ASIN_KEEPA_TREND": "/keepa",
    "KEYWORD_RESEARCH": "/research",
    "KEYWORD_RESEARCH_TRENDS": "/research/trends",
    "KEYWORD_MINER": "/mine",
    "KEYWORD_TRAFFIC_EXTEND": "/traffic/extend",
    "ABA_RESEARCH_WEEKLY": "/aba/weekly",
    "ABA_RESEARCH_MONTHLY": "/aba/monthly",
    "ABA_RESEARCH_TRENDS": "/aba/trends",
    "GOOGLE_TRENDS": "/google-trends",
    "KEYWORD_ORDER": "/order/reverse",
    "TRAFFIC_KEYWORD": "/keywords/reverse",
    "TRAFFIC_LISTING_PAGE": "/related",
    "TRAFFIC_KEYWORD_STAT": "/keywords/stats",
    "TRAFFIC_LISTING_STAT": "/listings/stats",
    "TRAFFIC_SOURCE": "/sources",
    "MARKET_RESEARCH": "/research",
    "MARKET_STATISTICS": "/statistics",
    "MARKET_GOODS": "/goods",
    "MARKET_BRAND": "/brands",
    "MARKET_SELLER_LOCATION": "/sellers/locations",
    "MARKET_SELLER": "/sellers",
    "MARKET_SELLER_TYPE": "/sellers/types",
    "MARKET_PERFORMANCE": "/demand-trend",
    "MARKET_SHELF_TIME": "/shelf-times",
    "MARKET_SHELF_TREND": "/shelf-trends",
    "MARKET_RATINGS": "/ratings",
    "MARKET_RATING": "/rating",
    "MARKET_PRICE": "/prices",
    "MARKET_EBC": "/ebc",
    "REVIEW_LIST": "/search",
    "OCR": "/ocr",
    "GLOBAL_BRAND_RANGE": "/range",
    "GLOBAL_BRAND_DETAIL": "/detail",
    "GLOBAL_BRAND_LIST": "/search",
    "GLOBAL_BRAND_STATS": "/stats",
}


DOMAIN_INFO = {
    "account": ("Account", "账户次数", "/api/sellersprite/account"),
    "product": ("Product", "产品分析", "/api/sellersprite/products"),
    "asin": ("Asin", "ASIN 分析", "/api/sellersprite/asins"),
    "keyword": ("Keyword", "关键词研究", "/api/sellersprite/keywords"),
    "traffic": ("Traffic", "流量分析", "/api/sellersprite/traffic"),
    "market": ("Market", "市场分析", "/api/sellersprite/markets"),
    "review": ("Review", "评论分析", "/api/sellersprite/reviews"),
    "trademark": ("Trademark", "全球商标", "/api/sellersprite/trademarks"),
    "tool": ("Tool", "数据工具", "/api/sellersprite/tools"),
}


MULTIPART_OPERATIONS = {"OCR", "GLOBAL_BRAND_LIST", "GLOBAL_BRAND_STATS"}


@dataclass(frozen=True)
class LayerEndpoint:
    domain: str
    base_name: str
    operation: str
    name: str
    method: str
    external_path: str
    response_shape: str
    has_request: bool

    @property
    def method_name(self) -> str:
        return METHOD_NAMES[self.operation]

    @property
    def controller_path(self) -> str:
        return CONTROLLER_PATHS[self.operation]

    @property
    def request_class(self) -> str:
        return self.base_name + "Request"

    @property
    def response_class(self) -> str:
        return self.base_name + "Vo"

    @property
    def response_type(self) -> str:
        if self.response_shape == "list":
            return f"List<{self.response_class}>"
        return self.response_class

    @property
    def path_fields(self) -> list[str]:
        return re.findall(r"\{([^}]+)}", self.external_path)


def build_endpoints() -> list[LayerEndpoint]:
    session = contract_generator.requests.Session()
    contracts = [contract_generator.fetch_contract(session, endpoint)
                 for endpoint in contract_generator.ENDPOINTS]
    result = [
        LayerEndpoint(
            contract.endpoint.domain,
            contract.endpoint.base_name,
            contract.endpoint.operation,
            contract.name,
            contract.method,
            contract.path,
            contract.endpoint.response_shape,
            bool(contract.request_nodes),
        )
        for contract in contracts
    ]
    result.append(LayerEndpoint("account", "Visits", "ACCOUNT_VISITS", "可用次数查询",
                                "GET", "/v1/visits", "object", False))
    return result


def write_java(package_name: str, class_name: str, content: str) -> None:
    directory = JAVA_ROOT / contract_generator.Path(package_name.replace(".", "/"))
    directory.mkdir(parents=True, exist_ok=True)
    (directory / f"{class_name}.java").write_text(content, encoding="utf-8", newline="\n")


def request_fqcn(endpoint: LayerEndpoint) -> str:
    return f"{BASE_PACKAGE}.{endpoint.domain}.model.dto.{endpoint.request_class}"


def response_fqcn(endpoint: LayerEndpoint) -> str:
    return f"{BASE_PACKAGE}.{endpoint.domain}.model.vo.{endpoint.response_class}"


def render_visits_vo() -> str:
    return f"""{GENERATED_NOTICE}
package {BASE_PACKAGE}.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;

/**
 * SellerSprite 可用次数响应。
 *
 * <p>官方概览只说明该接口返回当前月份各模块可用次数，未公开 data 子字段结构，
 * 因此保留完整 JSON 节点而不猜造字段。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "SellerSprite 当前月份各模块可用次数")
public class VisitsVo {{

    @Schema(description = "官方未公开固定结构的各模块可用次数 JSON")
    private JsonNode details;
}}
"""


def render_service_interface(domain: str, endpoints: list[LayerEndpoint]) -> str:
    domain_class, domain_label, _ = DOMAIN_INFO[domain]
    package_name = f"{BASE_PACKAGE}.{domain}.service"
    imports: set[str] = set()
    if any(endpoint.response_shape == "list" for endpoint in endpoints):
        imports.add("java.util.List")
    for endpoint in endpoints:
        if endpoint.has_request:
            imports.add(request_fqcn(endpoint))
        imports.add(response_fqcn(endpoint))
    lines = [GENERATED_NOTICE, f"package {package_name};", ""]
    lines.extend(f"import {name};" for name in sorted(imports))
    lines.extend(["", "/**", f" * SellerSprite {domain_label}接口封装。", " */",
                  f"public interface {domain_class}Service {{", ""])
    for endpoint in endpoints:
        parameter = f"{endpoint.request_class} request" if endpoint.has_request else ""
        lines.extend(["    /**", f"     * {endpoint.name}。", "     *",
                      f"     * <p>调用 SellerSprite 官方 {endpoint.method} "
                      f"{endpoint.external_path}，认证、超时和错误转换由统一 Client 处理。</p>"])
        if endpoint.has_request:
            lines.append(f"     * @param request {endpoint.name}的强类型请求参数")
        lines.extend([f"     * @return {endpoint.name}的强类型响应数据", "     */",
                      f"    {endpoint.response_type} {endpoint.method_name}({parameter});", ""])
    lines.append("}")
    return "\n".join(lines) + "\n"


def getter(field_name: str) -> str:
    java_name = contract_generator.java_field_name(field_name)
    return "get" + java_name[0].upper() + java_name[1:] + "()"


def render_get_body(endpoint: LayerEndpoint) -> list[str]:
    if endpoint.has_request:
        if endpoint.path_fields:
            entries = ", ".join(
                f'"{field}", SellerSpriteRequestEncoder.pathValue(request.{getter(field)})'
                for field in endpoint.path_fields
            )
            path_map = f"Map.of({entries})"
            excluded = ", ".join(f'"{contract_generator.java_field_name(field)}"'
                                 for field in endpoint.path_fields)
            excluded_set = f"Set.of({excluded})"
        else:
            path_map = "Map.of()"
            excluded_set = "Set.of()"
        query = f"SellerSpriteRequestEncoder.toQuery(request, {excluded_set})"
    else:
        path_map = "Map.of()"
        query = "SellerSpriteRequestEncoder.toQuery(null, Set.of())"
    return [
        f"        return client.get(SellerSpriteOperation.{endpoint.operation},",
        f"                {path_map}, {query},",
        f"                new ParameterizedTypeReference<SellerSpriteResponse<{endpoint.response_type}>>() {{",
        "                });",
    ]


def render_post_body(endpoint: LayerEndpoint) -> list[str]:
    call = "postMultipart" if endpoint.operation in MULTIPART_OPERATIONS else "post"
    body = "SellerSpriteRequestEncoder.toMultipart(request)" if call == "postMultipart" else "request"
    return [
        f"        return client.{call}(SellerSpriteOperation.{endpoint.operation}, {body},",
        f"                new ParameterizedTypeReference<SellerSpriteResponse<{endpoint.response_type}>>() {{",
        "                });",
    ]


def render_account_get_body() -> list[str]:
    return [
        "        JsonNode details = client.get(SellerSpriteOperation.ACCOUNT_VISITS, Map.of(),",
        "                SellerSpriteRequestEncoder.toQuery(null, Set.of()),",
        "                new ParameterizedTypeReference<SellerSpriteResponse<JsonNode>>() {",
        "                });",
        "        return new VisitsVo(details);",
    ]


def render_service_impl(domain: str, endpoints: list[LayerEndpoint]) -> str:
    domain_class, domain_label, _ = DOMAIN_INFO[domain]
    package_name = f"{BASE_PACKAGE}.{domain}.service.impl"
    imports: set[str] = {
        "java.util.Map", "java.util.Set",
        "org.springframework.core.ParameterizedTypeReference",
        "org.springframework.stereotype.Service",
        f"{BASE_PACKAGE}.client.SellerSpriteClient",
        f"{BASE_PACKAGE}.client.SellerSpriteOperation",
        f"{BASE_PACKAGE}.client.SellerSpriteRequestEncoder",
        f"{BASE_PACKAGE}.client.SellerSpriteResponse",
        f"{BASE_PACKAGE}.{domain}.service.{domain_class}Service",
        "lombok.RequiredArgsConstructor",
    }
    if any(endpoint.response_shape == "list" for endpoint in endpoints):
        imports.add("java.util.List")
    if domain == "account":
        imports.add("tools.jackson.databind.JsonNode")
    for endpoint in endpoints:
        if endpoint.has_request:
            imports.add(request_fqcn(endpoint))
        imports.add(response_fqcn(endpoint))
    lines = [GENERATED_NOTICE, f"package {package_name};", ""]
    lines.extend(f"import {name};" for name in sorted(imports))
    lines.extend([
        "", "/**", f" * SellerSprite {domain_label}接口实现，所有请求统一委派给 SellerSpriteClient。", " */",
        "@Service", "@RequiredArgsConstructor",
        f"public class {domain_class}ServiceImpl implements {domain_class}Service {{", "",
        "    private final SellerSpriteClient client;", "",
    ])
    for endpoint in endpoints:
        parameter = f"{endpoint.request_class} request" if endpoint.has_request else ""
        lines.extend([
            "    @Override",
            f"    public {endpoint.response_type} {endpoint.method_name}({parameter}) {{",
        ])
        if domain == "account":
            lines.extend(render_account_get_body())
        elif endpoint.method == "GET":
            lines.extend(render_get_body(endpoint))
        else:
            lines.extend(render_post_body(endpoint))
        lines.extend(["    }", ""])
    lines.append("}")
    return "\n".join(lines) + "\n"


def render_controller(domain: str, endpoints: list[LayerEndpoint]) -> str:
    domain_class, domain_label, base_path = DOMAIN_INFO[domain]
    package_name = f"{BASE_PACKAGE}.{domain}.controller"
    imports: set[str] = {
        "com.yuanbaomao.base.result.Result",
        "io.swagger.v3.oas.annotations.Operation",
        "io.swagger.v3.oas.annotations.tags.Tag",
        "lombok.RequiredArgsConstructor",
        "org.springframework.web.bind.annotation.GetMapping",
        "org.springframework.web.bind.annotation.PostMapping",
        "org.springframework.web.bind.annotation.RequestMapping",
        "org.springframework.web.bind.annotation.RestController",
        f"{BASE_PACKAGE}.{domain}.service.{domain_class}Service",
    }
    if any(endpoint.has_request for endpoint in endpoints):
        imports.add("jakarta.validation.Valid")
    if any(endpoint.response_shape == "list" for endpoint in endpoints):
        imports.add("java.util.List")
    if any(endpoint.has_request and endpoint.method == "GET" for endpoint in endpoints):
        imports.add("org.springframework.web.bind.annotation.ModelAttribute")
    if any(endpoint.has_request and endpoint.method == "POST" and endpoint.operation not in MULTIPART_OPERATIONS
           for endpoint in endpoints):
        imports.add("org.springframework.web.bind.annotation.RequestBody")
    if any(endpoint.operation in MULTIPART_OPERATIONS for endpoint in endpoints):
        imports.add("org.springframework.http.MediaType")
        imports.add("org.springframework.web.bind.annotation.ModelAttribute")
    for endpoint in endpoints:
        if endpoint.has_request:
            imports.add(request_fqcn(endpoint))
        imports.add(response_fqcn(endpoint))
    lines = [GENERATED_NOTICE, f"package {package_name};", ""]
    lines.extend(f"import {name};" for name in sorted(imports))
    lines.extend([
        "", f'@Tag(name = "SellerSprite {domain_label}", description = "SellerSprite {domain_label}分类接口")',
        "@RestController", "@RequiredArgsConstructor", f'@RequestMapping("{base_path}")',
        f"public class {domain_class}Controller {{", "",
        f"    private final {domain_class}Service {domain}Service;", "",
    ])
    for endpoint in endpoints:
        lines.append(f'    @Operation(summary = "{endpoint.name}", description = "通过统一 SellerSpriteClient 调用 {endpoint.external_path}")')
        if endpoint.method == "GET":
            lines.append(f'    @GetMapping("{endpoint.controller_path}")')
        elif endpoint.operation in MULTIPART_OPERATIONS:
            lines.append(f'    @PostMapping(value = "{endpoint.controller_path}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)')
        else:
            lines.append(f'    @PostMapping("{endpoint.controller_path}")')
        if not endpoint.has_request:
            parameter = ""
        elif endpoint.method == "GET" or endpoint.operation in MULTIPART_OPERATIONS:
            parameter = f"@Valid @ModelAttribute {endpoint.request_class} request"
        else:
            parameter = f"@Valid @RequestBody {endpoint.request_class} request"
        call_arg = "request" if endpoint.has_request else ""
        lines.extend([
            f"    public Result<{endpoint.response_type}> {endpoint.method_name}({parameter}) {{",
            f"        return Result.success({domain}Service.{endpoint.method_name}({call_arg}));",
            "    }",
            "",
        ])
    lines.append("}")
    return "\n".join(lines) + "\n"


def render_endpoint_index() -> str:
    service_types = []
    controller_types = []
    for domain, (domain_class, _, _) in DOMAIN_INFO.items():
        service_types.append(f"{BASE_PACKAGE}.{domain}.service.{domain_class}Service.class")
        controller_types.append(f"{BASE_PACKAGE}.{domain}.controller.{domain_class}Controller.class")
    services = ",\n            ".join(service_types)
    controllers = ",\n            ".join(controller_types)
    return f"""{GENERATED_NOTICE}
package {BASE_PACKAGE}.client;

import java.util.List;

/**
 * SellerSprite 九域 Service 与 Controller 契约索引。
 */
public final class GeneratedSellerSpriteEndpointIndex {{

    private static final List<Class<?>> SERVICE_TYPES = List.of(
            {services});

    private static final List<Class<?>> CONTROLLER_TYPES = List.of(
            {controllers});

    private GeneratedSellerSpriteEndpointIndex() {{
    }}

    public static List<Class<?>> getServiceTypes() {{
        return SERVICE_TYPES;
    }}

    public static List<Class<?>> getControllerTypes() {{
        return CONTROLLER_TYPES;
    }}

    public static int getOperationCount() {{
        return 45;
    }}
}}
"""


def main() -> None:
    endpoints = build_endpoints()
    write_java(f"{BASE_PACKAGE}.account.model.vo", "VisitsVo", render_visits_vo())
    for domain, (domain_class, _, _) in DOMAIN_INFO.items():
        domain_endpoints = [endpoint for endpoint in endpoints if endpoint.domain == domain]
        write_java(f"{BASE_PACKAGE}.{domain}.service", f"{domain_class}Service",
                   render_service_interface(domain, domain_endpoints))
        write_java(f"{BASE_PACKAGE}.{domain}.service.impl", f"{domain_class}ServiceImpl",
                   render_service_impl(domain, domain_endpoints))
        write_java(f"{BASE_PACKAGE}.{domain}.controller", f"{domain_class}Controller",
                   render_controller(domain, domain_endpoints))
    write_java(f"{BASE_PACKAGE}.client", "GeneratedSellerSpriteEndpointIndex", render_endpoint_index())
    print(f"Generated {len(DOMAIN_INFO)} services, {len(DOMAIN_INFO)} implementations, "
          f"{len(DOMAIN_INFO)} controllers and {len(endpoints)} explicit methods.")


if __name__ == "__main__":
    main()

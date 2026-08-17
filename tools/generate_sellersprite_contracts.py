"""Generate typed SellerSprite request/response models from the official HTML tables.

The official site does not publish an OpenAPI schema. This script snapshots the
tables and generates Java model drafts while preserving every documented field
and Chinese description. Run from the project root with Python 3, requests and
BeautifulSoup installed.
"""

from __future__ import annotations

import json
import re
from dataclasses import dataclass, field
from pathlib import Path
from typing import Iterable

import requests
from bs4 import BeautifulSoup


ROOT = Path(__file__).resolve().parents[1]
JAVA_ROOT = ROOT / "sellersprite-api/src/main/java"
FRONTEND_CONTRACT_PATH = (
    ROOT / "sellersprite-web/src/features/sellersprite/model/officialOperationContracts.generated.ts"
)
BASE_PACKAGE = "cyou.yuanbaomao.sellersprite.api"
DOC_BASE_URL = "https://open.sellersprite.com/api"
CAPTURED_AT = "2026-07-14"
MODEL_GENERATED_AT = "2026-07-10"
GENERATED_NOTICE = f"// Generated from SellerSprite official documentation on {MODEL_GENERATED_AT}."


@dataclass(frozen=True)
class Endpoint:
    doc_id: int
    domain: str
    base_name: str
    operation: str
    response_shape: str
    response_item_override: str | None = None


ENDPOINTS = [
    Endpoint(1, "product", "CompetitorLookup", "PRODUCT_COMPETITOR_LOOKUP", "page", "ProductSummaryVo"),
    Endpoint(2, "product", "ProductResearch", "PRODUCT_RESEARCH", "page", "ProductSummaryVo"),
    Endpoint(9, "product", "ProductNode", "PRODUCT_NODE", "list"),
    Endpoint(3, "asin", "AsinDetail", "ASIN_DETAIL", "object"),
    Endpoint(56, "asin", "AsinCouponTrend", "ASIN_COUPON_TREND", "list"),
    Endpoint(57, "asin", "AsinWithCouponTrend", "ASIN_WITH_COUPON_TREND", "object"),
    Endpoint(61, "asin", "AsinSalesTrend", "ASIN_SALES_TREND", "object"),
    Endpoint(27, "asin", "AsinSalesPrediction", "ASIN_SALES_PREDICTION", "object"),
    Endpoint(26, "asin", "BsrSalesPrediction", "BSR_SALES_PREDICTION", "object"),
    Endpoint(22, "asin", "KeepaTrend", "ASIN_KEEPA_TREND", "object"),
    Endpoint(10, "keyword", "KeywordResearch", "KEYWORD_RESEARCH", "page"),
    Endpoint(11, "keyword", "KeywordResearchTrend", "KEYWORD_RESEARCH_TRENDS", "list"),
    Endpoint(6, "keyword", "KeywordMiner", "KEYWORD_MINER", "page"),
    Endpoint(46, "keyword", "TrafficKeywordExtend", "KEYWORD_TRAFFIC_EXTEND", "page"),
    Endpoint(19, "keyword", "AbaWeeklyResearch", "ABA_RESEARCH_WEEKLY", "page"),
    Endpoint(20, "keyword", "AbaMonthlyResearch", "ABA_RESEARCH_MONTHLY", "page"),
    Endpoint(60, "keyword", "AbaKeywordTrend", "ABA_RESEARCH_TRENDS", "list"),
    Endpoint(12, "keyword", "GoogleTrend", "GOOGLE_TRENDS", "object"),
    Endpoint(24, "keyword", "KeywordOrder", "KEYWORD_ORDER", "page"),
    Endpoint(14, "traffic", "TrafficKeyword", "TRAFFIC_KEYWORD", "object"),
    Endpoint(16, "traffic", "RelatedTraffic", "TRAFFIC_LISTING_PAGE", "page", "ProductSummaryVo"),
    Endpoint(13, "traffic", "TrafficKeywordStat", "TRAFFIC_KEYWORD_STAT", "object"),
    Endpoint(15, "traffic", "TrafficListingStat", "TRAFFIC_LISTING_STAT", "object"),
    Endpoint(17, "traffic", "TrafficSource", "TRAFFIC_SOURCE", "page"),
    Endpoint(29, "market", "MarketResearch", "MARKET_RESEARCH", "page"),
    Endpoint(30, "market", "MarketStatistics", "MARKET_STATISTICS", "object"),
    Endpoint(31, "market", "MarketGoodsConcentration", "MARKET_GOODS", "list"),
    Endpoint(32, "market", "MarketBrandConcentration", "MARKET_BRAND", "list"),
    Endpoint(35, "market", "MarketSellerLocation", "MARKET_SELLER_LOCATION", "list"),
    Endpoint(33, "market", "MarketSellerConcentration", "MARKET_SELLER", "list"),
    Endpoint(34, "market", "MarketSellerType", "MARKET_SELLER_TYPE", "list"),
    Endpoint(36, "market", "MarketDemandTrend", "MARKET_PERFORMANCE", "object"),
    Endpoint(37, "market", "MarketShelfTime", "MARKET_SHELF_TIME", "list"),
    Endpoint(38, "market", "MarketShelfTrend", "MARKET_SHELF_TREND", "list"),
    Endpoint(39, "market", "MarketRatingsDistribution", "MARKET_RATINGS", "list"),
    Endpoint(40, "market", "MarketRatingDistribution", "MARKET_RATING", "list"),
    Endpoint(41, "market", "MarketPriceDistribution", "MARKET_PRICE", "list"),
    Endpoint(42, "market", "MarketEbcDistribution", "MARKET_EBC", "list"),
    Endpoint(25, "review", "ReviewList", "REVIEW_LIST", "page"),
    Endpoint(44, "tool", "Ocr", "OCR", "object"),
    Endpoint(50, "trademark", "TrademarkRange", "GLOBAL_BRAND_RANGE", "list"),
    Endpoint(49, "trademark", "TrademarkDetail", "GLOBAL_BRAND_DETAIL", "object"),
    Endpoint(48, "trademark", "TrademarkList", "GLOBAL_BRAND_LIST", "page"),
    Endpoint(47, "trademark", "TrademarkStats", "GLOBAL_BRAND_STATS", "object"),
]

# 官方字段表与实际响应示例不一致时，以线上响应字段为准。
RESPONSE_FIELD_OVERRIDES = {
    "MARKET_SELLER": {
        "name": "sellerName",
        "asinSet": "asins",
    },
}

RESPONSE_TYPE_OVERRIDES = {
    "MARKET_PERFORMANCE": {
        "searchToPurchaseRatio": "BigDecimal",
        "avgReturnRatio": "BigDecimal",
    },
}

# 官方文档中的可选字段若会导致线上接口返回空数据，则从实际请求契约中排除。
REQUEST_FIELD_EXCLUSIONS = {
    "MARKET_PERFORMANCE": {"newProduct"},
}


@dataclass
class FieldNode:
    raw_name: str
    java_name: str
    documented_type: str
    required: bool
    display_name: str
    detail: str
    depth: int
    children: list["FieldNode"] = field(default_factory=list)


@dataclass
class Contract:
    endpoint: Endpoint
    name: str
    method: str
    path: str
    request_rows: list[dict]
    response_rows: list[dict]
    request_nodes: list[FieldNode]
    response_nodes: list[FieldNode]
    response_example: dict | list | None


JAVA_RESERVED = {
    "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
    "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
    "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
    "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp",
    "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void",
    "volatile", "while", "record", "sealed", "permits", "yield", "var",
}


def clean_text(value: str) -> str:
    return " ".join(value.split()).replace("*/", "* /")


def java_string(value: str) -> str:
    return (value.replace("\\", "\\\\")
            .replace('"', '\\"')
            .replace("\r", "\\r")
            .replace("\n", "\\n"))


def java_field_name(raw_name: str) -> str:
    value = raw_name.replace("└", "").strip()
    parts = [part for part in re.split(r"[^0-9A-Za-z]+", value) if part]
    if not parts:
        return "value"
    result = parts[0][0].lower() + parts[0][1:]
    result += "".join(part[0].upper() + part[1:] for part in parts[1:])
    if result[0].isdigit():
        result = "value" + result
    if result in JAVA_RESERVED:
        result += "Value"
    return result


def pascal(value: str) -> str:
    normalized = java_field_name(value)
    return normalized[0].upper() + normalized[1:]


def table_cells(table) -> list[list[str]]:
    return [
        [clean_text(cell.get_text(" ", strip=True)) for cell in row.select("th,td")]
        for row in table.select("tr")
    ]


def parse_nodes(rows: list[dict]) -> list[FieldNode]:
    roots: list[FieldNode] = []
    stack: dict[int, FieldNode] = {}
    children_by_parent: dict[int, dict[str, FieldNode]] = {}
    for row in rows:
        raw = row["field"]
        depth = raw.count("└")
        node = FieldNode(
            raw_name=raw.replace("└", "").strip(),
            java_name=java_field_name(raw),
            documented_type=row["type"],
            required=row.get("required", False),
            display_name=row["name"],
            detail=row["description"],
            depth=depth,
        )
        if depth == 0 or depth - 1 not in stack:
            target = roots
            parent_key = -1
        else:
            parent = stack[depth - 1]
            target = parent.children
            parent_key = id(parent)
        known = children_by_parent.setdefault(parent_key, {})
        if node.java_name in known:
            stack[depth] = known[node.java_name]
            continue
        known[node.java_name] = node
        target.append(node)
        stack[depth] = node
        for stale_depth in [key for key in stack if key > depth]:
            del stack[stale_depth]
    return roots


def fetch_contract(session: requests.Session, endpoint: Endpoint) -> Contract:
    response = session.get(f"{DOC_BASE_URL}/{endpoint.doc_id}", timeout=30)
    response.raise_for_status()
    response.encoding = "utf-8"
    soup = BeautifulSoup(response.text, "html.parser")
    page = soup.select_one(".page-body") or soup
    title = clean_text(page.select_one("h1").get_text(" ", strip=True))
    tables = page.select("table")
    metadata = table_cells(tables[0])
    flat_metadata = [item for row in metadata for item in row]
    method = flat_metadata[flat_metadata.index("Http Method") + 1]
    path_url = flat_metadata[flat_metadata.index("Http Request URL") + 1]
    path = path_url.replace("https://api.sellersprite.com", "")

    request_rows: list[dict] = []
    response_rows: list[dict] = []
    for table in tables[1:]:
        rows = table_cells(table)
        if not rows:
            continue
        header = rows[0]
        is_request = "是否必填" in header
        for cells in rows[1:]:
            if is_request and len(cells) >= 6:
                request_rows.append({
                    "field": cells[1], "type": cells[2], "required": cells[3] == "✓",
                    "name": cells[4], "description": cells[5],
                })
            elif not is_request and len(cells) >= 5:
                response_rows.append({
                    "field": cells[1], "type": cells[2], "required": False,
                    "name": cells[3], "description": cells[4],
                })

    excluded_request_fields = REQUEST_FIELD_EXCLUSIONS.get(endpoint.operation, set())
    request_rows = [
        row for row in request_rows
        if row["field"] not in excluded_request_fields
    ]

    overrides = RESPONSE_FIELD_OVERRIDES.get(endpoint.operation, {})
    type_overrides = RESPONSE_TYPE_OVERRIDES.get(endpoint.operation, {})
    for row in response_rows:
        row["field"] = overrides.get(row["field"], row["field"])
        row["type"] = type_overrides.get(row["field"], row["type"])

    # The URL is part of the authoritative request contract. A few official
    # pages omit a path placeholder from the request table (for example,
    # /v1/traffic/listing/stat/{marketplace}/{asin} documents asinList only).
    # Add the missing scalar so generated service code and DTOs stay callable.
    documented_fields = {
        re.sub(r"^[^A-Za-z0-9_]+", "", row["field"]).split(".", 1)[0]
        for row in request_rows
    }
    for placeholder in re.findall(r"\{([A-Za-z0-9_]+)}", path):
        if placeholder not in documented_fields:
            request_rows.insert(0, {
                "field": placeholder,
                "type": "String",
                "required": True,
                "name": f"{placeholder} 路径参数",
                "description": "由官方 Http Request URL 定义；官方参数表未单独列出",
            })
    response_example = None
    for pre in page.select("pre")[1:]:
        try:
            response_example = json.loads(pre.get_text("\n", strip=True))
        except json.JSONDecodeError:
            continue
    return Contract(endpoint, title, method, path, request_rows, response_rows,
                    parse_nodes(request_rows), parse_nodes(response_rows), response_example)


def field_description(contract_name: str, direction: str, node: FieldNode) -> str:
    parts = [f"{contract_name}{direction}参数：{node.display_name or node.raw_name}"]
    if node.detail:
        parts.append(node.detail)
    return clean_text("；".join(parts))


def nested_class_name(path: tuple[str, ...]) -> str:
    return "".join(pascal(part) for part in path) + "Vo"


def simple_java_type(node: FieldNode, direction: str) -> str:
    documented = node.documented_type.strip().lower()
    if direction == "请求" and node.java_name == "marketplace":
        return "SellerSpriteMarketplace"
    if direction == "请求" and node.java_name == "availableMonth":
        # The web/API boundary carries LISTING_DATE_* labels; the client resolves them
        # to the official numeric value immediately before the remote request.
        return "String"
    if direction == "响应" and node.java_name == "keywordCn":
        # ABA weekly/monthly tables label keywordCn as Integer, while both the
        # description and official examples return translated text.
        return "String"
    if documented in {"string", "char"}:
        return "String"
    if documented in {"integer", "int", "short"}:
        return "Integer"
    if documented in {"long", "date"}:
        return "Long"
    if documented in {"float", "double", "decimal", "bigdecimal", "number"}:
        return "BigDecimal"
    if documented in {"boolean", "bool"}:
        return "Boolean"
    if documented == "file" and direction == "请求":
        return "MultipartFile"
    if documented in {"list", "array"}:
        return "List<String>"
    if documented in {"jsonarray"}:
        return "List<JsonNode>"
    return "JsonNode"


def node_java_type(node: FieldNode, direction: str, path: tuple[str, ...]) -> str:
    if direction == "请求" and node.java_name == "order" and node.children:
        return "SortOrder"
    if direction == "响应" and node.java_name == "badge" and node.children:
        return "BadgeVo"
    if direction == "响应" and node.java_name == "subcategories" and node.children:
        return "List<SubcategoryVo>"
    if direction == "响应" and node.java_name == "variationList":
        return "List<VariationVo>"
    if direction == "响应" and "PairNumberDto" in node.detail:
        return "List<NumericTrendPointVo>"
    if direction == "响应" and "PairStrDto" in node.detail:
        return "List<StringTrendPointVo>"
    if direction == "响应" and "SubRankTrendDto" in node.detail:
        return "List<SubRankTrendVo>"
    if node.children and has_structured_children(node):
        child_type = nested_class_name(path + (node.java_name,))
        if node.documented_type.strip().lower() in {"list", "array", "jsonarray"}:
            return f"List<{child_type}>"
        return child_type
    return simple_java_type(node, direction)


def has_structured_children(node: FieldNode) -> bool:
    documented = node.documented_type.strip().lower()
    scalar_types = {
        "string", "char", "integer", "int", "short", "long", "date",
        "float", "double", "decimal", "bigdecimal", "number", "boolean", "bool", "file",
    }
    return documented not in scalar_types


def collect_imports(nodes: Iterable[FieldNode], direction: str) -> set[str]:
    imports = {
        "io.swagger.v3.oas.annotations.media.Schema",
        "lombok.Data",
    }
    if direction == "响应":
        imports.update({
            "com.fasterxml.jackson.annotation.JsonAnyGetter",
            "com.fasterxml.jackson.annotation.JsonAnySetter",
            "java.util.LinkedHashMap",
            "java.util.Map",
            "lombok.extern.slf4j.Slf4j",
            "tools.jackson.databind.JsonNode",
        })

    def visit(node: FieldNode, path: tuple[str, ...]) -> None:
        java_type = node_java_type(node, direction, path)
        if "BigDecimal" in java_type:
            imports.add("java.math.BigDecimal")
        if "List<" in java_type:
            imports.add("java.util.List")
        if "JsonNode" in java_type:
            imports.add("tools.jackson.databind.JsonNode")
        if "MultipartFile" in java_type:
            imports.add("org.springframework.web.multipart.MultipartFile")
        if "SellerSpriteMarketplace" in java_type:
            imports.add(f"{BASE_PACKAGE}.common.enums.SellerSpriteMarketplace")
        if "SortOrder" in java_type:
            imports.add(f"{BASE_PACKAGE}.common.model.dto.SortOrder")
        if "BadgeVo" in java_type:
            imports.add(f"{BASE_PACKAGE}.common.model.vo.BadgeVo")
        if "SubcategoryVo" in java_type:
            imports.add(f"{BASE_PACKAGE}.common.model.vo.SubcategoryVo")
        if "VariationVo" in java_type:
            imports.add(f"{BASE_PACKAGE}.common.model.vo.VariationVo")
        if "NumericTrendPointVo" in java_type:
            imports.add(f"{BASE_PACKAGE}.common.model.vo.NumericTrendPointVo")
        if "StringTrendPointVo" in java_type:
            imports.add(f"{BASE_PACKAGE}.common.model.vo.StringTrendPointVo")
        if "SubRankTrendVo" in java_type:
            imports.add(f"{BASE_PACKAGE}.common.model.vo.SubRankTrendVo")
        if java_field_name(node.raw_name) != node.raw_name:
            imports.add("com.fasterxml.jackson.annotation.JsonProperty")
        if direction == "请求" and node.required:
            if java_type == "String":
                imports.add("jakarta.validation.constraints.NotBlank")
            elif java_type.startswith("List<"):
                imports.add("jakarta.validation.constraints.NotEmpty")
            else:
                imports.add("jakarta.validation.constraints.NotNull")
        if direction == "请求" and node.java_name in {"page", "size"} and java_type == "Integer":
            imports.add("jakarta.validation.constraints.Min")
        maximum = request_maximum(node, java_type) if direction == "请求" else None
        if maximum is not None:
            annotation = "Size" if java_type.startswith("List<") else "Max"
            imports.add(f"jakarta.validation.constraints.{annotation}")
        for child in node.children:
            visit(child, path + (node.java_name,))

    for root in nodes:
        visit(root, ())
    return imports


def render_fields(contract_name: str, direction: str, nodes: list[FieldNode],
                  path: tuple[str, ...], indent: str) -> list[str]:
    lines: list[str] = []
    for node in nodes:
        description = field_description(contract_name, direction, node)
        java_type = node_java_type(node, direction, path)
        lines.append(f"{indent}/** {description} */")
        if java_field_name(node.raw_name) != node.raw_name:
            lines.append(f'{indent}@JsonProperty("{java_string(node.raw_name)}")')
        if direction == "请求" and node.required:
            if java_type == "String":
                lines.append(f"{indent}@NotBlank")
            elif java_type.startswith("List<"):
                lines.append(f"{indent}@NotEmpty")
            else:
                lines.append(f"{indent}@NotNull")
        if direction == "请求" and node.java_name in {"page", "size"} and java_type == "Integer":
            lines.append(f'{indent}@Min(value = 1, message = "{node.java_name} 不能小于 1")')
        maximum = request_maximum(node, java_type) if direction == "请求" else None
        if maximum is not None:
            if java_type.startswith("List<"):
                lines.append(f'{indent}@Size(max = {maximum}, message = "{node.java_name} 最多允许 {maximum} 项")')
            else:
                lines.append(f'{indent}@Max(value = {maximum}, message = "{node.java_name} 不能大于 {maximum}")')
        lines.append(f'{indent}@Schema(description = "{java_string(description)}")')
        initializer = request_default_initializer(node, java_type) if direction == "请求" else ""
        lines.append(f"{indent}private {java_type} {node.java_name}{initializer};")
        lines.append("")
    return lines


def request_default_initializer(node: FieldNode, java_type: str) -> str:
    """Return only defaults explicitly stated by the official field description."""
    if java_type != "Integer" or node.java_name not in {"page", "size"}:
        return ""
    text = f"{node.display_name} {node.detail}"
    match = re.search(r"(?:默认|default|固定)\s*[：:]?\s*(\d+)", text, re.IGNORECASE)
    return f" = {match.group(1)}" if match else ""


def render_additional_properties(indent: str) -> list[str]:
    return [
        f"{indent}/** 官方响应中未建模字段的原始值。 */",
        f'{indent}@Schema(description = "官方响应未建模字段", hidden = true)',
        f"{indent}private final Map<String, JsonNode> additionalProperties = new LinkedHashMap<>();",
        "",
        f"{indent}@JsonAnySetter",
        f"{indent}public void putAdditionalProperty(String name, JsonNode value) {{",
        f'{indent}    log.warn("SellerSprite 响应包含未建模字段 modelType={{}}, fieldName={{}}, fieldValue={{}}",',
        f"{indent}            getClass().getName(), name, value);",
        f"{indent}    additionalProperties.put(name, value);",
        f"{indent}}}",
        "",
        f"{indent}@JsonAnyGetter",
        f"{indent}public Map<String, JsonNode> getAdditionalProperties() {{",
        f"{indent}    return additionalProperties;",
        f"{indent}}}",
        "",
    ]


def request_maximum(node: FieldNode, java_type: str) -> int | None:
    """Parse explicit maximums for page sizes and collection cardinality."""
    is_collection = java_type.startswith("List<")
    if not is_collection and not (java_type == "Integer" and node.java_name == "size"):
        return None
    text = f"{node.display_name} {node.detail}"
    match = re.search(r"(?:最大|最多|max(?:imum)?)[^0-9]{0,12}(\d+)", text, re.IGNORECASE)
    return int(match.group(1)) if match else None


def collect_nested(nodes: list[FieldNode], direction: str, path: tuple[str, ...] = ()) -> list[tuple[str, FieldNode]]:
    nested: list[tuple[str, FieldNode]] = []
    for node in nodes:
        special = ((direction == "请求" and node.java_name == "order")
                   or (direction == "响应" and node.java_name in {"badge", "subcategories"}))
        if node.children and has_structured_children(node) and not special:
            nested.append((nested_class_name(path + (node.java_name,)), node))
            nested.extend(collect_nested(node.children, direction, path + (node.java_name,)))
    return nested


def render_model(package_name: str, class_name: str, contract_name: str,
                 direction: str, nodes: list[FieldNode]) -> tuple[str, list[str]]:
    imports = collect_imports(nodes, direction)
    is_ocr_request = class_name == "OcrRequest" and direction == "请求"
    if is_ocr_request:
        imports.add(f"{BASE_PACKAGE}.tool.validation.ValidOcrSource")
    type_names = [f"{package_name}.{class_name}"]
    lines = [GENERATED_NOTICE, f"package {package_name};", ""]
    for import_name in sorted(imports):
        lines.append(f"import {import_name};")
    class_annotations = ["@Data"]
    if direction == "响应":
        class_annotations.insert(0, "@Slf4j")
    if is_ocr_request:
        class_annotations.insert(0, "@ValidOcrSource")
    lines.extend([
        "",
        "/**",
        f" * {contract_name}{direction}模型。",
        " *",
        " * <p>字段来源：SellerSprite 官方接口文档表格。</p>",
        " */",
        *class_annotations,
        f'@Schema(description = "{contract_name}{direction}模型")',
        f"public class {class_name} {{",
        "",
    ])
    lines.extend(render_fields(contract_name, direction, nodes, (), "    "))
    if direction == "响应":
        lines.extend(render_additional_properties("    "))
    for nested_name, parent in collect_nested(nodes, direction):
        type_names.append(f"{package_name}.{class_name}.{nested_name}")
        description = field_description(contract_name, direction, parent)
        nested_annotations = ["    @Data"]
        if direction == "响应":
            nested_annotations.insert(0, "    @Slf4j")
        lines.extend([
            *nested_annotations,
            f'    @Schema(description = "{java_string(description)}")',
            f"    public static class {nested_name} {{",
            "",
        ])
        lines.extend(render_fields(contract_name, direction, parent.children,
                                   tuple_part_from_nested(nested_name), "        "))
        if direction == "响应":
            lines.extend(render_additional_properties("        "))
        lines.extend(["    }", ""])
    lines.append("}")
    return "\n".join(lines) + "\n", type_names


def tuple_part_from_nested(nested_name: str) -> tuple[str, ...]:
    # Nested names are already globally unique within a root model. Passing the
    # name as the path keeps descendant type names deterministic.
    return (nested_name.removesuffix("Vo"),)


def render_page_wrapper(package_name: str, class_name: str, contract_name: str,
                        item_type: str, item_import: str | None) -> str:
    lines = [
        GENERATED_NOTICE,
        f"package {package_name};",
        "",
        f"import {BASE_PACKAGE}.common.model.vo.SellerSpritePageVo;",
        "import com.fasterxml.jackson.annotation.JsonAnyGetter;",
        "import com.fasterxml.jackson.annotation.JsonAnySetter;",
        "import io.swagger.v3.oas.annotations.media.Schema;",
        "import java.util.LinkedHashMap;",
        "import java.util.Map;",
        "import lombok.extern.slf4j.Slf4j;",
        "import tools.jackson.databind.JsonNode;",
    ]
    if item_import:
        lines.append(f"import {item_import};")
    lines.extend([
        "",
        "/**",
        f" * {contract_name}分页响应。",
        " */",
        "@Slf4j",
        f'@Schema(description = "{contract_name}分页响应")',
        f"public class {class_name} extends SellerSpritePageVo<{item_type}> {{",
    ])
    lines.extend(render_additional_properties("    "))
    lines.extend(["}", ""])
    return "\n".join(lines)


def write_java(package_name: str, class_name: str, content: str) -> None:
    directory = JAVA_ROOT / Path(package_name.replace(".", "/"))
    directory.mkdir(parents=True, exist_ok=True)
    (directory / f"{class_name}.java").write_text(content, encoding="utf-8", newline="\n")


def generate_contract_models(contracts: list[Contract]) -> tuple[list[str], int, int]:
    model_types: list[str] = [
        f"{BASE_PACKAGE}.common.model.dto.SortOrder",
        f"{BASE_PACKAGE}.common.model.vo.SellerSpritePageVo",
        f"{BASE_PACKAGE}.common.model.vo.BadgeVo",
        f"{BASE_PACKAGE}.common.model.vo.SubcategoryVo",
        f"{BASE_PACKAGE}.common.model.vo.ProductSummaryVo",
        f"{BASE_PACKAGE}.common.model.vo.VariationVo",
        f"{BASE_PACKAGE}.common.model.vo.NumericTrendPointVo",
        f"{BASE_PACKAGE}.common.model.vo.StringTrendPointVo",
        f"{BASE_PACKAGE}.common.model.vo.SubRankTrendVo",
    ]
    request_count = 0
    response_count = 0
    for contract in contracts:
        endpoint = contract.endpoint
        request_count += len(contract.request_rows)
        response_count += len(contract.response_rows)
        if contract.request_nodes:
            package_name = f"{BASE_PACKAGE}.{endpoint.domain}.model.dto"
            class_name = endpoint.base_name + "Request"
            content, names = render_model(package_name, class_name, contract.name, "请求", contract.request_nodes)
            write_java(package_name, class_name, content)
            model_types.extend(names)

        package_name = f"{BASE_PACKAGE}.{endpoint.domain}.model.vo"
        if endpoint.response_shape == "page":
            wrapper_name = endpoint.base_name + "Vo"
            if endpoint.response_item_override:
                item_type = endpoint.response_item_override
                item_import = f"{BASE_PACKAGE}.common.model.vo.{item_type}"
            else:
                item_type = endpoint.base_name + "ItemVo"
                item_import = None
                item_content, names = render_model(package_name, item_type, contract.name + "明细",
                                                   "响应", contract.response_nodes)
                write_java(package_name, item_type, item_content)
                model_types.extend(names)
            write_java(package_name, wrapper_name,
                       render_page_wrapper(package_name, wrapper_name, contract.name, item_type, item_import))
            model_types.append(f"{package_name}.{wrapper_name}")
        else:
            class_name = endpoint.base_name + "Vo"
            content, names = render_model(package_name, class_name, contract.name, "响应", contract.response_nodes)
            write_java(package_name, class_name, content)
            model_types.extend(names)
    return model_types, request_count, response_count


def render_index(contracts: list[Contract], model_types: list[str],
                 request_count: int, response_count: int) -> str:
    operation_lines = ",\n            ".join(
        f"SellerSpriteOperation.{contract.endpoint.operation}" for contract in contracts
    )
    endpoint_lines = ",\n            ".join(
        "new DocumentedEndpoint("
        f"SellerSpriteOperation.{contract.endpoint.operation}, "
        f'"{java_string(contract.method)}", "{java_string(contract.path)}")'
        for contract in contracts
    )
    type_lines = ",\n            ".join(f"{type_name}.class" for type_name in model_types)
    example_lines = ",\n            ".join(
        "new OfficialExample("
        f"SellerSpriteOperation.{contract.endpoint.operation}, "
        f'"/sellersprite/examples/{contract.endpoint.operation.lower()}.json", '
        f"{BASE_PACKAGE}.{contract.endpoint.domain}.model.vo."
        f"{contract.endpoint.base_name}Vo.class, "
        f"{str(contract.endpoint.response_shape == 'list').lower()})"
        for contract in contracts if contract.response_example is not None
    )
    return f"""{GENERATED_NOTICE}
package {BASE_PACKAGE}.client;

import java.util.List;

/**
 * SellerSprite 官方文档生成契约索引，用于覆盖率和注释完整性测试。
 */
public final class GeneratedSellerSpriteContractIndex {{

    private static final List<SellerSpriteOperation> OPERATIONS = List.of(
            {operation_lines});

    private static final List<DocumentedEndpoint> DOCUMENTED_ENDPOINTS = List.of(
            {endpoint_lines});

    private static final List<Class<?>> MODEL_TYPES = List.of(
            {type_lines});

    private static final List<OfficialExample> OFFICIAL_EXAMPLES = List.of(
            {example_lines});

    private GeneratedSellerSpriteContractIndex() {{
    }}

    public static List<SellerSpriteOperation> getOperations() {{
        return OPERATIONS;
    }}

    public static List<Class<?>> getModelTypes() {{
        return MODEL_TYPES;
    }}

    public static List<DocumentedEndpoint> getDocumentedEndpoints() {{
        return DOCUMENTED_ENDPOINTS;
    }}

    public static int getDocumentedRequestFieldCount() {{
        return {request_count};
    }}

    public static int getDocumentedResponseFieldCount() {{
        return {response_count};
    }}

    public static List<OfficialExample> getOfficialExamples() {{
        return OFFICIAL_EXAMPLES;
    }}

    /**
     * 官方响应示例与强类型 data 模型的对应关系。
     *
     * @param operation SellerSprite 操作
     * @param resourcePath classpath 下的官方响应示例
     * @param dataType data 字段的元素或对象类型
     * @param collection data 是否为数组
     */
    public record OfficialExample(SellerSpriteOperation operation, String resourcePath,
                                  Class<?> dataType, boolean collection) {{
    }}

    /**
     * 官方文档中的 HTTP 方法与远端路径。
     *
     * @param operation SellerSprite 操作
     * @param method 官方 HTTP 方法
     * @param path 官方远端路径
     */
    public record DocumentedEndpoint(SellerSpriteOperation operation, String method, String path) {{
    }}
}}
"""


def snapshot(contracts: list[Contract]) -> None:
    payload = {
        "source": DOC_BASE_URL,
        "capturedAt": CAPTURED_AT,
        "businessOperationCount": len(contracts),
        "endpoints": [
            {
                "id": contract.endpoint.doc_id,
                "operation": contract.endpoint.operation,
                "domain": contract.endpoint.domain,
                "name": contract.name,
                "method": contract.method,
                "path": contract.path,
                "requestFields": contract.request_rows,
                "responseFields": contract.response_rows,
                "officialResponseExample": contract.response_example,
            }
            for contract in contracts
        ],
    }
    path = ROOT / "docs/sellersprite-api-contract.json"
    path.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8", newline="\n")


def write_frontend_contracts(contracts: list[Contract]) -> None:
    payload = [
        {
            "operation": contract.endpoint.operation,
            "domain": contract.endpoint.domain,
            "responseShape": contract.endpoint.response_shape,
            "requestFields": contract.request_rows,
            "responseFields": contract.response_rows,
        }
        for contract in contracts
    ]
    content = (
        f"// Generated from SellerSprite official documentation on {CAPTURED_AT}.\n"
        "export const officialSellerSpriteOperationContracts = "
        f"{json.dumps(payload, ensure_ascii=False, indent=2)} as const\n"
    )
    FRONTEND_CONTRACT_PATH.parent.mkdir(parents=True, exist_ok=True)
    FRONTEND_CONTRACT_PATH.write_text(content, encoding="utf-8", newline="\n")


def write_official_examples(contracts: list[Contract]) -> int:
    directory = ROOT / "sellersprite-api/src/test/resources/sellersprite/examples"
    directory.mkdir(parents=True, exist_ok=True)
    count = 0
    for contract in contracts:
        if contract.response_example is None:
            continue
        filename = contract.endpoint.operation.lower() + ".json"
        (directory / filename).write_text(
            json.dumps(contract.response_example, ensure_ascii=False, indent=2) + "\n",
            encoding="utf-8", newline="\n")
        count += 1
    return count


def main() -> None:
    session = requests.Session()
    session.headers["User-Agent"] = "sellersprite-contract-generator/1.0"
    contracts = [fetch_contract(session, endpoint) for endpoint in ENDPOINTS]
    model_types, request_count, response_count = generate_contract_models(contracts)
    write_java(f"{BASE_PACKAGE}.client", "GeneratedSellerSpriteContractIndex",
               render_index(contracts, model_types, request_count, response_count))
    snapshot(contracts)
    write_frontend_contracts(contracts)
    example_count = write_official_examples(contracts)
    print(f"Generated {len(contracts)} endpoint contracts, {request_count} request fields, "
          f"{response_count} response fields, {len(model_types)} model types and "
          f"{example_count} official response examples.")


if __name__ == "__main__":
    main()

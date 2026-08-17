// Generated from SellerSprite official documentation on 2026-07-14.
export const officialSellerSpriteOperationContracts = [
  {
    "operation": "PRODUCT_COMPETITOR_LOOKUP",
    "domain": "product",
    "responseShape": "page",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "市场编码",
        "description": "见表 1.2"
      },
      {
        "field": "month",
        "type": "String",
        "required": false,
        "name": "查询月份",
        "description": "格式：yyyyMM，示例：202507，见表 1.1"
      },
      {
        "field": "brand",
        "type": "String",
        "required": false,
        "name": "品牌",
        "description": "WWDOLL"
      },
      {
        "field": "sellerName",
        "type": "String",
        "required": false,
        "name": "卖家",
        "description": "Apple"
      },
      {
        "field": "asins",
        "type": "List",
        "required": false,
        "name": "asin 的 list 字符串",
        "description": "最多支持40个ASIN"
      },
      {
        "field": "nodeIdPath",
        "type": "String",
        "required": false,
        "name": "类目节点字符串",
        "description": "见查产品类目"
      },
      {
        "field": "nodeIdPathEqual",
        "type": "boolean",
        "required": false,
        "name": "类目节点查询方式",
        "description": "true: 为类目精确查询, false: 为查询当前及子类目; 默认：false"
      },
      {
        "field": "keyword",
        "type": "String",
        "required": false,
        "name": "关键字",
        "description": ""
      },
      {
        "field": "matchType",
        "type": "Integer",
        "required": false,
        "name": "关键词匹配方式",
        "description": "1：词组匹配，2：模糊匹配，3：精准匹配；默认：2"
      },
      {
        "field": "variation",
        "type": "String",
        "required": false,
        "name": "是否查询变体ASIN",
        "description": "N: 含变体, Y: 不含变体"
      },
      {
        "field": "page",
        "type": "Integer",
        "required": false,
        "name": "页码",
        "description": "Default: 1"
      },
      {
        "field": "size",
        "type": "Integer",
        "required": false,
        "name": "每页条数",
        "description": "Default：50，Max: 100"
      },
      {
        "field": "order",
        "type": "Object",
        "required": false,
        "name": "排序对象",
        "description": ""
      },
      {
        "field": "└field",
        "type": "String",
        "required": false,
        "name": "排序字段，默认：total_units",
        "description": "见表1.6"
      },
      {
        "field": "└desc",
        "type": "boolean",
        "required": false,
        "name": "排序方式",
        "description": "true：desc，false：asc；Default：true"
      }
    ],
    "responseFields": [
      {
        "field": "asin",
        "type": "String",
        "required": false,
        "name": "asin",
        "description": "B078J8VPVW"
      },
      {
        "field": "brand",
        "type": "String",
        "required": false,
        "name": "品牌",
        "description": "Pampers"
      },
      {
        "field": "brandUrl",
        "type": "String",
        "required": false,
        "name": "品牌 URL",
        "description": "https://www.amazon.com/s?k=HP"
      },
      {
        "field": "imageUrl",
        "type": "String",
        "required": false,
        "name": "图片 URL",
        "description": "https://images-na.ssl-images-amazon.com/images/I/51axlzme6aL .AC_US200.jpg"
      },
      {
        "field": "title",
        "type": "String",
        "required": false,
        "name": "商品标题",
        "description": "Diapers Size ……"
      },
      {
        "field": "parent",
        "type": "String",
        "required": false,
        "name": "父体",
        "description": "B081RGNL17"
      },
      {
        "field": "nodeId",
        "type": "Long",
        "required": false,
        "name": "节点 id",
        "description": "3741281"
      },
      {
        "field": "nodeIdPath",
        "type": "String",
        "required": false,
        "name": "节点 id 路径字符串",
        "description": "2619525011:3741271:3741281"
      },
      {
        "field": "nodeLabelPath",
        "type": "String",
        "required": false,
        "name": "类目",
        "description": "Baby Products:Diapering:Disposable Diapers"
      },
      {
        "field": "symbol",
        "type": "String",
        "required": false,
        "name": "是否畅销",
        "description": "Y"
      },
      {
        "field": "bsrId",
        "type": "String",
        "required": false,
        "name": "BSRid",
        "description": "office-products"
      },
      {
        "field": "bsr",
        "type": "Integer",
        "required": false,
        "name": "BSR 排名",
        "description": "1"
      },
      {
        "field": "bsrCr",
        "type": "Float",
        "required": false,
        "name": "BSR 增长率",
        "description": "926.67"
      },
      {
        "field": "bsrCv",
        "type": "Integer",
        "required": false,
        "name": "BSR 增长数",
        "description": "10"
      },
      {
        "field": "units",
        "type": "Integer",
        "required": false,
        "name": "月销量(父体)",
        "description": "26289"
      },
      {
        "field": "unitsGr",
        "type": "Float",
        "required": false,
        "name": "月销量增长率(父体)",
        "description": "-46.3"
      },
      {
        "field": "amzUnit",
        "type": "Integer",
        "required": false,
        "name": "子体近30日销量",
        "description": "4000"
      },
      {
        "field": "amzSales",
        "type": "Float",
        "required": false,
        "name": "销售额(子体)",
        "description": "235000"
      },
      {
        "field": "amzUnitDate",
        "type": "Date",
        "required": false,
        "name": "子体销量更新日期",
        "description": "1702476590000"
      },
      {
        "field": "revenue",
        "type": "Float",
        "required": false,
        "name": "月销售额(父体)",
        "description": "1693537.4"
      },
      {
        "field": "price",
        "type": "Float",
        "required": false,
        "name": "价格",
        "description": "64.42"
      },
      {
        "field": "primePrice",
        "type": "Float",
        "required": false,
        "name": "prime价格，-1表示没有",
        "description": "56.6"
      },
      {
        "field": "profit",
        "type": "Float",
        "required": false,
        "name": "利润率",
        "description": "63.92"
      },
      {
        "field": "fba",
        "type": "Float",
        "required": false,
        "name": "fba 运费",
        "description": "13.58"
      },
      {
        "field": "ratings",
        "type": "Integer",
        "required": false,
        "name": "评分数",
        "description": "32004"
      },
      {
        "field": "ratingsRate",
        "type": "Float",
        "required": false,
        "name": "留评率",
        "description": "40.57"
      },
      {
        "field": "rating",
        "type": "Float",
        "required": false,
        "name": "评分",
        "description": "4.8"
      },
      {
        "field": "ratingsCv",
        "type": "Integer",
        "required": false,
        "name": "月度增长数",
        "description": "10666"
      },
      {
        "field": "ratingDelta",
        "type": "Integer",
        "required": false,
        "name": "留评数：近 30 天新增评论数",
        "description": "0"
      },
      {
        "field": "lqs",
        "type": "Float",
        "required": false,
        "name": "listing质量得分",
        "description": ""
      },
      {
        "field": "availableDate",
        "type": "Long",
        "required": false,
        "name": "上架时间",
        "description": "1454083200000"
      },
      {
        "field": "fulfillment",
        "type": "String",
        "required": false,
        "name": "配送方式",
        "description": "AMZ or FBA or FBM"
      },
      {
        "field": "variations",
        "type": "Integer",
        "required": false,
        "name": "变体数",
        "description": "7"
      },
      {
        "field": "sellers",
        "type": "Integer",
        "required": false,
        "name": "卖家数",
        "description": "7"
      },
      {
        "field": "sellerId",
        "type": "String",
        "required": false,
        "name": "BuyBox 卖家 id",
        "description": "A1Y8BVAASXO4R7"
      },
      {
        "field": "sellerName",
        "type": "String",
        "required": false,
        "name": "BuyBox 卖家",
        "description": "Amazon"
      },
      {
        "field": "sellerNation",
        "type": "String",
        "required": false,
        "name": "BuyBox 卖家国籍",
        "description": "见表 1.5"
      },
      {
        "field": "badge",
        "type": "Badge",
        "required": false,
        "name": "标识",
        "description": "包括了下面 5 个标识"
      },
      {
        "field": "└bestSeller",
        "type": "String",
        "required": false,
        "name": "Best Seller 标识",
        "description": "Y / N"
      },
      {
        "field": "└amazonChoice",
        "type": "String",
        "required": false,
        "name": "amazon choice 标识",
        "description": "Y / N"
      },
      {
        "field": "└amazonChoice",
        "type": "String",
        "required": false,
        "name": "amazon choice 标识",
        "description": "Y / N"
      },
      {
        "field": "└newRelease",
        "type": "String",
        "required": false,
        "name": "release 标识",
        "description": "Y / N"
      },
      {
        "field": "└ebc",
        "type": "String",
        "required": false,
        "name": "A+页面",
        "description": "Y / N"
      },
      {
        "field": "└video",
        "type": "String",
        "required": false,
        "name": "视频介绍",
        "description": "Y / N"
      },
      {
        "field": "weight",
        "type": "String",
        "required": false,
        "name": "重量",
        "description": "8.88 pounds"
      },
      {
        "field": "dimension",
        "type": "String",
        "required": false,
        "name": "尺寸",
        "description": "13.3 x 15.8 x 10.6 inches"
      },
      {
        "field": "dimensionsType",
        "type": "String",
        "required": false,
        "name": "尺寸类型",
        "description": "ST,0V"
      },
      {
        "field": "pkgDimensions",
        "type": "String",
        "required": false,
        "name": "包装尺寸",
        "description": "14.3 x 16.8 x 12.6 inches"
      },
      {
        "field": "pkgDimensionType",
        "type": "String",
        "required": false,
        "name": "包装尺寸类型",
        "description": ""
      },
      {
        "field": "pkgWeight",
        "type": "String",
        "required": false,
        "name": "包装重量",
        "description": "18.88 pounds"
      },
      {
        "field": "sku",
        "type": "String",
        "required": false,
        "name": "sku",
        "description": "[\"Color: Beige\",\"Size: 47 inches\"]"
      },
      {
        "field": "subcategories",
        "type": "List",
        "required": false,
        "name": "子类目",
        "description": ""
      },
      {
        "field": "└code",
        "type": "String",
        "required": false,
        "name": "类目code",
        "description": "1063242"
      },
      {
        "field": "└rank",
        "type": "Integer",
        "required": false,
        "name": "排名",
        "description": "1"
      },
      {
        "field": "└label",
        "type": "String",
        "required": false,
        "name": "名称",
        "description": "Bath Rugs"
      },
      {
        "field": "deliveryPrice",
        "type": "Float",
        "required": false,
        "name": "卖家运费,-1表示没有",
        "description": "4"
      },
      {
        "field": "primePrice",
        "type": "Float",
        "required": false,
        "name": "prime价格，-1表示没有",
        "description": "42"
      }
    ]
  },
  {
    "operation": "PRODUCT_RESEARCH",
    "domain": "product",
    "responseShape": "page",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "市场编码",
        "description": "见表 1.2"
      },
      {
        "field": "month",
        "type": "String",
        "required": false,
        "name": "查询月份",
        "description": "格式：yyyyMM，示例：202507，见表 1.1"
      },
      {
        "field": "keyword",
        "type": "String",
        "required": false,
        "name": "关键字",
        "description": "N95"
      },
      {
        "field": "includeSellers",
        "type": "String",
        "required": false,
        "name": "包含卖家",
        "description": ""
      },
      {
        "field": "excludeSellers",
        "type": "String",
        "required": false,
        "name": "排除卖家",
        "description": ""
      },
      {
        "field": "matchType",
        "type": "Integer",
        "required": false,
        "name": "匹配方式，1词组匹配 2模糊匹配 3精准匹配；默认2",
        "description": "2"
      },
      {
        "field": "excludeKeywords",
        "type": "String",
        "required": false,
        "name": "排除的关键字",
        "description": "portable"
      },
      {
        "field": "minPrice",
        "type": "Float",
        "required": false,
        "name": "最低价格",
        "description": "10"
      },
      {
        "field": "maxPrice",
        "type": "Float",
        "required": false,
        "name": "最高价格",
        "description": "30"
      },
      {
        "field": "minRating",
        "type": "Float",
        "required": false,
        "name": "最低评分值",
        "description": "1"
      },
      {
        "field": "maxRating",
        "type": "Float",
        "required": false,
        "name": "最高评分值",
        "description": "5"
      },
      {
        "field": "minRatings",
        "type": "Integer",
        "required": false,
        "name": "最低评分数",
        "description": "1"
      },
      {
        "field": "maxRatings",
        "type": "Integer",
        "required": false,
        "name": "最高评分数",
        "description": "90"
      },
      {
        "field": "minRatingsCv",
        "type": "Integer",
        "required": false,
        "name": "最低月新增评分数",
        "description": "1"
      },
      {
        "field": "maxRatingsCv",
        "type": "Integer",
        "required": false,
        "name": "最高月新增评分数",
        "description": "5"
      },
      {
        "field": "minSellers",
        "type": "Integer",
        "required": false,
        "name": "最小卖家数量",
        "description": "3"
      },
      {
        "field": "maxSellers",
        "type": "Integer",
        "required": false,
        "name": "最大卖家数量",
        "description": "10"
      },
      {
        "field": "minProfit",
        "type": "Float",
        "required": false,
        "name": "最小毛利率",
        "description": "10"
      },
      {
        "field": "maxProfit",
        "type": "Float",
        "required": false,
        "name": "最大毛利率",
        "description": "20"
      },
      {
        "field": "minBsr",
        "type": "Integer",
        "required": false,
        "name": "大类 BSR 最高排名",
        "description": "1"
      },
      {
        "field": "maxBsr",
        "type": "Integer",
        "required": false,
        "name": "大类 BSR 最低排名",
        "description": "100"
      },
      {
        "field": "minBsrCv",
        "type": "Integer",
        "required": false,
        "name": "BSR 最低增长数",
        "description": "3"
      },
      {
        "field": "maxBsrCv",
        "type": "Integer",
        "required": false,
        "name": "BSR 最高增长数",
        "description": "5"
      },
      {
        "field": "minBsrCr",
        "type": "Float",
        "required": false,
        "name": "BSR 最低增长率",
        "description": "30"
      },
      {
        "field": "maxBsrCr",
        "type": "Float",
        "required": false,
        "name": "BSR 最高增长率",
        "description": "60"
      },
      {
        "field": "minUnits",
        "type": "Integer",
        "required": false,
        "name": "最低月销量",
        "description": "20"
      },
      {
        "field": "maxUnits",
        "type": "Integer",
        "required": false,
        "name": "最高月销量",
        "description": "50"
      },
      {
        "field": "minAmzUnit",
        "type": "Integer",
        "required": false,
        "name": "最低月子体销量",
        "description": "20"
      },
      {
        "field": "maxAmzUnit",
        "type": "Integer",
        "required": false,
        "name": "最高月子体销量",
        "description": "50"
      },
      {
        "field": "minRevenue",
        "type": "Float",
        "required": false,
        "name": "最低月销售额",
        "description": "60"
      },
      {
        "field": "maxRevenue",
        "type": "Float",
        "required": false,
        "name": "最高月销售额",
        "description": "200"
      },
      {
        "field": "minRevenueCr",
        "type": "Float",
        "required": false,
        "name": "月销售额最低增长率",
        "description": "20"
      },
      {
        "field": "maxRevenueCr",
        "type": "Float",
        "required": false,
        "name": "月销售额最高增长率",
        "description": "30"
      },
      {
        "field": "minUnitsCr",
        "type": "Float",
        "required": false,
        "name": "月销量最低增长率",
        "description": "20"
      },
      {
        "field": "maxUnitsCr",
        "type": "Float",
        "required": false,
        "name": "月销量最高增长率",
        "description": "30"
      },
      {
        "field": "weightUnit",
        "type": "String",
        "required": false,
        "name": "重量单位，默认：g",
        "description": "见表2.7"
      },
      {
        "field": "minWeights",
        "type": "Float",
        "required": false,
        "name": "最小重量",
        "description": "20"
      },
      {
        "field": "maxWeights",
        "type": "Float",
        "required": false,
        "name": "最大重量",
        "description": "30"
      },
      {
        "field": "minVariations",
        "type": "Integer",
        "required": false,
        "name": "最低变体数",
        "description": "1"
      },
      {
        "field": "maxVariations",
        "type": "Integer",
        "required": false,
        "name": "最高变体数",
        "description": "3"
      },
      {
        "field": "filterSub",
        "type": "String",
        "required": false,
        "name": "是否筛选子类目，Y：是",
        "description": "只有在指定类目时才会生效"
      },
      {
        "field": "minSubBsrRank",
        "type": "Integer",
        "required": false,
        "name": "最小子类排名",
        "description": "只有参数 filterSub=Y 时才生效"
      },
      {
        "field": "maxSubBsrRank",
        "type": "Integer",
        "required": false,
        "name": "最大子类排名",
        "description": "只有参数 filterSub=Y 时才生效"
      },
      {
        "field": "includeBrands",
        "type": "String",
        "required": false,
        "name": "包含品牌",
        "description": "Apple"
      },
      {
        "field": "excludeBrands",
        "type": "String",
        "required": false,
        "name": "排除品牌",
        "description": "Apple"
      },
      {
        "field": "nodeIdPaths",
        "type": "List",
        "required": false,
        "name": "类目节点字符串列表",
        "description": "见查产品类目接口"
      },
      {
        "field": "nodeIdPathEqual",
        "type": "boolean",
        "required": false,
        "name": "true为类目精确查询 false为查询当前及子类目",
        "description": "默认false"
      },
      {
        "field": "availableMonth",
        "type": "Integer",
        "required": false,
        "name": "上架月份",
        "description": "见表 1.3，默认不限制"
      },
      {
        "field": "dimensionType",
        "type": "String",
        "required": false,
        "name": "尺寸类型集合,逗号分隔，默认不限制",
        "description": "见表 1.4"
      },
      {
        "field": "minFba",
        "type": "Float",
        "required": false,
        "name": "FBA 最低运费",
        "description": "10"
      },
      {
        "field": "maxFba",
        "type": "Float",
        "required": false,
        "name": "FBA 最高运费",
        "description": "20"
      },
      {
        "field": "minLqs",
        "type": "Float",
        "required": false,
        "name": "最低 Listing 页面质量分",
        "description": "0"
      },
      {
        "field": "maxLqs",
        "type": "Float",
        "required": false,
        "name": "最高 Listing 页面质量分",
        "description": "10"
      },
      {
        "field": "sellerNation",
        "type": "String",
        "required": false,
        "name": "卖家所属地，默认不限制，多条件查询用逗号隔开",
        "description": "见表 1.5"
      },
      {
        "field": "badgeBS",
        "type": "String",
        "required": false,
        "name": "是否有热销标识 Best Seller",
        "description": "Y:是"
      },
      {
        "field": "badgeAC",
        "type": "String",
        "required": false,
        "name": "是否有热销标识 Amazon's Choice",
        "description": "Y:是"
      },
      {
        "field": "badgeNR",
        "type": "String",
        "required": false,
        "name": "是否有新品标识 New Release",
        "description": "Y:是"
      },
      {
        "field": "fulfillment",
        "type": "String",
        "required": false,
        "name": "配送方式，多条件查询用逗号隔开",
        "description": "AMZ or FBA or FBM"
      },
      {
        "field": "variation",
        "type": "String",
        "required": false,
        "name": "是否查询变体 asin",
        "description": "N: 含变体, Y: 不含变体"
      },
      {
        "field": "page",
        "type": "Integer",
        "required": false,
        "name": "页码，从 1 开始",
        "description": "默认：1，总条数限制2000条，可以细分条件拉取整个类目数据"
      },
      {
        "field": "size",
        "type": "Integer",
        "required": false,
        "name": "每页条数",
        "description": "默认：50，最大：100"
      },
      {
        "field": "order",
        "type": "Object",
        "required": false,
        "name": "排序",
        "description": ""
      },
      {
        "field": "└field",
        "type": "String",
        "required": false,
        "name": "排序字段，默认：total_units",
        "description": "见表1.6"
      },
      {
        "field": "└desc",
        "type": "boolean",
        "required": false,
        "name": "排序方式",
        "description": "true：desc，false：asc；Default：true"
      }
    ],
    "responseFields": [
      {
        "field": "asin",
        "type": "String",
        "required": false,
        "name": "asin",
        "description": "B078J8VPVW"
      },
      {
        "field": "brand",
        "type": "String",
        "required": false,
        "name": "品牌",
        "description": "Pampers"
      },
      {
        "field": "brandUrl",
        "type": "String",
        "required": false,
        "name": "品牌 URL",
        "description": "https://www.amazon.com/s?k=HP"
      },
      {
        "field": "imageUrl",
        "type": "String",
        "required": false,
        "name": "图片 URL",
        "description": "https://images-na.ssl-images-amazon.com/images/I/51axlzme6aL .AC_US200.jpg"
      },
      {
        "field": "title",
        "type": "String",
        "required": false,
        "name": "商品标题",
        "description": "Diapers Size 2, 186 Count - Pampers Swaddlers Disposable Baby Diapers, ONE MONTH SUPPLY"
      },
      {
        "field": "parent",
        "type": "String",
        "required": false,
        "name": "父体",
        "description": "B081RGNL17"
      },
      {
        "field": "nodeId",
        "type": "Long",
        "required": false,
        "name": "节点 id",
        "description": "3741281"
      },
      {
        "field": "nodeIdPath",
        "type": "String",
        "required": false,
        "name": "节点 id 路径字符串",
        "description": "2619525011:3741271:3741281"
      },
      {
        "field": "nodeLabelPath",
        "type": "String",
        "required": false,
        "name": "类目",
        "description": "Baby Products:Diapering:Disposable Diapers"
      },
      {
        "field": "symbol",
        "type": "String",
        "required": false,
        "name": "是否畅销",
        "description": "Y"
      },
      {
        "field": "bsrId",
        "type": "String",
        "required": false,
        "name": "BSRid",
        "description": "office-products"
      },
      {
        "field": "bsr",
        "type": "Integer",
        "required": false,
        "name": "BSR 排名",
        "description": "1"
      },
      {
        "field": "bsrCr",
        "type": "Float",
        "required": false,
        "name": "BSR 增长率",
        "description": "926.67"
      },
      {
        "field": "bsrCv",
        "type": "Integer",
        "required": false,
        "name": "BSR 增长数",
        "description": "10"
      },
      {
        "field": "units",
        "type": "Integer",
        "required": false,
        "name": "月销量(父)",
        "description": "26289"
      },
      {
        "field": "unitsGr",
        "type": "Float",
        "required": false,
        "name": "月销量增长率",
        "description": "-46.3"
      },
      {
        "field": "amzUnit",
        "type": "Integer",
        "required": false,
        "name": "子体近30日销量(仅近30日查询支持)",
        "description": "4000"
      },
      {
        "field": "amzUnitDate",
        "type": "Date",
        "required": false,
        "name": "子体销量更新日期",
        "description": "1.70248E+12"
      },
      {
        "field": "revenue",
        "type": "Float",
        "required": false,
        "name": "月销售额(父体)",
        "description": "1693537.4"
      },
      {
        "field": "price",
        "type": "Float",
        "required": false,
        "name": "价格",
        "description": "64.42"
      },
      {
        "field": "primePrice",
        "type": "Float",
        "required": false,
        "name": "prime价格，-1表示没有",
        "description": "56.6"
      },
      {
        "field": "profit",
        "type": "Float",
        "required": false,
        "name": "利润率",
        "description": "63.92"
      },
      {
        "field": "fba",
        "type": "Float",
        "required": false,
        "name": "fba 运费",
        "description": "13.58"
      },
      {
        "field": "ratings",
        "type": "Integer",
        "required": false,
        "name": "评分数",
        "description": "32004"
      },
      {
        "field": "ratingsRate",
        "type": "Float",
        "required": false,
        "name": "留评率",
        "description": "40.57"
      },
      {
        "field": "rating",
        "type": "Float",
        "required": false,
        "name": "评分",
        "description": "4.8"
      },
      {
        "field": "ratingsCv",
        "type": "Integer",
        "required": false,
        "name": "月度增长数",
        "description": "10666"
      },
      {
        "field": "ratingDelta",
        "type": "Integer",
        "required": false,
        "name": "留评数：近 30 天新增评论数",
        "description": "0"
      },
      {
        "field": "lqs",
        "type": "Float",
        "required": false,
        "name": "listing质量得分",
        "description": ""
      },
      {
        "field": "availableDate",
        "type": "Long",
        "required": false,
        "name": "上架时间，时间戳格式",
        "description": "1.45408E+12"
      },
      {
        "field": "fulfillment",
        "type": "String",
        "required": false,
        "name": "配送方式",
        "description": "AMZ or FBA or FBM"
      },
      {
        "field": "variations",
        "type": "Integer",
        "required": false,
        "name": "变体数",
        "description": "7"
      },
      {
        "field": "sellers",
        "type": "Integer",
        "required": false,
        "name": "卖家数",
        "description": "7"
      },
      {
        "field": "sellerId",
        "type": "String",
        "required": false,
        "name": "BuyBox 卖家 id",
        "description": "A1Y8BVAASXO4R7"
      },
      {
        "field": "sellerName",
        "type": "String",
        "required": false,
        "name": "BuyBox 卖家",
        "description": "Amazon"
      },
      {
        "field": "sellerNation",
        "type": "String",
        "required": false,
        "name": "BuyBox 卖家国籍",
        "description": "见表 1.5"
      },
      {
        "field": "badge",
        "type": "Badge",
        "required": false,
        "name": "标识",
        "description": "包括了下面 5 个标识"
      },
      {
        "field": "└bestSeller",
        "type": "String",
        "required": false,
        "name": "Best Seller 标识",
        "description": "Y 或者 N"
      },
      {
        "field": "└amazonChoice",
        "type": "String",
        "required": false,
        "name": "amazon choice 标识",
        "description": "Y 或者 N"
      },
      {
        "field": "└newRelease",
        "type": "String",
        "required": false,
        "name": "release 标识",
        "description": "Y 或者 N"
      },
      {
        "field": "└ebc",
        "type": "String",
        "required": false,
        "name": "A+页面",
        "description": "Y 或者 N"
      },
      {
        "field": "└video",
        "type": "String",
        "required": false,
        "name": "视频介绍",
        "description": "Y 或者 N"
      },
      {
        "field": "weight",
        "type": "String",
        "required": false,
        "name": "重量",
        "description": "8.88 pounds"
      },
      {
        "field": "dimension",
        "type": "String",
        "required": false,
        "name": "尺寸",
        "description": "13.3 x 15.8 x 10.6 inches"
      },
      {
        "field": "dimensionsType",
        "type": "String",
        "required": false,
        "name": "尺寸类型",
        "description": "ST,0V"
      },
      {
        "field": "pkgDimensions",
        "type": "String",
        "required": false,
        "name": "包装尺寸",
        "description": "14.3 x 16.8 x 12.6 inches"
      },
      {
        "field": "pkgDimensionType",
        "type": "String",
        "required": false,
        "name": "包装尺寸类型",
        "description": ""
      },
      {
        "field": "pkgWeight",
        "type": "String",
        "required": false,
        "name": "包装重量",
        "description": "18.88 pounds"
      },
      {
        "field": "subcategories",
        "type": "List",
        "required": false,
        "name": "子类目",
        "description": ""
      },
      {
        "field": "└code",
        "type": "String",
        "required": false,
        "name": "类目code",
        "description": "1063242"
      },
      {
        "field": "└rank",
        "type": "Integer",
        "required": false,
        "name": "排名",
        "description": "1"
      },
      {
        "field": "└label",
        "type": "String",
        "required": false,
        "name": "名称",
        "description": "Bath Rugs"
      },
      {
        "field": "sku",
        "type": "String",
        "required": false,
        "name": "sku",
        "description": "[\"Color: Beige\",\"Size: 47 inches\"]"
      },
      {
        "field": "deliveryPrice",
        "type": "Float",
        "required": false,
        "name": "卖家运费,-1表示没有",
        "description": "4"
      },
      {
        "field": "primePrice",
        "type": "Float",
        "required": false,
        "name": "prime价格，-1表示没有",
        "description": "42"
      },
      {
        "field": "└amazonChoice",
        "type": "String",
        "required": false,
        "name": "amazon choice 标识",
        "description": "Y 或者 N"
      },
      {
        "field": "primePrice",
        "type": "Float",
        "required": false,
        "name": "prime价格，-1表示没有",
        "description": "42"
      }
    ]
  },
  {
    "operation": "PRODUCT_NODE",
    "domain": "product",
    "responseShape": "list",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "市场",
        "description": "见表 1.2"
      },
      {
        "field": "nodeIdPath",
        "type": "String",
        "required": false,
        "name": "类目节点 id 字符串",
        "description": "2619525011:3741271:3741281"
      },
      {
        "field": "keyword",
        "type": "String",
        "required": false,
        "name": "搜索关键字，nodeId或类目名称",
        "description": "Books 或者 4053"
      },
      {
        "field": "month",
        "type": "String",
        "required": false,
        "name": "查询历史月份类目，格式yyyyMM",
        "description": "202502"
      }
    ],
    "responseFields": [
      {
        "field": "nodeIdPath",
        "type": "String",
        "required": false,
        "name": "类目 id 字符串，即 nodeIdPath",
        "description": "2619525011:3741271"
      },
      {
        "field": "nodeLabelPath",
        "type": "String",
        "required": false,
        "name": "类目名称",
        "description": "Appliances:Dishwashers"
      },
      {
        "field": "products",
        "type": "Integer",
        "required": false,
        "name": "类目下产品数",
        "description": "42"
      },
      {
        "field": "nodeLabelLocale",
        "type": "String",
        "required": false,
        "name": "类目节点名称中文",
        "description": "洗碗机"
      },
      {
        "field": "nodeLabelPathLocale",
        "type": "String",
        "required": false,
        "name": "类目所属所有节点名称中文",
        "description": "大家电:洗碗机"
      }
    ]
  },
  {
    "operation": "ASIN_DETAIL",
    "domain": "asin",
    "responseShape": "object",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "市场",
        "description": "见表 1.2"
      },
      {
        "field": "asin",
        "type": "String",
        "required": true,
        "name": "asin",
        "description": "B08GHW4TBS"
      }
    ],
    "responseFields": [
      {
        "field": "asin",
        "type": "String",
        "required": false,
        "name": "asin",
        "description": "B08GHW4TBS"
      },
      {
        "field": "asinUrl",
        "type": "String",
        "required": false,
        "name": "asin url",
        "description": "https://www.amazon.com/dp/B08GHW4TBS"
      },
      {
        "field": "availableDate",
        "type": "Long",
        "required": false,
        "name": "上架日期",
        "description": "1609059137000"
      },
      {
        "field": "badge",
        "type": "Badge",
        "required": false,
        "name": "标识",
        "description": "包括了下面 5 个标识"
      },
      {
        "field": "└bestSeller",
        "type": "String",
        "required": false,
        "name": "Best Seller 标识",
        "description": "Y 或者 N"
      },
      {
        "field": "└amazonChoice",
        "type": "String",
        "required": false,
        "name": "amazon choice 标识",
        "description": "Y 或者 N"
      },
      {
        "field": "└newRelease",
        "type": "String",
        "required": false,
        "name": "release 标识",
        "description": "Y 或者 N"
      },
      {
        "field": "└ebc",
        "type": "String",
        "required": false,
        "name": "A+页面",
        "description": "Y 或者 N"
      },
      {
        "field": "└video",
        "type": "String",
        "required": false,
        "name": "视频介绍",
        "description": "Y 或者 N"
      },
      {
        "field": "brand",
        "type": "String",
        "required": false,
        "name": "品牌",
        "description": "mermaker"
      },
      {
        "field": "brandUrl",
        "type": "String",
        "required": false,
        "name": "品牌 URL",
        "description": "/stores/Mermaker/page/984A6448-1C68-4CCA-AD5A-D574EA2D65D5?ref_=ast_bln"
      },
      {
        "field": "bsrId",
        "type": "String",
        "required": false,
        "name": "bsr id",
        "description": "home-garden"
      },
      {
        "field": "bsrLabel",
        "type": "String",
        "required": false,
        "name": "bsr 标签",
        "description": "Home & Kitchen"
      },
      {
        "field": "bsrRank",
        "type": "Integer",
        "required": false,
        "name": "bsr 排名",
        "description": "1006"
      },
      {
        "field": "createdTime",
        "type": "Long",
        "required": false,
        "name": "创建时间",
        "description": "1606467137000"
      },
      {
        "field": "dimensions",
        "type": "String",
        "required": false,
        "name": "尺寸",
        "description": "7 x 6 x 0.6 inches"
      },
      {
        "field": "firstRatingDate",
        "type": "Long",
        "required": false,
        "name": "第一次评论时间",
        "description": "1609059137000"
      },
      {
        "field": "imageUrl",
        "type": "String",
        "required": false,
        "name": "图片链接",
        "description": "https://images-na.ssl-images-amazon.com/images/I/412616zl5YL .AC_US200.jpg"
      },
      {
        "field": "lqs",
        "type": "Integer",
        "required": false,
        "name": "Listing 页面质量得分",
        "description": "97"
      },
      {
        "field": "nodeId",
        "type": "String",
        "required": false,
        "name": "节点 id",
        "description": "1063280"
      },
      {
        "field": "nodeIdPath",
        "type": "String",
        "required": false,
        "name": "节点 id 串",
        "description": "1055398:1063252:1063280"
      },
      {
        "field": "nodeLabelPath",
        "type": "String",
        "required": false,
        "name": "类目名称串",
        "description": "Home & Kitchen:Bedding:Blankets & Throws"
      },
      {
        "field": "nodeLabelPathLocale",
        "type": "String",
        "required": false,
        "name": "类目名称串中文",
        "description": "家居厨房用品:床上用品:毯子、盖毯"
      },
      {
        "field": "parent",
        "type": "String",
        "required": false,
        "name": "父 asin",
        "description": "B07V5GB9B5"
      },
      {
        "field": "price",
        "type": "Float",
        "required": false,
        "name": "价格",
        "description": "21.99"
      },
      {
        "field": "questions",
        "type": "Integer",
        "required": false,
        "name": "问题数量",
        "description": "5"
      },
      {
        "field": "rating",
        "type": "Float",
        "required": false,
        "name": "评分",
        "description": "4.8"
      },
      {
        "field": "ratings",
        "type": "Integer",
        "required": false,
        "name": "评分数",
        "description": "29229"
      },
      {
        "field": "reviews",
        "type": "Integer",
        "required": false,
        "name": "评论数",
        "description": "9229"
      },
      {
        "field": "variantRatings",
        "type": "Integer",
        "required": false,
        "name": "子体评分数",
        "description": "12454"
      },
      {
        "field": "variantReviews",
        "type": "Integer",
        "required": false,
        "name": "子体评论数",
        "description": "3211"
      },
      {
        "field": "sellerId",
        "type": "String",
        "required": false,
        "name": "卖家 id",
        "description": "A13AJ1GXFINAZ"
      },
      {
        "field": "sellerName",
        "type": "String",
        "required": false,
        "name": "卖家名称",
        "description": "Mermaker"
      },
      {
        "field": "fulfillment",
        "type": "String",
        "required": false,
        "name": "配送方式",
        "description": "FBA"
      },
      {
        "field": "sellers",
        "type": "Integer",
        "required": false,
        "name": "卖家数",
        "description": "1"
      },
      {
        "field": "skuList",
        "type": "List",
        "required": false,
        "name": "sku",
        "description": "[\"Color: Beige\",\"Size: 47 inches\"]"
      },
      {
        "field": "marketplace",
        "type": "String",
        "required": false,
        "name": "String",
        "description": "见表 1.2"
      },
      {
        "field": "title",
        "type": "String",
        "required": false,
        "name": "标题",
        "description": "mermaker Burritos Tortilla Blanket 2.0 Double Sided 47 inches for Adult and Kids,Giant Funny Realistic Food Throw Blanket,285 GSM Novelty Soft Flannel Taco Blanket (Yellow Blanket-Double Sided)"
      },
      {
        "field": "features",
        "type": "List",
        "required": false,
        "name": "五点描述",
        "description": ""
      },
      {
        "field": "overviews",
        "type": "String",
        "required": false,
        "name": "详情，json格式字符串",
        "description": ""
      },
      {
        "field": "updatedTime",
        "type": "Long",
        "required": false,
        "name": "更新时间",
        "description": "1609059137000"
      },
      {
        "field": "variationList",
        "type": "List",
        "required": false,
        "name": "变体",
        "description": "[{\"asin\":\"B07V5GB9B5\",\"attribute\":\"Beige\"},{\"asin\":\"B08H86SSSF\",\"attribute\":\"Cookie\"}]"
      },
      {
        "field": "variations",
        "type": "Integer",
        "required": false,
        "name": "变体数量",
        "description": "14"
      },
      {
        "field": "weight",
        "type": "String",
        "required": false,
        "name": "重量",
        "description": "15.2 ounces"
      },
      {
        "field": "zoomImageUrl",
        "type": "String",
        "required": false,
        "name": "大图 URL",
        "description": "https://images-na.ssl-images-amazon.com/images/I/412616zl5YL .AC_US600.jpg"
      },
      {
        "field": "subcategories",
        "type": "Object",
        "required": false,
        "name": "子类目信息",
        "description": ""
      },
      {
        "field": "└rank",
        "type": "Integer",
        "required": false,
        "name": "子类目排名",
        "description": "1"
      },
      {
        "field": "└code",
        "type": "String",
        "required": false,
        "name": "子类目code",
        "description": "17874234011"
      },
      {
        "field": "└label",
        "type": "String",
        "required": false,
        "name": "子类目标签",
        "description": "Kids' Throw Blankets"
      },
      {
        "field": "deliveryPrice",
        "type": "Float",
        "required": false,
        "name": "卖家运费,-1表示没有",
        "description": "4"
      },
      {
        "field": "primePrice",
        "type": "Float",
        "required": false,
        "name": "prime价格，-1表示没有",
        "description": "42"
      },
      {
        "field": "coupon",
        "type": "String",
        "required": false,
        "name": "优惠卷",
        "description": "[save $20]"
      },
      {
        "field": "└amazonChoice",
        "type": "String",
        "required": false,
        "name": "amazon choice 标识",
        "description": "Y 或者 N"
      }
    ]
  },
  {
    "operation": "ASIN_COUPON_TREND",
    "domain": "asin",
    "responseShape": "list",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "市场",
        "description": "见表 1.2"
      },
      {
        "field": "asin",
        "type": "String",
        "required": true,
        "name": "asin",
        "description": "B08GHW4TBS"
      }
    ],
    "responseFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": false,
        "name": "marketplace",
        "description": ""
      },
      {
        "field": "asin",
        "type": "String",
        "required": false,
        "name": "asin",
        "description": ""
      },
      {
        "field": "date",
        "type": "String",
        "required": false,
        "name": "日期",
        "description": ""
      },
      {
        "field": "type",
        "type": "String",
        "required": false,
        "name": "优惠类型",
        "description": "M: 减免金额, P: 百分比折扣"
      },
      {
        "field": "asinPrice",
        "type": "Float",
        "required": false,
        "name": "ASIN价格",
        "description": ""
      },
      {
        "field": "couponPrice",
        "type": "Float",
        "required": false,
        "name": "优惠金额",
        "description": ""
      },
      {
        "field": "finalPrice",
        "type": "Float",
        "required": false,
        "name": "实际价格",
        "description": ""
      }
    ]
  },
  {
    "operation": "ASIN_WITH_COUPON_TREND",
    "domain": "asin",
    "responseShape": "object",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "市场",
        "description": "见表 1.2"
      },
      {
        "field": "asin",
        "type": "String",
        "required": true,
        "name": "asin",
        "description": "B08GHW4TBS"
      }
    ],
    "responseFields": [
      {
        "field": "asin",
        "type": "Object",
        "required": false,
        "name": "Asin Object",
        "description": ""
      },
      {
        "field": "└asin",
        "type": "String",
        "required": false,
        "name": "asin",
        "description": "B08GHW4TBS"
      },
      {
        "field": "└asinUrl",
        "type": "String",
        "required": false,
        "name": "asin url",
        "description": "https://www.amazon.com/dp/B08GHW4TBS"
      },
      {
        "field": "└availableDate",
        "type": "Long",
        "required": false,
        "name": "上架日期",
        "description": "1609059137000"
      },
      {
        "field": "└badge",
        "type": "Badge",
        "required": false,
        "name": "标识",
        "description": "包括了下面 5 个标识"
      },
      {
        "field": "└└bestSeller",
        "type": "String",
        "required": false,
        "name": "Best Seller 标识",
        "description": "Y 或者 N"
      },
      {
        "field": "└└amazonChoice",
        "type": "String",
        "required": false,
        "name": "amazon choice 标识",
        "description": "Y 或者 N"
      },
      {
        "field": "└└newRelease",
        "type": "String",
        "required": false,
        "name": "release 标识",
        "description": "Y 或者 N"
      },
      {
        "field": "└└ebc",
        "type": "String",
        "required": false,
        "name": "A+页面",
        "description": "Y 或者 N"
      },
      {
        "field": "└└video",
        "type": "String",
        "required": false,
        "name": "视频介绍",
        "description": "Y 或者 N"
      },
      {
        "field": "└brand",
        "type": "String",
        "required": false,
        "name": "品牌",
        "description": "mermaker"
      },
      {
        "field": "└brandUrl",
        "type": "String",
        "required": false,
        "name": "品牌 URL",
        "description": "/stores/Mermaker/page/984A6448-1C68-4CCA-AD5A-D574EA2D65D5?ref_=ast_bln"
      },
      {
        "field": "└bsrId",
        "type": "String",
        "required": false,
        "name": "bsr id",
        "description": "home-garden"
      },
      {
        "field": "└bsrLabel",
        "type": "String",
        "required": false,
        "name": "bsr 标签",
        "description": "Home & Kitchen"
      },
      {
        "field": "└bsrRank",
        "type": "Integer",
        "required": false,
        "name": "bsr 排名",
        "description": "1006"
      },
      {
        "field": "└createdTime",
        "type": "Long",
        "required": false,
        "name": "创建时间",
        "description": "1606467137000"
      },
      {
        "field": "└dimensions",
        "type": "String",
        "required": false,
        "name": "尺寸",
        "description": "7 x 6 x 0.6 inches"
      },
      {
        "field": "└firstRatingDate",
        "type": "Long",
        "required": false,
        "name": "第一次评论时间",
        "description": "1609059137000"
      },
      {
        "field": "└imageUrl",
        "type": "String",
        "required": false,
        "name": "图片链接",
        "description": "https://images-na.ssl-images-amazon.com/images/I/412616zl5YL .AC_US200.jpg"
      },
      {
        "field": "└lqs",
        "type": "Integer",
        "required": false,
        "name": "Listing 页面质量得分",
        "description": "97"
      },
      {
        "field": "└nodeId",
        "type": "String",
        "required": false,
        "name": "节点 id",
        "description": "1063280"
      },
      {
        "field": "└nodeIdPath",
        "type": "String",
        "required": false,
        "name": "节点 id 串",
        "description": "1055398:1063252:1063280"
      },
      {
        "field": "└nodeLabelPath",
        "type": "String",
        "required": false,
        "name": "类目名称串",
        "description": "Home & Kitchen:Bedding:Blankets & Throws"
      },
      {
        "field": "└nodeLabelPathLocale",
        "type": "String",
        "required": false,
        "name": "类目名称串中文",
        "description": "家居厨房用品:床上用品:毯子、盖毯"
      },
      {
        "field": "└parent",
        "type": "String",
        "required": false,
        "name": "父 asin",
        "description": "B07V5GB9B5"
      },
      {
        "field": "└price",
        "type": "Float",
        "required": false,
        "name": "价格",
        "description": "21.99"
      },
      {
        "field": "└questions",
        "type": "Integer",
        "required": false,
        "name": "问题数量",
        "description": "5"
      },
      {
        "field": "└rating",
        "type": "Float",
        "required": false,
        "name": "评分",
        "description": "4.8"
      },
      {
        "field": "└ratings",
        "type": "Integer",
        "required": false,
        "name": "评分数",
        "description": "29229"
      },
      {
        "field": "└reviews",
        "type": "Integer",
        "required": false,
        "name": "评论数",
        "description": "9229"
      },
      {
        "field": "└variantRatings",
        "type": "Integer",
        "required": false,
        "name": "子体评分数",
        "description": "12454"
      },
      {
        "field": "└variantReviews",
        "type": "Integer",
        "required": false,
        "name": "子体评论数",
        "description": "3211"
      },
      {
        "field": "└sellerId",
        "type": "String",
        "required": false,
        "name": "卖家 id",
        "description": "A13AJ1GXFINAZ"
      },
      {
        "field": "└sellerName",
        "type": "String",
        "required": false,
        "name": "卖家名称",
        "description": "Mermaker"
      },
      {
        "field": "└fulfillment",
        "type": "String",
        "required": false,
        "name": "配送方式",
        "description": "FBA"
      },
      {
        "field": "└sellers",
        "type": "Integer",
        "required": false,
        "name": "卖家数",
        "description": "1"
      },
      {
        "field": "└skuList",
        "type": "List",
        "required": false,
        "name": "sku",
        "description": "[\"Color: Beige\",\"Size: 47 inches\"]"
      },
      {
        "field": "└marketplace",
        "type": "String",
        "required": false,
        "name": "String",
        "description": "见表 1.2"
      },
      {
        "field": "└title",
        "type": "String",
        "required": false,
        "name": "标题",
        "description": "mermaker Burritos Tortilla Blanket 2.0 Double Sided 47 inches for Adult and Kids,Giant Funny Realistic Food Throw Blanket,285 GSM Novelty Soft Flannel Taco Blanket (Yellow Blanket-Double Sided)"
      },
      {
        "field": "└features",
        "type": "List",
        "required": false,
        "name": "五点描述",
        "description": ""
      },
      {
        "field": "└overviews",
        "type": "String",
        "required": false,
        "name": "详情，json格式字符串",
        "description": ""
      },
      {
        "field": "└updatedTime",
        "type": "Long",
        "required": false,
        "name": "更新时间",
        "description": "1609059137000"
      },
      {
        "field": "└variationList",
        "type": "List",
        "required": false,
        "name": "变体",
        "description": "[{\"asin\":\"B07V5GB9B5\",\"attribute\":\"Beige\"},{\"asin\":\"B08H86SSSF\",\"attribute\":\"Cookie\"}]"
      },
      {
        "field": "└variations",
        "type": "Integer",
        "required": false,
        "name": "变体数量",
        "description": "14"
      },
      {
        "field": "└weight",
        "type": "String",
        "required": false,
        "name": "重量",
        "description": "15.2 ounces"
      },
      {
        "field": "└zoomImageUrl",
        "type": "String",
        "required": false,
        "name": "大图 URL",
        "description": "https://images-na.ssl-images-amazon.com/images/I/412616zl5YL .AC_US600.jpg"
      },
      {
        "field": "└subcategories",
        "type": "Object",
        "required": false,
        "name": "子类目信息",
        "description": ""
      },
      {
        "field": "└└rank",
        "type": "Integer",
        "required": false,
        "name": "子类目排名",
        "description": "1"
      },
      {
        "field": "└└code",
        "type": "String",
        "required": false,
        "name": "子类目code",
        "description": "17874234011"
      },
      {
        "field": "└└label",
        "type": "String",
        "required": false,
        "name": "子类目标签",
        "description": "Kids' Throw Blankets"
      },
      {
        "field": "└deliveryPrice",
        "type": "Float",
        "required": false,
        "name": "卖家运费,-1表示没有",
        "description": "4"
      },
      {
        "field": "└primePrice",
        "type": "Float",
        "required": false,
        "name": "prime价格，-1表示没有",
        "description": "42"
      },
      {
        "field": "└coupon",
        "type": "String",
        "required": false,
        "name": "优惠卷",
        "description": "[save $20]"
      },
      {
        "field": "couponTrends",
        "type": "List",
        "required": false,
        "name": "Coupon Trends",
        "description": ""
      },
      {
        "field": "└marketplace",
        "type": "String",
        "required": false,
        "name": "marketplace",
        "description": "见表 1.2"
      },
      {
        "field": "└asin",
        "type": "String",
        "required": false,
        "name": "asin",
        "description": "B08GHW4TBS"
      },
      {
        "field": "└date",
        "type": "String",
        "required": false,
        "name": "日期",
        "description": ""
      },
      {
        "field": "└type",
        "type": "String",
        "required": false,
        "name": "优惠类型",
        "description": "M: 减免金额, P: 百分比折扣"
      },
      {
        "field": "└asinPrice",
        "type": "Float",
        "required": false,
        "name": "ASIN价格",
        "description": ""
      },
      {
        "field": "└couponPrice",
        "type": "Float",
        "required": false,
        "name": "优惠金额",
        "description": ""
      },
      {
        "field": "└finalPrice",
        "type": "Float",
        "required": false,
        "name": "实际价格",
        "description": ""
      }
    ]
  },
  {
    "operation": "ASIN_SALES_TREND",
    "domain": "asin",
    "responseShape": "object",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "市场",
        "description": "见表 1.2"
      },
      {
        "field": "asin",
        "type": "String",
        "required": true,
        "name": "asin",
        "description": "B08GHW4TBS"
      }
    ],
    "responseFields": [
      {
        "field": "asin",
        "type": "Object",
        "required": false,
        "name": "Asin Object",
        "description": ""
      },
      {
        "field": "└asin",
        "type": "String",
        "required": false,
        "name": "asin",
        "description": "B08GHW4TBS"
      },
      {
        "field": "└asinUrl",
        "type": "String",
        "required": false,
        "name": "asin url",
        "description": "https://www.amazon.com/dp/B08GHW4TBS"
      },
      {
        "field": "└availableDate",
        "type": "Long",
        "required": false,
        "name": "上架日期",
        "description": "1.60906E+12"
      },
      {
        "field": "└badge",
        "type": "Badge",
        "required": false,
        "name": "标识",
        "description": "包括了下面 5 个标识"
      },
      {
        "field": "└└bestSeller",
        "type": "String",
        "required": false,
        "name": "Best Seller 标识",
        "description": "Y 或者 N"
      },
      {
        "field": "└└amazonChoice",
        "type": "String",
        "required": false,
        "name": "amazon choice 标识",
        "description": "Y 或者 N"
      },
      {
        "field": "└└newRelease",
        "type": "String",
        "required": false,
        "name": "release 标识",
        "description": "Y 或者 N"
      },
      {
        "field": "└└ebc",
        "type": "String",
        "required": false,
        "name": "A+页面",
        "description": "Y 或者 N"
      },
      {
        "field": "└└video",
        "type": "String",
        "required": false,
        "name": "视频介绍",
        "description": "Y 或者 N"
      },
      {
        "field": "└brand",
        "type": "String",
        "required": false,
        "name": "品牌",
        "description": "mermaker"
      },
      {
        "field": "└brandUrl",
        "type": "String",
        "required": false,
        "name": "品牌 URL",
        "description": "/stores/Mermaker/page/984A6448-1C68-4CCA-AD5A-D574EA2D65D5?ref_=ast_bln"
      },
      {
        "field": "└bsrId",
        "type": "String",
        "required": false,
        "name": "bsr id",
        "description": "home-garden"
      },
      {
        "field": "└bsrLabel",
        "type": "String",
        "required": false,
        "name": "bsr 标签",
        "description": "Home & Kitchen"
      },
      {
        "field": "└bsrRank",
        "type": "Integer",
        "required": false,
        "name": "bsr 排名",
        "description": "1006"
      },
      {
        "field": "└createdTime",
        "type": "Long",
        "required": false,
        "name": "创建时间",
        "description": "1.60647E+12"
      },
      {
        "field": "└dimensions",
        "type": "String",
        "required": false,
        "name": "尺寸",
        "description": "7 x 6 x 0.6 inches"
      },
      {
        "field": "└firstRatingDate",
        "type": "Long",
        "required": false,
        "name": "第一次评论时间",
        "description": "1.60906E+12"
      },
      {
        "field": "└imageUrl",
        "type": "String",
        "required": false,
        "name": "图片链接",
        "description": "https://images-na.ssl-images-amazon.com/images/I/412616zl5YL .AC_US200.jpg"
      },
      {
        "field": "└lqs",
        "type": "Integer",
        "required": false,
        "name": "Listing 页面质量得分",
        "description": "97"
      },
      {
        "field": "└nodeId",
        "type": "String",
        "required": false,
        "name": "节点 id",
        "description": "1063280"
      },
      {
        "field": "└nodeIdPath",
        "type": "String",
        "required": false,
        "name": "节点 id 串",
        "description": "1055398:1063252:1063280"
      },
      {
        "field": "└nodeLabelPath",
        "type": "String",
        "required": false,
        "name": "类目名称串",
        "description": "Home & Kitchen:Bedding:Blankets & Throws"
      },
      {
        "field": "└nodeLabelPathLocale",
        "type": "String",
        "required": false,
        "name": "类目名称串中文",
        "description": "家居厨房用品:床上用品:毯子、盖毯"
      },
      {
        "field": "└parent",
        "type": "String",
        "required": false,
        "name": "父 asin",
        "description": "B07V5GB9B5"
      },
      {
        "field": "└price",
        "type": "Float",
        "required": false,
        "name": "价格",
        "description": "21.99"
      },
      {
        "field": "└questions",
        "type": "Integer",
        "required": false,
        "name": "问题数量",
        "description": "5"
      },
      {
        "field": "└rating",
        "type": "Float",
        "required": false,
        "name": "评分",
        "description": "4.8"
      },
      {
        "field": "└ratings",
        "type": "Integer",
        "required": false,
        "name": "评分数",
        "description": "29229"
      },
      {
        "field": "└reviews",
        "type": "Integer",
        "required": false,
        "name": "评论数",
        "description": "9229"
      },
      {
        "field": "└variantRatings",
        "type": "Integer",
        "required": false,
        "name": "子体评分数",
        "description": "12454"
      },
      {
        "field": "└variantReviews",
        "type": "Integer",
        "required": false,
        "name": "子体评论数",
        "description": "3211"
      },
      {
        "field": "└sellerId",
        "type": "String",
        "required": false,
        "name": "卖家 id",
        "description": "A13AJ1GXFINAZ"
      },
      {
        "field": "└sellerName",
        "type": "String",
        "required": false,
        "name": "卖家名称",
        "description": "Mermaker"
      },
      {
        "field": "└fulfillment",
        "type": "String",
        "required": false,
        "name": "配送方式",
        "description": "FBA"
      },
      {
        "field": "└sellers",
        "type": "Integer",
        "required": false,
        "name": "卖家数",
        "description": "1"
      },
      {
        "field": "└skuList",
        "type": "List",
        "required": false,
        "name": "sku",
        "description": "[\"Color: Beige\",\"Size: 47 inches\"]"
      },
      {
        "field": "└marketplace",
        "type": "String",
        "required": false,
        "name": "String",
        "description": "见表 1.2"
      },
      {
        "field": "└title",
        "type": "String",
        "required": false,
        "name": "标题",
        "description": "mermaker Burritos Tortilla Blanket 2.0 Double Sided 47 inches for Adult and Kids,Giant Funny Realistic Food Throw Blanket,285 GSM Novelty Soft Flannel Taco Blanket (Yellow Blanket-Double Sided)"
      },
      {
        "field": "└features",
        "type": "List",
        "required": false,
        "name": "五点描述",
        "description": ""
      },
      {
        "field": "└overviews",
        "type": "String",
        "required": false,
        "name": "详情，json格式字符串",
        "description": ""
      },
      {
        "field": "└updatedTime",
        "type": "Long",
        "required": false,
        "name": "更新时间",
        "description": "1.60906E+12"
      },
      {
        "field": "└variationList",
        "type": "List",
        "required": false,
        "name": "变体",
        "description": "[{\"asin\":\"B07V5GB9B5\",\"attribute\":\"Beige\"},{\"asin\":\"B08H86SSSF\",\"attribute\":\"Cookie\"}]"
      },
      {
        "field": "└variations",
        "type": "Integer",
        "required": false,
        "name": "变体数量",
        "description": "14"
      },
      {
        "field": "└weight",
        "type": "String",
        "required": false,
        "name": "重量",
        "description": "15.2 ounces"
      },
      {
        "field": "└zoomImageUrl",
        "type": "String",
        "required": false,
        "name": "大图 URL",
        "description": "https://images-na.ssl-images-amazon.com/images/I/412616zl5YL .AC_US600.jpg"
      },
      {
        "field": "└subcategories",
        "type": "Object",
        "required": false,
        "name": "子类目信息",
        "description": ""
      },
      {
        "field": "└└rank",
        "type": "Integer",
        "required": false,
        "name": "子类目排名",
        "description": "1"
      },
      {
        "field": "└└code",
        "type": "String",
        "required": false,
        "name": "子类目code",
        "description": "17874234011"
      },
      {
        "field": "└└label",
        "type": "String",
        "required": false,
        "name": "子类目标签",
        "description": "Kids' Throw Blankets"
      },
      {
        "field": "└deliveryPrice",
        "type": "Float",
        "required": false,
        "name": "卖家运费,-1表示没有",
        "description": "4"
      },
      {
        "field": "└primePrice",
        "type": "Float",
        "required": false,
        "name": "prime价格，-1表示没有",
        "description": "42"
      },
      {
        "field": "└coupon",
        "type": "String",
        "required": false,
        "name": "优惠卷",
        "description": "[save $20]"
      },
      {
        "field": "salesTrendPoints",
        "type": "List",
        "required": false,
        "name": "ASIN sales Trend",
        "description": ""
      },
      {
        "field": "└month",
        "type": "String",
        "required": false,
        "name": "月份",
        "description": ""
      },
      {
        "field": "└price",
        "type": "Float",
        "required": false,
        "name": "价格",
        "description": ""
      },
      {
        "field": "└averagePrice",
        "type": "Float",
        "required": false,
        "name": "平均价格",
        "description": ""
      },
      {
        "field": "└parentUnitSales",
        "type": "Integer",
        "required": false,
        "name": "父体 销量",
        "description": ""
      },
      {
        "field": "└childUnitSales",
        "type": "Integer",
        "required": false,
        "name": "子体销量",
        "description": ""
      },
      {
        "field": "└parentSalesRevenue",
        "type": "Float",
        "required": false,
        "name": "父体销售额",
        "description": ""
      },
      {
        "field": "└childSalesRevenue",
        "type": "Float",
        "required": false,
        "name": "子体销售额度",
        "description": ""
      }
    ]
  },
  {
    "operation": "ASIN_SALES_PREDICTION",
    "domain": "asin",
    "responseShape": "object",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "市场,见表1.2",
        "description": "US"
      },
      {
        "field": "asin",
        "type": "String",
        "required": true,
        "name": "asin",
        "description": "B07Z82895W"
      }
    ],
    "responseFields": [
      {
        "field": "asinDetail",
        "type": "Object",
        "required": false,
        "name": "asin明细",
        "description": ""
      },
      {
        "field": "└asin",
        "type": "String",
        "required": false,
        "name": "asin",
        "description": "B00CFM8DI2"
      },
      {
        "field": "└title",
        "type": "String",
        "required": false,
        "name": "标题",
        "description": "Boot Bananas Original Shoe Deodorizer"
      },
      {
        "field": "└brand",
        "type": "String",
        "required": false,
        "name": "平台",
        "description": "Boot Bananas"
      },
      {
        "field": "└availableDate",
        "type": "Long",
        "required": false,
        "name": "上架时间",
        "description": "1397001600000"
      },
      {
        "field": "└category",
        "type": "String",
        "required": false,
        "name": "类目名称",
        "description": "Clothing, Shoes & Jewelry"
      },
      {
        "field": "└categoryId",
        "type": "String",
        "required": false,
        "name": "类目id",
        "description": "7141123011"
      },
      {
        "field": "└imageUrl",
        "type": "String",
        "required": false,
        "name": "图片URL",
        "description": "https://images-na.ssl-images-amazon.com/images/I/41AGxmiW-vL._AC_US600_.jpg"
      },
      {
        "field": "└ratings",
        "type": "Integer",
        "required": false,
        "name": "评分数",
        "description": "32004"
      },
      {
        "field": "└rating",
        "type": "Float",
        "required": false,
        "name": "评分值",
        "description": "4.6"
      },
      {
        "field": "dailyItemList",
        "type": "List",
        "required": false,
        "name": "日销量预测明细",
        "description": ""
      },
      {
        "field": "└date",
        "type": "String",
        "required": false,
        "name": "日期",
        "description": "45035"
      },
      {
        "field": "└bsr",
        "type": "Integer",
        "required": false,
        "name": "bsr",
        "description": "48614"
      },
      {
        "field": "└sales",
        "type": "Integer",
        "required": false,
        "name": "销量",
        "description": "14"
      },
      {
        "field": "└amount",
        "type": "Float",
        "required": false,
        "name": "销售额",
        "description": "200"
      },
      {
        "field": "└price",
        "type": "Float",
        "required": false,
        "name": "单价",
        "description": "20"
      },
      {
        "field": "monthItemList",
        "type": "List",
        "required": false,
        "name": "月销量预测明细",
        "description": ""
      },
      {
        "field": "└date",
        "type": "String",
        "required": false,
        "name": "日期",
        "description": "45017"
      },
      {
        "field": "└sales",
        "type": "Integer",
        "required": false,
        "name": "销量",
        "description": "14"
      },
      {
        "field": "└amount",
        "type": "Float",
        "required": false,
        "name": "销售额",
        "description": "200"
      },
      {
        "field": "└price",
        "type": "Float",
        "required": false,
        "name": "单价",
        "description": "20"
      }
    ]
  },
  {
    "operation": "BSR_SALES_PREDICTION",
    "domain": "asin",
    "responseShape": "object",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "市场,见表1.2",
        "description": "US"
      },
      {
        "field": "bsr",
        "type": "Integer",
        "required": true,
        "name": "大类排名",
        "description": "1024"
      },
      {
        "field": "categoryId",
        "type": "String",
        "required": true,
        "name": "一级类目节点，查产品类目返回",
        "description": "11260432011"
      }
    ],
    "responseFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": false,
        "name": "市场",
        "description": "US"
      },
      {
        "field": "bsr",
        "type": "Integer",
        "required": false,
        "name": "1",
        "description": "B07Z82895W"
      },
      {
        "field": "categoryLabel",
        "type": "String",
        "required": false,
        "name": "类目名称",
        "description": "2685"
      },
      {
        "field": "estDailySales",
        "type": "Integer",
        "required": false,
        "name": "预测日销量",
        "description": "99"
      },
      {
        "field": "estMonthSales",
        "type": "Integer",
        "required": false,
        "name": "预测30天销量",
        "description": "2965"
      },
      {
        "field": "itemList",
        "type": "List",
        "required": false,
        "name": "明细",
        "description": ""
      },
      {
        "field": "└bsr",
        "type": "Integer",
        "required": false,
        "name": "bsr",
        "description": "1"
      },
      {
        "field": "└estDailySales",
        "type": "Integer",
        "required": false,
        "name": "预测日销量",
        "description": "99"
      },
      {
        "field": "└estMonthSales",
        "type": "Integer",
        "required": false,
        "name": "预测30天销量",
        "description": "2965"
      }
    ]
  },
  {
    "operation": "ASIN_KEEPA_TREND",
    "domain": "asin",
    "responseShape": "object",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "市场",
        "description": "见表 1.2"
      },
      {
        "field": "asin",
        "type": "String",
        "required": true,
        "name": "asin",
        "description": "B08GHW4TBS"
      },
      {
        "field": "startTimestamp",
        "type": "Long",
        "required": false,
        "name": "Trend Data Start Timestamp",
        "description": ""
      },
      {
        "field": "endTimestamp",
        "type": "Long",
        "required": false,
        "name": "Trend Data End Timestamp",
        "description": ""
      },
      {
        "field": "dailyLatest",
        "type": "Boolean",
        "required": false,
        "name": "Only Get Daily Latest Data",
        "description": ""
      }
    ],
    "responseFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": false,
        "name": "市场",
        "description": "见表 1.2"
      },
      {
        "field": "asin",
        "type": "String",
        "required": false,
        "name": "asin",
        "description": "B07V34QQ3C"
      },
      {
        "field": "dataAsin",
        "type": "String",
        "required": false,
        "name": "实际返回Keepa数据的ASIN",
        "description": "B07V34QQ3C"
      },
      {
        "field": "parentAsin",
        "type": "String",
        "required": false,
        "name": "父体ASIN",
        "description": "B0CWW9N7QW"
      },
      {
        "field": "variationAsins",
        "type": "List",
        "required": false,
        "name": "变体ASIN列表",
        "description": "[\"B0CN2PBVNS\",\"B0BT4PMNY4\",\"B0C6FYKC3D\",\"B0CSLMG2TF\",\"B0CGGPC6G3\",\"B0BXG8L46Y\",\"B0CRSZGN9L\",\"B07V34QQ3C\"]"
      },
      {
        "field": "rootCategory",
        "type": "String",
        "required": false,
        "name": "BSR大类节点ID",
        "description": "172282"
      },
      {
        "field": "rootCategoryLabel",
        "type": "String",
        "required": false,
        "name": "跟类目",
        "description": "Electronics"
      },
      {
        "field": "salesRankReference",
        "type": "String",
        "required": false,
        "name": "排名节点ID",
        "description": "541966"
      },
      {
        "field": "salesRankReferenceHistory",
        "type": "List",
        "required": false,
        "name": "排名节点变动历史",
        "description": "PairStrDto 趋势字符串数据结构"
      },
      {
        "field": "nodeIdPath",
        "type": "String",
        "required": false,
        "name": "上架类目全路径",
        "description": "172282:541966:13896617011:565098:13896597011"
      },
      {
        "field": "nodeLabelPath",
        "type": "String",
        "required": false,
        "name": "上架类目名称全路径",
        "description": "Electronics:Computers & Accessories:Computers & Tablets:Desktops:Towers"
      },
      {
        "field": "productStatus",
        "type": "String",
        "required": false,
        "name": "商品状态",
        "description": "STANDARD:everything accessibleDOWNLOADABLE:no marketplace/3rd party price dataEBOOK:no price data and sales rank accessibleINACCESSIBLE:no data accessibleINVALID:invalid or deprecated asinVARIATION_PARENT:product is a parent ASINUNKNOWN:null of status"
      },
      {
        "field": "availabilityAmazon",
        "type": "String",
        "required": false,
        "name": "亚马逊跟卖转态",
        "description": "-1"
      },
      {
        "field": "title",
        "type": "String",
        "required": false,
        "name": "标题",
        "description": "iBUYPOWER Gaming PC Computer Desktop Element 9260 (Intel Core i7-9700F 3.0Ghz, NVIDIA GeForce GTX 1660 Ti 6GB, 16GB DDR4, 240GB SSD, 1TB HDD, Wi-Fi & Windows 10 Home) Black"
      },
      {
        "field": "brand",
        "type": "String",
        "required": false,
        "name": "品牌",
        "description": "iBUYPOWER"
      },
      {
        "field": "asinUrl",
        "type": "String",
        "required": false,
        "name": "ASIN链接",
        "description": "https://www.amazon.com/dp/B07V34QQ3C"
      },
      {
        "field": "brandUrl",
        "type": "String",
        "required": false,
        "name": "品牌链接",
        "description": "https://www.amazon.com/s?k=iBUYPOWER"
      },
      {
        "field": "salesRankUrl",
        "type": "String",
        "required": false,
        "name": "销售排名链接",
        "description": "https://www.amazon.com/b/?node=541966"
      },
      {
        "field": "imageUrl",
        "type": "String",
        "required": false,
        "name": "商品缩略图200*200",
        "description": "https://images-na.ssl-images-amazon.com/images/I/711nEj5l5SL._AC_US200_.jpg"
      },
      {
        "field": "zoomImageUrl",
        "type": "String",
        "required": false,
        "name": "商品大图600*600",
        "description": "https://images-na.ssl-images-amazon.com/images/I/711nEj5l5SL._AC_US600_.jpg"
      },
      {
        "field": "imageUrls",
        "type": "List",
        "required": false,
        "name": "商品图片列表",
        "description": "[\"https://images-na.ssl-images-amazon.com/images/I/711nEj5l5SL._AC_US200_.jpg\",\"https://images-na.ssl-images-amazon.com/images/I/61bpfnvHjqL._AC_US200_.jpg\",......]"
      },
      {
        "field": "dimensions",
        "type": "String",
        "required": false,
        "name": "净尺寸",
        "description": "97"
      },
      {
        "field": "weight",
        "type": "String",
        "required": false,
        "name": "净重量",
        "description": "1063280"
      },
      {
        "field": "weightGram",
        "type": "Integer",
        "required": false,
        "name": "净重数值 单位统一为：克(g)",
        "description": "1055398:1063252:1063280"
      },
      {
        "field": "pkgDimensions",
        "type": "String",
        "required": false,
        "name": "打包尺寸",
        "description": "22 x 19.9 x 12.4 inches"
      },
      {
        "field": "pkgDimensionsSize",
        "type": "List",
        "required": false,
        "name": "打包尺寸 长/宽/高 单位统一为：厘米(cm)",
        "description": "[558,506,316]"
      },
      {
        "field": "pkgWeight",
        "type": "String",
        "required": false,
        "name": "打包重量",
        "description": "0.11 pounds"
      },
      {
        "field": "pkgWeightGram",
        "type": "Integer",
        "required": false,
        "name": "打包重量数值 单位统一为：克(g)",
        "description": "13660"
      },
      {
        "field": "fbaFees",
        "type": "Float",
        "required": false,
        "name": "FBA总费用",
        "description": "26.11"
      },
      {
        "field": "fbaItems",
        "type": "String",
        "required": false,
        "name": "FBA费用项明细JSON串，包含：仓储费，仓储费税，运送打包费，运送打包费税",
        "description": "\"{\\\"pickAndPackFeeTax\\\":0,\\\"storageFee\\\":0,\\\"storageFeeTax\\\":0,\\\"pickAndPackFee\\\":26.11}\""
      },
      {
        "field": "numberOfPages",
        "type": "Integer",
        "required": false,
        "name": "在第几页",
        "description": "-1"
      },
      {
        "field": "numberOfItems",
        "type": "Integer",
        "required": false,
        "name": "在第几个",
        "description": "1"
      },
      {
        "field": "price",
        "type": "List",
        "required": false,
        "name": "价格趋势",
        "description": "见 PairNumberDto 趋势数字数据结构"
      },
      {
        "field": "dealPrice",
        "type": "List",
        "required": false,
        "name": "成交价趋势",
        "description": "见 PairNumberDto 趋势数字数据结构"
      },
      {
        "field": "buyBox",
        "type": "List",
        "required": false,
        "name": "黄金购物车价格趋势",
        "description": "见 PairNumberDto 趋势数字数据结构"
      },
      {
        "field": "priceList",
        "type": "List",
        "required": false,
        "name": "划线价格",
        "description": "见 PairNumberDto 趋势数字数据结构"
      },
      {
        "field": "buyBoxSellerIdHistory",
        "type": "List",
        "required": false,
        "name": "黄金购物车卖家Id历史趋势",
        "description": "PairStrDto 趋势字符串数据结构"
      },
      {
        "field": "bsr",
        "type": "List",
        "required": false,
        "name": "大类BSR排名历史趋势",
        "description": "见 PairNumberDto 趋势数字数据结构"
      },
      {
        "field": "subSalesRank",
        "type": "List",
        "required": false,
        "name": "小类排名趋势数据",
        "description": "见 SubRankTrendDto 小类排名趋势"
      },
      {
        "field": "reviews",
        "type": "List",
        "required": false,
        "name": "评分数趋势数据",
        "description": "见 PairNumberDto 趋势数字数据结构"
      },
      {
        "field": "rating",
        "type": "List",
        "required": false,
        "name": "评分值趋势数据",
        "description": "见 PairNumberDto 趋势数字数据结构"
      },
      {
        "field": "sellers",
        "type": "List",
        "required": false,
        "name": "卖家数趋势数据",
        "description": "见 PairNumberDto 趋势数字数据结构"
      }
    ]
  },
  {
    "operation": "KEYWORD_RESEARCH",
    "domain": "keyword",
    "responseShape": "page",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "市场",
        "description": "见表 1.2"
      },
      {
        "field": "month",
        "type": "String",
        "required": false,
        "name": "筛选日期,yyyyMM格式，支持近24个月的",
        "description": "202203"
      },
      {
        "field": "departments",
        "type": "List",
        "required": false,
        "name": "查询类目，见关键词选品类目接口，传递code",
        "description": "[\"automotive\",\"baby-products\"]"
      },
      {
        "field": "keywords",
        "type": "String",
        "required": false,
        "name": "关键词",
        "description": "N95"
      },
      {
        "field": "excludeKeywords",
        "type": "String",
        "required": false,
        "name": "排除的关键字",
        "description": "portable"
      },
      {
        "field": "minSearches",
        "type": "Integer",
        "required": false,
        "name": "最小月搜索量",
        "description": "100"
      },
      {
        "field": "maxSearches",
        "type": "Integer",
        "required": false,
        "name": "最大月搜索量",
        "description": "300"
      },
      {
        "field": "minSearchesCr",
        "type": "Float",
        "required": false,
        "name": "最小月搜索量增长率",
        "description": "10"
      },
      {
        "field": "maxSearchesCr",
        "type": "Float",
        "required": false,
        "name": "最大月搜索量增长率",
        "description": "50.8"
      },
      {
        "field": "minProducts",
        "type": "Integer",
        "required": false,
        "name": "最小商品数",
        "description": "10"
      },
      {
        "field": "maxProducts",
        "type": "Integer",
        "required": false,
        "name": "最大商品数",
        "description": "90"
      },
      {
        "field": "minPurchases",
        "type": "Integer",
        "required": false,
        "name": "最小购买量",
        "description": "100"
      },
      {
        "field": "maxPurchases",
        "type": "Integer",
        "required": false,
        "name": "最大购买量",
        "description": "500"
      },
      {
        "field": "minPurchaseRate",
        "type": "Float",
        "required": false,
        "name": "最小购买率",
        "description": "3.2"
      },
      {
        "field": "maxPurchaseRate",
        "type": "Float",
        "required": false,
        "name": "最大购买率",
        "description": "10.5"
      },
      {
        "field": "withYearlyGrowth",
        "type": "Boolean",
        "required": false,
        "name": "新细分市场",
        "description": "false"
      },
      {
        "field": "minSearchMonthCv",
        "type": "Integer",
        "required": false,
        "name": "最小月搜索量同比增长值",
        "description": "1000"
      },
      {
        "field": "maxSearchMonthCv",
        "type": "Integer",
        "required": false,
        "name": "最大月搜索量同比增长值",
        "description": "3000"
      },
      {
        "field": "minSearchMonthCr",
        "type": "Float",
        "required": false,
        "name": "最小月搜索量同比增长率",
        "description": "5.3"
      },
      {
        "field": "maxSearchMonthCr",
        "type": "Float",
        "required": false,
        "name": "最大月搜索量同比增长率",
        "description": "30.1"
      },
      {
        "field": "minSearchNearlyCv",
        "type": "Integer",
        "required": false,
        "name": "最小月搜索量近3个月增长值",
        "description": "6000"
      },
      {
        "field": "maxSearchNearlyCv",
        "type": "Integer",
        "required": false,
        "name": "最大月搜索量近3个月增长值",
        "description": "20000"
      },
      {
        "field": "minSearchNearlyCr",
        "type": "Float",
        "required": false,
        "name": "最小月搜索量近3个月增长率",
        "description": "10.3"
      },
      {
        "field": "maxSearchNearlyCr",
        "type": "Float",
        "required": false,
        "name": "最大月搜索量近3个月增长率",
        "description": "20.4"
      },
      {
        "field": "marketPeriod",
        "type": "String",
        "required": false,
        "name": "市场周期",
        "description": "见表1.7"
      },
      {
        "field": "minAvgPrice",
        "type": "Float",
        "required": false,
        "name": "最小均价",
        "description": "20"
      },
      {
        "field": "maxAvgPrice",
        "type": "Float",
        "required": false,
        "name": "最大均价",
        "description": "30.3"
      },
      {
        "field": "minRatings",
        "type": "Integer",
        "required": false,
        "name": "最小评分数",
        "description": "2000"
      },
      {
        "field": "maxRatings",
        "type": "Integer",
        "required": false,
        "name": "最大评分数",
        "description": "3000"
      },
      {
        "field": "minRating",
        "type": "Float",
        "required": false,
        "name": "最小评分值",
        "description": "3.2"
      },
      {
        "field": "maxRating",
        "type": "Float",
        "required": false,
        "name": "最大评分值",
        "description": "4.1"
      },
      {
        "field": "minBid",
        "type": "Float",
        "required": false,
        "name": "最小PPC竞价",
        "description": "6.2"
      },
      {
        "field": "maxBid",
        "type": "Float",
        "required": false,
        "name": "最大PPC竞价",
        "description": "10.6"
      },
      {
        "field": "minAraClickRate",
        "type": "Float",
        "required": false,
        "name": "最小点击集中度",
        "description": "20.1"
      },
      {
        "field": "maxAraClickRate",
        "type": "Float",
        "required": false,
        "name": "最大点击集中度",
        "description": "56.4"
      },
      {
        "field": "minGoodsValue",
        "type": "Float",
        "required": false,
        "name": "最小货流值",
        "description": "10.1"
      },
      {
        "field": "maxGoodsValue",
        "type": "Float",
        "required": false,
        "name": "最大货流值",
        "description": "41.1"
      },
      {
        "field": "minSupplyDemandRatio",
        "type": "Float",
        "required": false,
        "name": "最小供需比",
        "description": "5.6"
      },
      {
        "field": "maxSupplyDemandRatio",
        "type": "Float",
        "required": false,
        "name": "最大供需比",
        "description": "10.4"
      },
      {
        "field": "minWordCount",
        "type": "Integer",
        "required": false,
        "name": "最小单词个数",
        "description": "1"
      },
      {
        "field": "maxWordCount",
        "type": "Integer",
        "required": false,
        "name": "最大单词个数",
        "description": "3"
      },
      {
        "field": "page",
        "type": "Integer",
        "required": false,
        "name": "页码，从 1 开始",
        "description": "默认：1"
      },
      {
        "field": "size",
        "type": "Integer",
        "required": false,
        "name": "每页条数，默认15",
        "description": "最大：15"
      },
      {
        "field": "order",
        "type": "Object",
        "required": false,
        "name": "排序",
        "description": ""
      },
      {
        "field": "└field",
        "type": "String",
        "required": false,
        "name": "排序字段",
        "description": "见表1.8"
      },
      {
        "field": "└desc",
        "type": "boolean",
        "required": false,
        "name": "true为降序 false为升序",
        "description": "默认降序"
      }
    ],
    "responseFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": false,
        "name": "市场",
        "description": "US"
      },
      {
        "field": "keywords",
        "type": "String",
        "required": false,
        "name": "关键词",
        "description": "polaroid cameras"
      },
      {
        "field": "searches",
        "type": "Integer",
        "required": false,
        "name": "搜索量",
        "description": "141356"
      },
      {
        "field": "clicks",
        "type": "Integer",
        "required": false,
        "name": "点击量",
        "description": "在某个关键词搜索结果页中被点击的总次数非单个ASIN在关键词下的点击量"
      },
      {
        "field": "impressions",
        "type": "Long",
        "required": false,
        "name": "展示量",
        "description": "在某个关键词搜索结果页中所有ASIN的总展示次数非单个ASIN在关键词下的曝光量"
      },
      {
        "field": "purchases",
        "type": "Integer",
        "required": false,
        "name": "月购买量",
        "description": "4029"
      },
      {
        "field": "growth",
        "type": "Float",
        "required": false,
        "name": "增长率",
        "description": "-25.482092"
      },
      {
        "field": "purchaseRate",
        "type": "Float",
        "required": false,
        "name": "月购买率",
        "description": "0.0285"
      },
      {
        "field": "products",
        "type": "Integer",
        "required": false,
        "name": "产品数",
        "description": "173"
      },
      {
        "field": "supplyDemandRatio",
        "type": "Float",
        "required": false,
        "name": "供需比",
        "description": "817.09"
      },
      {
        "field": "searchDepartments",
        "type": "List",
        "required": false,
        "name": "类目",
        "description": ""
      },
      {
        "field": "└code",
        "type": "String",
        "required": false,
        "name": "类目代码",
        "description": "electronics"
      },
      {
        "field": "└label",
        "type": "String",
        "required": false,
        "name": "类目名称",
        "description": "Electronics"
      },
      {
        "field": "└total",
        "type": "Integer",
        "required": false,
        "name": "类目总计",
        "description": "141356"
      },
      {
        "field": "└ratio",
        "type": "Float",
        "required": false,
        "name": "类目占比",
        "description": "1"
      },
      {
        "field": "month",
        "type": "String",
        "required": false,
        "name": "查询月份",
        "description": "2022.01"
      },
      {
        "field": "supplement",
        "type": "String",
        "required": false,
        "name": "是否属于补充关键词",
        "description": "N"
      },
      {
        "field": "searchMonthlyCv",
        "type": "Integer",
        "required": false,
        "name": "关键词同比增长",
        "description": "139749"
      },
      {
        "field": "searchMonthlyCr",
        "type": "Float",
        "required": false,
        "name": "关键词同比增长率",
        "description": "8696.27"
      },
      {
        "field": "searchNearlyCv",
        "type": "Integer",
        "required": false,
        "name": "关键词近3个月增长值",
        "description": "-48338"
      },
      {
        "field": "searchNearlyCr",
        "type": "Float",
        "required": false,
        "name": "关键词近3个月增长率",
        "description": "-25.48"
      },
      {
        "field": "currency",
        "type": "String",
        "required": false,
        "name": "货币",
        "description": "$"
      },
      {
        "field": "avgPrice",
        "type": "Float",
        "required": false,
        "name": "平均价格",
        "description": "116.24"
      },
      {
        "field": "avgRatings",
        "type": "Integer",
        "required": false,
        "name": "平均评分数",
        "description": "2584"
      },
      {
        "field": "avgRating",
        "type": "Float",
        "required": false,
        "name": "平均评论数",
        "description": "4.5"
      },
      {
        "field": "relationAsinList",
        "type": "List",
        "required": false,
        "name": "关键词关联asin",
        "description": "4.8"
      },
      {
        "field": "└price",
        "type": "Float",
        "required": false,
        "name": "价格",
        "description": "59.95"
      },
      {
        "field": "└ratings",
        "type": "Integer",
        "required": false,
        "name": "评分数",
        "description": "20115"
      },
      {
        "field": "└rating",
        "type": "Float",
        "required": false,
        "name": "评分",
        "description": "4.7"
      },
      {
        "field": "bidMin",
        "type": "Float",
        "required": false,
        "name": "bid最小价格",
        "description": "0.987"
      },
      {
        "field": "bidMax",
        "type": "Float",
        "required": false,
        "name": "bid最大价格",
        "description": "2.54"
      },
      {
        "field": "bid",
        "type": "Float",
        "required": false,
        "name": "bid价格",
        "description": "1.26"
      },
      {
        "field": "araClickRate",
        "type": "Float",
        "required": false,
        "name": "点击垄断率",
        "description": "0.2633"
      },
      {
        "field": "araShareRate",
        "type": "Float",
        "required": false,
        "name": "共享转化率",
        "description": "0.2633"
      },
      {
        "field": "araAsinList",
        "type": "List",
        "required": false,
        "name": "点击前三ASIN",
        "description": ""
      },
      {
        "field": "└asin",
        "type": "String",
        "required": false,
        "name": "asin",
        "description": "B099VDRGG1"
      },
      {
        "field": "└title",
        "type": "String",
        "required": false,
        "name": "title",
        "description": "Fujifilm Instax Mini 9"
      },
      {
        "field": "└imageUrl",
        "type": "String",
        "required": false,
        "name": "图片",
        "description": "https://m.media-amazon.com/images/I/51aZiZaicYL._AC_US200_.jpg"
      },
      {
        "field": "└clickRate",
        "type": "Double",
        "required": false,
        "name": "点击率",
        "description": "0.116"
      },
      {
        "field": "└conversionShareRate",
        "type": "Double",
        "required": false,
        "name": "转化率",
        "description": "0.1217"
      },
      {
        "field": "goodsValue",
        "type": "Float",
        "required": false,
        "name": "货流值",
        "description": "0.0108"
      },
      {
        "field": "brands",
        "type": "List",
        "required": false,
        "name": "TOP3 品牌",
        "description": "[\"LEGO\",\"Jorumo\",\"Nifeliz\"]"
      },
      {
        "field": "categories",
        "type": "List",
        "required": false,
        "name": "TOP3 类目",
        "description": "[\"Toys\",\"Home\",\"Mobile_Apps\"]"
      },
      {
        "field": "titleDensityExact",
        "type": "String",
        "required": false,
        "name": "标题密度首页商品包含该关键词的数量（不含广告位）",
        "description": "21"
      },
      {
        "field": "marketPeriod",
        "type": "String",
        "required": false,
        "name": "市场周期",
        "description": "S11,S12"
      },
      {
        "field": "brand",
        "type": "String",
        "required": false,
        "name": "品牌",
        "description": "Fujifilm"
      },
      {
        "field": "hasBrandWord",
        "type": "Boolean",
        "required": false,
        "name": "是否存在品牌词",
        "description": "false"
      },
      {
        "field": "keywordCn",
        "type": "String",
        "required": false,
        "name": "中文翻译",
        "description": "宝丽来相机"
      }
    ]
  },
  {
    "operation": "KEYWORD_RESEARCH_TRENDS",
    "domain": "keyword",
    "responseShape": "list",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "市场",
        "description": "见表 1.2"
      },
      {
        "field": "keyword",
        "type": "String",
        "required": true,
        "name": "",
        "description": ""
      }
    ],
    "responseFields": [
      {
        "field": "time",
        "type": "String",
        "required": false,
        "name": "时间",
        "description": ""
      },
      {
        "field": "keywrod",
        "type": "String",
        "required": false,
        "name": "关键词",
        "description": ""
      },
      {
        "field": "keywrodCn",
        "type": "String",
        "required": false,
        "name": "关键词-中文",
        "description": ""
      },
      {
        "field": "keywrodJp",
        "type": "String",
        "required": false,
        "name": "关键词-日文",
        "description": ""
      },
      {
        "field": "search",
        "type": "Integer",
        "required": false,
        "name": "搜索量",
        "description": ""
      },
      {
        "field": "purchase",
        "type": "BigDecimal",
        "required": false,
        "name": "购买量",
        "description": ""
      },
      {
        "field": "purchaseRate",
        "type": "BigDecimal",
        "required": false,
        "name": "购买率",
        "description": ""
      },
      {
        "field": "yearlyGrowth",
        "type": "BigDecimal",
        "required": false,
        "name": "同比增长率",
        "description": ""
      },
      {
        "field": "chainGrowth",
        "type": "BigDecimal",
        "required": false,
        "name": "环比增长率",
        "description": ""
      },
      {
        "field": "threeMonthGrowth",
        "type": "BigDecimal",
        "required": false,
        "name": "三个月增长率",
        "description": ""
      }
    ]
  },
  {
    "operation": "KEYWORD_MINER",
    "domain": "keyword",
    "responseShape": "page",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "市场",
        "description": "见表 1.2"
      },
      {
        "field": "historyDate",
        "type": "String",
        "required": false,
        "name": "历史日期，yyyyMM格式，最近30天不传或传空字符串",
        "description": "202201"
      },
      {
        "field": "keyword",
        "type": "String",
        "required": true,
        "name": "关键词",
        "description": ""
      },
      {
        "field": "keywordList",
        "type": "List",
        "required": false,
        "name": "批量查询关键词",
        "description": "[\"phone stand\"]"
      },
      {
        "field": "minSearch",
        "type": "Integer",
        "required": false,
        "name": "最小搜索量",
        "description": "543"
      },
      {
        "field": "maxSearch",
        "type": "Integer",
        "required": false,
        "name": "最大搜索量",
        "description": "23453"
      },
      {
        "field": "minPurchases",
        "type": "Integer",
        "required": false,
        "name": "最小购买量",
        "description": "6"
      },
      {
        "field": "maxPurchases",
        "type": "Integer",
        "required": false,
        "name": "最大购买量",
        "description": "34"
      },
      {
        "field": "minPurchasesRate",
        "type": "Float",
        "required": false,
        "name": "最小购买率",
        "description": "3"
      },
      {
        "field": "maxPurchasesRate",
        "type": "Float",
        "required": false,
        "name": "最大购买率",
        "description": "43"
      },
      {
        "field": "minSPR",
        "type": "Integer",
        "required": false,
        "name": "最小SPR",
        "description": "2"
      },
      {
        "field": "maxSPR",
        "type": "Integer",
        "required": false,
        "name": "最大SPR",
        "description": "16"
      },
      {
        "field": "minTitleDensity",
        "type": "Integer",
        "required": false,
        "name": "最小标题密度",
        "description": "2"
      },
      {
        "field": "maxTitleDensity",
        "type": "Integer",
        "required": false,
        "name": "最大标题密度",
        "description": "23"
      },
      {
        "field": "minRelevancy",
        "type": "Float",
        "required": false,
        "name": "最小相关度",
        "description": "23，最小0"
      },
      {
        "field": "maxRelevancy",
        "type": "Float",
        "required": false,
        "name": "最大相关度",
        "description": "90，最大100"
      },
      {
        "field": "minSearchRank",
        "type": "Integer",
        "required": false,
        "name": "最小搜索排名",
        "description": "33"
      },
      {
        "field": "maxSearchRank",
        "type": "Integer",
        "required": false,
        "name": "最大搜索排名",
        "description": "3223"
      },
      {
        "field": "minProducts",
        "type": "Integer",
        "required": false,
        "name": "最小商品数",
        "description": "54"
      },
      {
        "field": "maxProducts",
        "type": "Integer",
        "required": false,
        "name": "最大商品数",
        "description": "324"
      },
      {
        "field": "minSupplyDemandRatio",
        "type": "Float",
        "required": false,
        "name": "最小供需比",
        "description": "11.2"
      },
      {
        "field": "maxSupplyDemandRatio",
        "type": "Float",
        "required": false,
        "name": "最大供需比",
        "description": "45.2"
      },
      {
        "field": "minAdProducts",
        "type": "Integer",
        "required": false,
        "name": "最小广告竞品数",
        "description": "123"
      },
      {
        "field": "maxAdProducts",
        "type": "Integer",
        "required": false,
        "name": "最大广告竞品数",
        "description": "345"
      },
      {
        "field": "minWordCount",
        "type": "Integer",
        "required": false,
        "name": "最小单词个数",
        "description": "2"
      },
      {
        "field": "maxWordCount",
        "type": "Integer",
        "required": false,
        "name": "最大单词个数",
        "description": "4"
      },
      {
        "field": "minMonopolyClickRate",
        "type": "Float",
        "required": false,
        "name": "最小点击集中度",
        "description": "23.4"
      },
      {
        "field": "maxMonopolyClickRate",
        "type": "Float",
        "required": false,
        "name": "最大点击集中度",
        "description": "53.1"
      },
      {
        "field": "minBid",
        "type": "Float",
        "required": false,
        "name": "最小ppc竞价",
        "description": "10.2"
      },
      {
        "field": "maxBid",
        "type": "Float",
        "required": false,
        "name": "最大ppc竞价",
        "description": "23.1"
      },
      {
        "field": "minPrice",
        "type": "Float",
        "required": false,
        "name": "最小均价",
        "description": "43.3"
      },
      {
        "field": "maxPrice",
        "type": "Float",
        "required": false,
        "name": "最大均价",
        "description": "234.2"
      },
      {
        "field": "minRatings",
        "type": "Integer",
        "required": false,
        "name": "最小评分数",
        "description": "100"
      },
      {
        "field": "maxRatings",
        "type": "Integer",
        "required": false,
        "name": "最大评分数",
        "description": "399"
      },
      {
        "field": "minRating",
        "type": "Float",
        "required": false,
        "name": "最小评分值",
        "description": "3"
      },
      {
        "field": "maxRating",
        "type": "Float",
        "required": false,
        "name": "最大评分值",
        "description": "4.9"
      },
      {
        "field": "amazonChoice",
        "type": "Boolean",
        "required": false,
        "name": "亚马逊推荐词",
        "description": "true"
      },
      {
        "field": "filterRootWord",
        "type": "Integer",
        "required": false,
        "name": "过滤词根 0包含所有 1只包含词根",
        "description": "0"
      },
      {
        "field": "matchType",
        "type": "Integer",
        "required": false,
        "name": "2: 广泛匹配, 3: 词组匹配",
        "description": "2"
      },
      {
        "field": "includeKeywords",
        "type": "List",
        "required": false,
        "name": "包含的词",
        "description": "[\"phone stand\"]"
      },
      {
        "field": "excludeKeywords",
        "type": "List",
        "required": false,
        "name": "排除的词",
        "description": "[\"phone stand\"]"
      },
      {
        "field": "page",
        "type": "Integer",
        "required": false,
        "name": "页码，从 1 开始",
        "description": "默认：1"
      },
      {
        "field": "size",
        "type": "Integer",
        "required": false,
        "name": "每页条数",
        "description": "默认：50，最大：100"
      },
      {
        "field": "order",
        "type": "Object",
        "required": false,
        "name": "排序",
        "description": ""
      },
      {
        "field": "└field",
        "type": "String",
        "required": false,
        "name": "排序字段，加入筛序条件之后，不能以相关度排序",
        "description": "见表2.4"
      },
      {
        "field": "└desc",
        "type": "boolean",
        "required": false,
        "name": "true为降序 false为升序",
        "description": "默认降序"
      }
    ],
    "responseFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": false,
        "name": "市场，见表 1.2",
        "description": "US"
      },
      {
        "field": "keyword",
        "type": "String",
        "required": false,
        "name": "关键词",
        "description": "phone stand for recording"
      },
      {
        "field": "keywordCn",
        "type": "String",
        "required": false,
        "name": "关键词中文翻译",
        "description": "用于录音的电话支架"
      },
      {
        "field": "keywordJp",
        "type": "String",
        "required": false,
        "name": "关键词英文翻译",
        "description": "録音用電話スタンド"
      },
      {
        "field": "departments",
        "type": "List",
        "required": false,
        "name": "类目",
        "description": ""
      },
      {
        "field": "└code",
        "type": "String",
        "required": false,
        "name": "类目代码",
        "description": "electronics"
      },
      {
        "field": "└label",
        "type": "String",
        "required": false,
        "name": "类目名称",
        "description": "Electronics"
      },
      {
        "field": "month",
        "type": "String",
        "required": false,
        "name": "搜索月份",
        "description": "2022.01"
      },
      {
        "field": "supplement",
        "type": "String",
        "required": false,
        "name": "是否属于补充关键词（无当前月搜索量）",
        "description": "N"
      },
      {
        "field": "searches",
        "type": "Integer",
        "required": false,
        "name": "搜索量",
        "description": "21582"
      },
      {
        "field": "purchases",
        "type": "Integer",
        "required": false,
        "name": "月购买量",
        "description": "1996"
      },
      {
        "field": "purchaseRate",
        "type": "Float",
        "required": false,
        "name": "月购买率",
        "description": "0.0925"
      },
      {
        "field": "monopolyClickRate",
        "type": "Float",
        "required": false,
        "name": "点击垄断率",
        "description": "0.3"
      },
      {
        "field": "products",
        "type": "Integer",
        "required": false,
        "name": "商品数",
        "description": "1645"
      },
      {
        "field": "adProducts",
        "type": "Integer",
        "required": false,
        "name": "广告竞品数",
        "description": "34"
      },
      {
        "field": "supplyDemandRatio",
        "type": "Float",
        "required": false,
        "name": "供需比",
        "description": "13.12"
      },
      {
        "field": "avgPrice",
        "type": "Float",
        "required": false,
        "name": "平均价格",
        "description": "36.14"
      },
      {
        "field": "avgRatings",
        "type": "Integer",
        "required": false,
        "name": "平均评分数",
        "description": "12223"
      },
      {
        "field": "avgRating",
        "type": "Float",
        "required": false,
        "name": "平均评分值",
        "description": "4.5"
      },
      {
        "field": "bidMin",
        "type": "Float",
        "required": false,
        "name": "最小PPC价格",
        "description": "1.34"
      },
      {
        "field": "bidMax",
        "type": "Float",
        "required": false,
        "name": "最大PPC价格",
        "description": "3.21"
      },
      {
        "field": "bid",
        "type": "Float",
        "required": false,
        "name": "PPC价格",
        "description": "1.6"
      },
      {
        "field": "cvsShareRate",
        "type": "Float",
        "required": false,
        "name": "转化共享率",
        "description": "0.3084"
      },
      {
        "field": "wordCount",
        "type": "Integer",
        "required": false,
        "name": "单词个数",
        "description": "4"
      },
      {
        "field": "titleDensity",
        "type": "Integer",
        "required": false,
        "name": "标题密度",
        "description": "42.9"
      },
      {
        "field": "spr",
        "type": "Integer",
        "required": false,
        "name": "SPR",
        "description": "6"
      },
      {
        "field": "relevancy",
        "type": "Double",
        "required": false,
        "name": "相关度",
        "description": "28.6"
      },
      {
        "field": "amazonChoice",
        "type": "Boolean",
        "required": false,
        "name": "亚马逊推荐词 true是的 false不是",
        "description": "false"
      },
      {
        "field": "searchRank",
        "type": "Integer",
        "required": false,
        "name": "搜索排名",
        "description": "17910"
      },
      {
        "field": "└code",
        "type": "String",
        "required": false,
        "name": "类目代码",
        "description": "electronics"
      },
      {
        "field": "└label",
        "type": "String",
        "required": false,
        "name": "类目名称",
        "description": "Electronics"
      },
      {
        "field": "clicks",
        "type": "Integer",
        "required": false,
        "name": "点击量",
        "description": "10"
      },
      {
        "field": "impressions",
        "type": "Long",
        "required": false,
        "name": "展示量",
        "description": "20"
      }
    ]
  },
  {
    "operation": "KEYWORD_TRAFFIC_EXTEND",
    "domain": "keyword",
    "responseShape": "page",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "市场,见表1.2",
        "description": "US"
      },
      {
        "field": "historyDate",
        "type": "String",
        "required": false,
        "name": "历史日期，yyyyMM格式，最近30天不传或传空字符串",
        "description": "202201"
      },
      {
        "field": "asinList",
        "type": "List",
        "required": true,
        "name": "asin列表(最多20)",
        "description": "[\"B07Z82895W\"]"
      },
      {
        "field": "queryType",
        "type": "Integer",
        "required": false,
        "name": "查询方式 0 所有变体 1畅销变体 2当前变体，默认2",
        "description": "2"
      },
      {
        "field": "minSearches",
        "type": "Integer",
        "required": false,
        "name": "最小月搜索量",
        "description": "100"
      },
      {
        "field": "maxSearches",
        "type": "Integer",
        "required": false,
        "name": "最大月搜索量",
        "description": "300"
      },
      {
        "field": "minSearchRank",
        "type": "Integer",
        "required": false,
        "name": "最小搜索排名",
        "description": "33"
      },
      {
        "field": "maxSearchRank",
        "type": "Integer",
        "required": false,
        "name": "最大搜索排名",
        "description": "3223"
      },
      {
        "field": "minPurchases",
        "type": "Integer",
        "required": false,
        "name": "最小购买量",
        "description": "6"
      },
      {
        "field": "maxPurchases",
        "type": "Integer",
        "required": false,
        "name": "最大购买量",
        "description": "34"
      },
      {
        "field": "minPurchaseRate",
        "type": "Float",
        "required": false,
        "name": "最小购买率",
        "description": "3"
      },
      {
        "field": "maxPurchaseRate",
        "type": "Float",
        "required": false,
        "name": "最大购买率",
        "description": "43"
      },
      {
        "field": "minProducts",
        "type": "Integer",
        "required": false,
        "name": "最小商品数",
        "description": "10"
      },
      {
        "field": "maxProducts",
        "type": "Integer",
        "required": false,
        "name": "最大商品数",
        "description": "90"
      },
      {
        "field": "minSupplyDemandRatio",
        "type": "Float",
        "required": false,
        "name": "最小供需比",
        "description": "11.2"
      },
      {
        "field": "maxSupplyDemandRatio",
        "type": "Float",
        "required": false,
        "name": "最大供需比",
        "description": "45.2"
      },
      {
        "field": "minBid",
        "type": "Float",
        "required": false,
        "name": "最小ppc竞价",
        "description": "10.2"
      },
      {
        "field": "maxBid",
        "type": "Float",
        "required": false,
        "name": "最大ppc竞价",
        "description": "23.1"
      },
      {
        "field": "minAdProducts",
        "type": "Integer",
        "required": false,
        "name": "最小广告竞品数",
        "description": "123"
      },
      {
        "field": "maxAdProducts",
        "type": "Integer",
        "required": false,
        "name": "最大广告竞品数",
        "description": "345"
      },
      {
        "field": "minAvgPrice",
        "type": "Float",
        "required": false,
        "name": "最小均价",
        "description": "20"
      },
      {
        "field": "maxAvgPrice",
        "type": "Float",
        "required": false,
        "name": "最大均价",
        "description": "30.3"
      },
      {
        "field": "minWordCount",
        "type": "Integer",
        "required": false,
        "name": "最小单词个数",
        "description": "2"
      },
      {
        "field": "maxWordCount",
        "type": "Integer",
        "required": false,
        "name": "最大单词个数",
        "description": "4"
      },
      {
        "field": "includeKeywords",
        "type": "List",
        "required": false,
        "name": "包含的词",
        "description": "[\"phone stand\"]"
      },
      {
        "field": "excludeKeywords",
        "type": "List",
        "required": false,
        "name": "排除的词",
        "description": "[\"phone stand\"]"
      },
      {
        "field": "minSPR",
        "type": "Integer",
        "required": false,
        "name": "最小SPR",
        "description": "2"
      },
      {
        "field": "maxSPR",
        "type": "Integer",
        "required": false,
        "name": "最大SPR",
        "description": "16"
      },
      {
        "field": "minTitleDensity",
        "type": "Integer",
        "required": false,
        "name": "最小标题密度",
        "description": "2"
      },
      {
        "field": "maxTitleDensity",
        "type": "Integer",
        "required": false,
        "name": "最大标题密度",
        "description": "23"
      },
      {
        "field": "minMonopolyClickRate",
        "type": "Float",
        "required": false,
        "name": "最小点击集中度",
        "description": "23.4"
      },
      {
        "field": "maxMonopolyClickRate",
        "type": "Float",
        "required": false,
        "name": "最大点击集中度",
        "description": "53.1"
      },
      {
        "field": "minTrafficPercentage",
        "type": "Float",
        "required": false,
        "name": "最小流量占比",
        "description": "45"
      },
      {
        "field": "maxTrafficPercentage",
        "type": "Float",
        "required": false,
        "name": "最大流量占比",
        "description": "23"
      },
      {
        "field": "minConversionRate",
        "type": "Float",
        "required": false,
        "name": "最小转化率",
        "description": "0.23"
      },
      {
        "field": "maxConversionRate",
        "type": "Float",
        "required": false,
        "name": "最大转化率",
        "description": "1.4"
      },
      {
        "field": "minCompetitors",
        "type": "Integer",
        "required": false,
        "name": "最小asin数",
        "description": "4"
      },
      {
        "field": "maxCompetitors",
        "type": "Integer",
        "required": false,
        "name": "最大asin数",
        "description": "23"
      },
      {
        "field": "amazonChoice",
        "type": "Boolean",
        "required": false,
        "name": "亚马逊推荐词",
        "description": "TRUE"
      },
      {
        "field": "page",
        "type": "Integer",
        "required": false,
        "name": "页码，从 1 开始",
        "description": "默认：1"
      },
      {
        "field": "size",
        "type": "Integer",
        "required": false,
        "name": "每页条数，最大50",
        "description": "默认：50"
      },
      {
        "field": "order",
        "type": "Object",
        "required": false,
        "name": "排序",
        "description": ""
      },
      {
        "field": "└field",
        "type": "String",
        "required": false,
        "name": "排序字段",
        "description": "见表2.5"
      },
      {
        "field": "└desc",
        "type": "boolean",
        "required": false,
        "name": "true为降序 false为升序",
        "description": "默认降序"
      }
    ],
    "responseFields": [
      {
        "field": "keyword",
        "type": "String",
        "required": false,
        "name": "关键字",
        "description": "N95"
      },
      {
        "field": "keywordCn",
        "type": "String",
        "required": false,
        "name": "关键词中文翻译",
        "description": "用于录音的电话支架"
      },
      {
        "field": "searches",
        "type": "Integer",
        "required": false,
        "name": "搜索量",
        "description": "21582"
      },
      {
        "field": "purchases",
        "type": "Integer",
        "required": false,
        "name": "月购买量",
        "description": "1996"
      },
      {
        "field": "purchaseRate",
        "type": "Float",
        "required": false,
        "name": "月购买率",
        "description": "0.0925"
      },
      {
        "field": "products",
        "type": "Integer",
        "required": false,
        "name": "商品数",
        "description": "1645"
      },
      {
        "field": "bidMin",
        "type": "Float",
        "required": false,
        "name": "最小PPC价格",
        "description": "1.34"
      },
      {
        "field": "bidMax",
        "type": "Float",
        "required": false,
        "name": "最大PPC价格",
        "description": "3.21"
      },
      {
        "field": "bid",
        "type": "Float",
        "required": false,
        "name": "PPC价格",
        "description": "1.6"
      },
      {
        "field": "badges",
        "type": "List",
        "required": false,
        "name": "流量词类型",
        "description": "见表1.10"
      },
      {
        "field": "updatedTime",
        "type": "long",
        "required": false,
        "name": "更新时间",
        "description": ""
      },
      {
        "field": "searchesRank",
        "type": "Integer",
        "required": false,
        "name": "周搜索量排名",
        "description": "25"
      },
      {
        "field": "searchesRankTimeFrom",
        "type": "Long",
        "required": false,
        "name": "周搜索量排名时间范围",
        "description": ""
      },
      {
        "field": "searchesRankTimeTo",
        "type": "Long",
        "required": false,
        "name": "searchesRankTimeTo",
        "description": ""
      },
      {
        "field": "latest1daysAds",
        "type": "Integer",
        "required": false,
        "name": "最近1天广告竞品数",
        "description": "70"
      },
      {
        "field": "latest7daysAds",
        "type": "Integer",
        "required": false,
        "name": "最近7天广告竞品数",
        "description": "100"
      },
      {
        "field": "latest30daysAds",
        "type": "Integer",
        "required": false,
        "name": "最近30天广告竞品数",
        "description": "280"
      },
      {
        "field": "supplyDemandRatio",
        "type": "Float",
        "required": false,
        "name": "供需比",
        "description": "3.8"
      },
      {
        "field": "trafficPercentage",
        "type": "Float",
        "required": false,
        "name": "流量占比",
        "description": "0.015"
      },
      {
        "field": "calculatedWeeklySearches",
        "type": "Float",
        "required": false,
        "name": "预估周搜索量",
        "description": "40"
      },
      {
        "field": "avgPrice",
        "type": "Float",
        "required": false,
        "name": "平均价格",
        "description": "36.14"
      },
      {
        "field": "avgRatings",
        "type": "Integer",
        "required": false,
        "name": "平均评分数",
        "description": "12223"
      },
      {
        "field": "avgRating",
        "type": "Float",
        "required": false,
        "name": "平均评分值",
        "description": "4.5"
      },
      {
        "field": "titleDensity",
        "type": "Integer",
        "required": false,
        "name": "标题密度",
        "description": "42.9"
      },
      {
        "field": "spr",
        "type": "Integer",
        "required": false,
        "name": "SPR",
        "description": "6"
      },
      {
        "field": "monopolyClickRate",
        "type": "Float",
        "required": false,
        "name": "点击垄断率",
        "description": "0.3"
      },
      {
        "field": "top3ClickingRate",
        "type": "Float",
        "required": false,
        "name": "前三点击",
        "description": "0.0813"
      },
      {
        "field": "top3ConversionRate",
        "type": "Float",
        "required": false,
        "name": "前三转化",
        "description": "0.2011"
      },
      {
        "field": "relationVariationsItems",
        "type": "List",
        "required": false,
        "name": "来自于哪些变体",
        "description": ""
      },
      {
        "field": "└marketplace",
        "type": "String",
        "required": false,
        "name": "站点",
        "description": "3"
      },
      {
        "field": "└asin",
        "type": "String",
        "required": false,
        "name": "asin",
        "description": "B08P6SC34B"
      },
      {
        "field": "└imageUrl",
        "type": "String",
        "required": false,
        "name": "图片链接",
        "description": "10"
      },
      {
        "field": "└trafficPercentage",
        "type": "Float",
        "required": false,
        "name": "流量占比",
        "description": "54.6"
      },
      {
        "field": "└title",
        "type": "String",
        "required": false,
        "name": "标题",
        "description": ""
      },
      {
        "field": "└price",
        "type": "Float",
        "required": false,
        "name": "价格",
        "description": "60"
      },
      {
        "field": "└reviews",
        "type": "Float",
        "required": false,
        "name": "评论数",
        "description": "10"
      },
      {
        "field": "└rating",
        "type": "Float",
        "required": false,
        "name": "评分",
        "description": "4.5"
      },
      {
        "field": "└marketplace",
        "type": "String",
        "required": false,
        "name": "站点",
        "description": "3"
      },
      {
        "field": "└asin",
        "type": "String",
        "required": false,
        "name": "asin",
        "description": "B08P6SC34B"
      },
      {
        "field": "└imageUrl",
        "type": "String",
        "required": false,
        "name": "图片链接",
        "description": "10"
      },
      {
        "field": "└trafficPercentage",
        "type": "Float",
        "required": false,
        "name": "流量占比",
        "description": "54.6"
      },
      {
        "field": "└title",
        "type": "String",
        "required": false,
        "name": "标题",
        "description": ""
      },
      {
        "field": "└price",
        "type": "Float",
        "required": false,
        "name": "价格",
        "description": "60"
      },
      {
        "field": "└reviews",
        "type": "Float",
        "required": false,
        "name": "评论数",
        "description": "10"
      },
      {
        "field": "└rating",
        "type": "Float",
        "required": false,
        "name": "评分",
        "description": "4.5"
      }
    ]
  },
  {
    "operation": "ABA_RESEARCH_WEEKLY",
    "domain": "keyword",
    "responseShape": "page",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "市场",
        "description": "见表 1.2"
      },
      {
        "field": "date",
        "type": "String",
        "required": false,
        "name": "为空时，查最新周",
        "description": "20230610，限定为周六的日期）"
      },
      {
        "field": "departments",
        "type": "List",
        "required": false,
        "name": "类目列表",
        "description": "[\"automotive\",\"baby-products\"]"
      },
      {
        "field": "excludeKeywords",
        "type": "String",
        "required": false,
        "name": "排除关键词",
        "description": "portable"
      },
      {
        "field": "includeKeywords",
        "type": "String",
        "required": false,
        "name": "包含关键词",
        "description": ""
      },
      {
        "field": "exactFlag",
        "type": "Boolean",
        "required": false,
        "name": "是否精确匹配",
        "description": ""
      },
      {
        "field": "rankGrowthValue",
        "type": "Integer",
        "required": false,
        "name": "搜索增长量",
        "description": ""
      },
      {
        "field": "rankGrowthRate",
        "type": "Double",
        "required": false,
        "name": "搜索增长率",
        "description": ""
      },
      {
        "field": "minRankGrowthRate",
        "type": "Double",
        "required": false,
        "name": "最小排名增长率",
        "description": ""
      },
      {
        "field": "maxRankGrowthRate",
        "type": "Double",
        "required": false,
        "name": "最大排名增长率",
        "description": ""
      },
      {
        "field": "minSearchRank",
        "type": "Integer",
        "required": false,
        "name": "最小排名",
        "description": ""
      },
      {
        "field": "maxSearchRank",
        "type": "Integer",
        "required": false,
        "name": "最大排名",
        "description": ""
      },
      {
        "field": "minSearches",
        "type": "Integer",
        "required": false,
        "name": "最小搜索量",
        "description": ""
      },
      {
        "field": "maxSearches",
        "type": "Integer",
        "required": false,
        "name": "最大搜索量",
        "description": ""
      },
      {
        "field": "minMonopolyClickRate",
        "type": "Double",
        "required": false,
        "name": "最小点击集中度",
        "description": ""
      },
      {
        "field": "maxMonopolyClickRate",
        "type": "Double",
        "required": false,
        "name": "最大点击集中度",
        "description": ""
      },
      {
        "field": "minConversionRate",
        "type": "Double",
        "required": false,
        "name": "最小转化占比",
        "description": ""
      },
      {
        "field": "maxConversionRate",
        "type": "Double",
        "required": false,
        "name": "最大转化占比",
        "description": ""
      },
      {
        "field": "minWordCount",
        "type": "Integer",
        "required": false,
        "name": "最小单词数",
        "description": ""
      },
      {
        "field": "maxWordCount",
        "type": "Integer",
        "required": false,
        "name": "最大单词数",
        "description": ""
      },
      {
        "field": "minSPR",
        "type": "Integer",
        "required": false,
        "name": "最小SPR",
        "description": ""
      },
      {
        "field": "maxSPR",
        "type": "Integer",
        "required": false,
        "name": "最大SPR",
        "description": ""
      },
      {
        "field": "minTitleDensity",
        "type": "Integer",
        "required": false,
        "name": "最小标题密度",
        "description": ""
      },
      {
        "field": "maxTitleDensity",
        "type": "Integer",
        "required": false,
        "name": "最大标题密度",
        "description": ""
      },
      {
        "field": "minClicks",
        "type": "Integer",
        "required": false,
        "name": "最小点击量",
        "description": "1"
      },
      {
        "field": "maxClicks",
        "type": "Integer",
        "required": false,
        "name": "最大点击量",
        "description": "10000"
      },
      {
        "field": "minImpressions",
        "type": "Integer",
        "required": false,
        "name": "最小展示量",
        "description": "10000"
      },
      {
        "field": "maxImpressions",
        "type": "Integer",
        "required": false,
        "name": "最大展示量",
        "description": "20000"
      },
      {
        "field": "searchModel",
        "type": "Integer",
        "required": false,
        "name": "搜索模式：1：热门市场2：异动市场3：持续增长市场4：快速飙升市场5：潜力市场6：长尾市场",
        "description": "1"
      },
      {
        "field": "page",
        "type": "Integer",
        "required": false,
        "name": "页码，从 1 开始",
        "description": "默认：1"
      },
      {
        "field": "size",
        "type": "Integer",
        "required": false,
        "name": "每页条数，最大40",
        "description": "默认：40"
      },
      {
        "field": "order",
        "type": "Object",
        "required": false,
        "name": "排序",
        "description": ""
      },
      {
        "field": "└field",
        "type": "String",
        "required": false,
        "name": "排序字段",
        "description": "见表2.4"
      },
      {
        "field": "└desc",
        "type": "boolean",
        "required": false,
        "name": "true为降序 false为升序",
        "description": "默认降序"
      }
    ],
    "responseFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": false,
        "name": "市场",
        "description": "US"
      },
      {
        "field": "date",
        "type": "String",
        "required": false,
        "name": "查询日期",
        "description": "20230610，限定为周六的日期"
      },
      {
        "field": "keyword",
        "type": "String",
        "required": false,
        "name": "关键词",
        "description": "portable charger"
      },
      {
        "field": "keywordCn",
        "type": "Integer",
        "required": false,
        "name": "关键词中文",
        "description": "便携式充电器"
      },
      {
        "field": "keywordJp",
        "type": "String",
        "required": false,
        "name": "关键词日文",
        "description": ""
      },
      {
        "field": "departments",
        "type": "List",
        "required": false,
        "name": "类目",
        "description": "[\"Cell Phones & Accessories\"]"
      },
      {
        "field": "searchRank",
        "type": "Integer",
        "required": false,
        "name": "搜索排名",
        "description": "62"
      },
      {
        "field": "searchRankCv",
        "type": "Integer",
        "required": false,
        "name": "排名增长量",
        "description": "19"
      },
      {
        "field": "searchRankCr",
        "type": "Double",
        "required": false,
        "name": "排名增长率",
        "description": "0.2346"
      },
      {
        "field": "searches",
        "type": "Integer",
        "required": false,
        "name": "搜索量",
        "description": "46147979"
      },
      {
        "field": "purchases",
        "type": "Integer",
        "required": false,
        "name": "购买量",
        "description": "2492"
      },
      {
        "field": "purchaseRate",
        "type": "Double",
        "required": false,
        "name": "购买率",
        "description": "0.0054"
      },
      {
        "field": "clicks",
        "type": "Integer",
        "required": false,
        "name": "点击量",
        "description": "1380"
      },
      {
        "field": "impressions",
        "type": "BigInteger",
        "required": false,
        "name": "展示量",
        "description": "73560"
      },
      {
        "field": "titleDensityExact",
        "type": "Integer",
        "required": false,
        "name": "首页商品标题中包含该关键词的商品数(精确匹配)",
        "description": ""
      },
      {
        "field": "cprExact",
        "type": "Integer",
        "required": false,
        "name": "精确 CPR（8天内确保关键词上首页的销量数）",
        "description": ""
      },
      {
        "field": "w1SearchRank",
        "type": "Integer",
        "required": false,
        "name": "上周的排名",
        "description": ""
      },
      {
        "field": "w1RankGrowthValue",
        "type": "Integer",
        "required": false,
        "name": "上周的排名变化值",
        "description": ""
      },
      {
        "field": "w1RankGrowthRate",
        "type": "Double",
        "required": false,
        "name": "上周的排名变化率",
        "description": ""
      },
      {
        "field": "w4SearchRank",
        "type": "Integer",
        "required": false,
        "name": "4周前的排名",
        "description": ""
      },
      {
        "field": "w4RankGrowthValue",
        "type": "Integer",
        "required": false,
        "name": "4周前的排名变化值",
        "description": ""
      },
      {
        "field": "w4RankGrowthRate",
        "type": "Double",
        "required": false,
        "name": "4周前的排名变化率",
        "description": ""
      },
      {
        "field": "w12SearchRank",
        "type": "Integer",
        "required": false,
        "name": "12周前的排名",
        "description": ""
      },
      {
        "field": "w12RankGrowthValue",
        "type": "Integer",
        "required": false,
        "name": "12周前的排名变化值",
        "description": ""
      },
      {
        "field": "w12RankGrowthRate",
        "type": "Double",
        "required": false,
        "name": "12周前的排名变化率",
        "description": ""
      },
      {
        "field": "top3Brands",
        "type": "List",
        "required": false,
        "name": "点击前三品牌",
        "description": ""
      },
      {
        "field": "bid",
        "type": "Float",
        "required": false,
        "name": "ppc竞价",
        "description": ""
      },
      {
        "field": "bidMax",
        "type": "Float",
        "required": false,
        "name": "最大ppc竞价",
        "description": ""
      },
      {
        "field": "bidMin",
        "type": "Float",
        "required": false,
        "name": "最小ppc竞价",
        "description": ""
      },
      {
        "field": "top3AsinDtoList",
        "type": "List",
        "required": false,
        "name": "前三点击asin",
        "description": ""
      },
      {
        "field": "└asin",
        "type": "String",
        "required": false,
        "name": "asin",
        "description": ""
      },
      {
        "field": "└imageUrl",
        "type": "String",
        "required": false,
        "name": "图片URL",
        "description": ""
      },
      {
        "field": "└clickRate",
        "type": "Double",
        "required": false,
        "name": "点击集中度",
        "description": ""
      },
      {
        "field": "└conversionRate",
        "type": "Double",
        "required": false,
        "name": "转化率",
        "description": ""
      },
      {
        "field": "clickShareRate",
        "type": "Double",
        "required": false,
        "name": "前三点击比",
        "description": "54.2"
      },
      {
        "field": "cvsShareRate",
        "type": "Double",
        "required": false,
        "name": "前三转化总比",
        "description": "43.5"
      }
    ]
  },
  {
    "operation": "ABA_RESEARCH_MONTHLY",
    "domain": "keyword",
    "responseShape": "page",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "市场",
        "description": "见表 1.2"
      },
      {
        "field": "date",
        "type": "String",
        "required": false,
        "name": "为空时，查最近30天",
        "description": "202506"
      },
      {
        "field": "departments",
        "type": "List",
        "required": false,
        "name": "类目列表",
        "description": "[\"automotive\",\"baby-products\"]"
      },
      {
        "field": "excludeKeywords",
        "type": "String",
        "required": false,
        "name": "排除关键词",
        "description": "portable"
      },
      {
        "field": "includeKeywords",
        "type": "String",
        "required": false,
        "name": "包含关键词",
        "description": ""
      },
      {
        "field": "exactFlag",
        "type": "Boolean",
        "required": false,
        "name": "是否精确匹配",
        "description": ""
      },
      {
        "field": "minRankGrowthRate",
        "type": "Double",
        "required": false,
        "name": "最小排名增长率",
        "description": ""
      },
      {
        "field": "maxRankGrowthRate",
        "type": "Double",
        "required": false,
        "name": "最大排名增长率",
        "description": ""
      },
      {
        "field": "minSearchRank",
        "type": "Integer",
        "required": false,
        "name": "最小排名",
        "description": ""
      },
      {
        "field": "maxSearchRank",
        "type": "Integer",
        "required": false,
        "name": "最大排名",
        "description": ""
      },
      {
        "field": "minSearches",
        "type": "Integer",
        "required": false,
        "name": "最小搜索量",
        "description": ""
      },
      {
        "field": "maxSearches",
        "type": "Integer",
        "required": false,
        "name": "最大搜索量",
        "description": ""
      },
      {
        "field": "minMonopolyClickRate",
        "type": "Double",
        "required": false,
        "name": "最小点击集中度",
        "description": ""
      },
      {
        "field": "maxMonopolyClickRate",
        "type": "Double",
        "required": false,
        "name": "最大点击集中度",
        "description": ""
      },
      {
        "field": "minConversionRate",
        "type": "Double",
        "required": false,
        "name": "最小转化占比",
        "description": ""
      },
      {
        "field": "maxConversionRate",
        "type": "Double",
        "required": false,
        "name": "最大转化占比",
        "description": ""
      },
      {
        "field": "minWordCount",
        "type": "Integer",
        "required": false,
        "name": "最小单词数",
        "description": ""
      },
      {
        "field": "maxWordCount",
        "type": "Integer",
        "required": false,
        "name": "最大单词数",
        "description": ""
      },
      {
        "field": "minSPR",
        "type": "Integer",
        "required": false,
        "name": "最小SPR",
        "description": ""
      },
      {
        "field": "maxSPR",
        "type": "Integer",
        "required": false,
        "name": "最大SPR",
        "description": ""
      },
      {
        "field": "minTitleDensity",
        "type": "Integer",
        "required": false,
        "name": "最小标题密度",
        "description": ""
      },
      {
        "field": "maxTitleDensity",
        "type": "Integer",
        "required": false,
        "name": "最大标题密度",
        "description": ""
      },
      {
        "field": "minClicks",
        "type": "Integer",
        "required": false,
        "name": "最小点击量",
        "description": "1"
      },
      {
        "field": "maxClicks",
        "type": "Integer",
        "required": false,
        "name": "最大点击量",
        "description": "10000"
      },
      {
        "field": "minImpressions",
        "type": "Integer",
        "required": false,
        "name": "最小展示量",
        "description": "10000"
      },
      {
        "field": "maxImpressions",
        "type": "Integer",
        "required": false,
        "name": "最大展示量",
        "description": "20000"
      },
      {
        "field": "searchModel",
        "type": "Integer",
        "required": false,
        "name": "搜索模式：1：热门市场2：异动市场3：持续增长市场4：快速飙升市场5：潜力市场6：长尾市场",
        "description": "1"
      },
      {
        "field": "page",
        "type": "Integer",
        "required": false,
        "name": "页码，从 1 开始",
        "description": "默认：1"
      },
      {
        "field": "size",
        "type": "Integer",
        "required": false,
        "name": "每页条数，最大15",
        "description": "默认：15"
      },
      {
        "field": "order",
        "type": "Object",
        "required": false,
        "name": "排序",
        "description": ""
      },
      {
        "field": "└field",
        "type": "String",
        "required": false,
        "name": "排序字段",
        "description": "见表2.4"
      },
      {
        "field": "└desc",
        "type": "boolean",
        "required": false,
        "name": "true为降序 false为升序",
        "description": "默认降序"
      }
    ],
    "responseFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": false,
        "name": "市场",
        "description": "US"
      },
      {
        "field": "date",
        "type": "String",
        "required": false,
        "name": "查询日期",
        "description": "202306"
      },
      {
        "field": "keyword",
        "type": "String",
        "required": false,
        "name": "关键词",
        "description": "portable charger"
      },
      {
        "field": "keywordCn",
        "type": "Integer",
        "required": false,
        "name": "关键词中文",
        "description": "便携式充电器"
      },
      {
        "field": "keywordJp",
        "type": "String",
        "required": false,
        "name": "关键词日文",
        "description": ""
      },
      {
        "field": "departments",
        "type": "List",
        "required": false,
        "name": "类目",
        "description": "[\"Cell Phones & Accessories\"]"
      },
      {
        "field": "searchRank",
        "type": "Integer",
        "required": false,
        "name": "搜索排名",
        "description": "62"
      },
      {
        "field": "searchRankCv",
        "type": "Integer",
        "required": false,
        "name": "排名增长量",
        "description": "19"
      },
      {
        "field": "searchRankCr",
        "type": "Double",
        "required": false,
        "name": "排名增长率",
        "description": "0.2346"
      },
      {
        "field": "searches",
        "type": "Integer",
        "required": false,
        "name": "搜索量",
        "description": "46147979"
      },
      {
        "field": "purchases",
        "type": "Integer",
        "required": false,
        "name": "购买量",
        "description": "2492"
      },
      {
        "field": "purchaseRate",
        "type": "Double",
        "required": false,
        "name": "购买率",
        "description": "0.0054"
      },
      {
        "field": "clicks",
        "type": "Integer",
        "required": false,
        "name": "点击量",
        "description": "1380"
      },
      {
        "field": "impressions",
        "type": "BigInteger",
        "required": false,
        "name": "展示量",
        "description": "73560"
      },
      {
        "field": "titleDensityExact",
        "type": "Integer",
        "required": false,
        "name": "首页商品标题中包含该关键词的商品数(精确匹配)",
        "description": ""
      },
      {
        "field": "cprExact",
        "type": "Integer",
        "required": false,
        "name": "精确 CPR（8天内确保关键词上首页的销量数）",
        "description": ""
      },
      {
        "field": "w1SearchRank",
        "type": "Integer",
        "required": false,
        "name": "上周的排名",
        "description": ""
      },
      {
        "field": "w1RankGrowthValue",
        "type": "Integer",
        "required": false,
        "name": "上周的排名变化值",
        "description": ""
      },
      {
        "field": "w1RankGrowthRate",
        "type": "Double",
        "required": false,
        "name": "上周的排名变化率",
        "description": ""
      },
      {
        "field": "w4SearchRank",
        "type": "Integer",
        "required": false,
        "name": "4周前的排名",
        "description": ""
      },
      {
        "field": "w4RankGrowthValue",
        "type": "Integer",
        "required": false,
        "name": "4周前的排名变化值",
        "description": ""
      },
      {
        "field": "w4RankGrowthRate",
        "type": "Double",
        "required": false,
        "name": "4周前的排名变化率",
        "description": ""
      },
      {
        "field": "w12SearchRank",
        "type": "Integer",
        "required": false,
        "name": "12周前的排名",
        "description": ""
      },
      {
        "field": "w12RankGrowthValue",
        "type": "Integer",
        "required": false,
        "name": "12周前的排名变化值",
        "description": ""
      },
      {
        "field": "w12RankGrowthRate",
        "type": "Double",
        "required": false,
        "name": "12周前的排名变化率",
        "description": ""
      },
      {
        "field": "top3Brands",
        "type": "List",
        "required": false,
        "name": "点击前三品牌",
        "description": ""
      },
      {
        "field": "bid",
        "type": "Float",
        "required": false,
        "name": "ppc竞价",
        "description": ""
      },
      {
        "field": "bidMax",
        "type": "Float",
        "required": false,
        "name": "最大ppc竞价",
        "description": ""
      },
      {
        "field": "bidMin",
        "type": "Float",
        "required": false,
        "name": "最小ppc竞价",
        "description": ""
      },
      {
        "field": "top3AsinDtoList",
        "type": "List",
        "required": false,
        "name": "前三点击asin",
        "description": ""
      },
      {
        "field": "└asin",
        "type": "String",
        "required": false,
        "name": "asin",
        "description": ""
      },
      {
        "field": "└imageUrl",
        "type": "String",
        "required": false,
        "name": "图片URL",
        "description": ""
      },
      {
        "field": "└clickRate",
        "type": "Double",
        "required": false,
        "name": "点击集中度",
        "description": ""
      },
      {
        "field": "└conversionRate",
        "type": "Double",
        "required": false,
        "name": "转化率",
        "description": ""
      },
      {
        "field": "clickShareRate",
        "type": "Double",
        "required": false,
        "name": "前三点击比",
        "description": "54.2"
      },
      {
        "field": "cvsShareRate",
        "type": "Double",
        "required": false,
        "name": "前三转化总比",
        "description": "43.5"
      }
    ]
  },
  {
    "operation": "ABA_RESEARCH_TRENDS",
    "domain": "keyword",
    "responseShape": "list",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "市场",
        "description": "见表 1.2"
      },
      {
        "field": "keyword",
        "type": "String",
        "required": true,
        "name": "关键词",
        "description": ""
      },
      {
        "field": "timeGranularity",
        "type": "String",
        "required": false,
        "name": "时间粒度",
        "description": "W：周，M：月"
      }
    ],
    "responseFields": [
      {
        "field": "date",
        "type": "Date",
        "required": false,
        "name": "日期",
        "description": ""
      },
      {
        "field": "rank",
        "type": "String",
        "required": false,
        "name": "ABA排名",
        "description": ""
      },
      {
        "field": "searches",
        "type": "String",
        "required": false,
        "name": "搜索量",
        "description": ""
      },
      {
        "field": "label",
        "type": "Integer",
        "required": false,
        "name": "日期标签",
        "description": ""
      }
    ]
  },
  {
    "operation": "GOOGLE_TRENDS",
    "domain": "keyword",
    "responseShape": "object",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "市场",
        "description": "见表 1.2"
      },
      {
        "field": "keyword",
        "type": "String",
        "required": false,
        "name": "关键字",
        "description": "iphone stand"
      },
      {
        "field": "googleProp",
        "type": "String",
        "required": false,
        "name": "类别",
        "description": "web:google网页搜索shoppingCart:google购物搜索"
      },
      {
        "field": "monthly",
        "type": "boolean",
        "required": false,
        "name": "按照月份",
        "description": "false（默认值）"
      }
    ],
    "responseFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": false,
        "name": "市场，见表 1.2",
        "description": "US"
      },
      {
        "field": "keyword",
        "type": "String",
        "required": false,
        "name": "关键字",
        "description": "phone stand"
      },
      {
        "field": "link",
        "type": "String",
        "required": false,
        "name": "google trend链接",
        "description": ""
      },
      {
        "field": "items",
        "type": "List",
        "required": false,
        "name": "明细",
        "description": ""
      },
      {
        "field": "└time",
        "type": "Long",
        "required": false,
        "name": "时间戳",
        "description": "1555804800000"
      },
      {
        "field": "└value",
        "type": "Integer",
        "required": false,
        "name": "值",
        "description": "2"
      }
    ]
  },
  {
    "operation": "KEYWORD_ORDER",
    "domain": "keyword",
    "responseShape": "page",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "市场,见表1.2",
        "description": "US"
      },
      {
        "field": "asins",
        "type": "List",
        "required": true,
        "name": "asin列表，最大20",
        "description": "B07Z82895W"
      },
      {
        "field": "reverseType",
        "type": "String",
        "required": true,
        "name": "反查模式 W-周 M-月",
        "description": "W"
      },
      {
        "field": "date",
        "type": "String",
        "required": false,
        "name": "查询日期，按周查，格式为yyyMMdd该周最后一天，按月查询yyyyMM",
        "description": "周：20241109月：202411"
      },
      {
        "field": "conversionType",
        "type": "List",
        "required": false,
        "name": "转化类型：E：转化优质词，S：转化平稳词，L：转化流失词，I：无效曝光词",
        "description": "E"
      },
      {
        "field": "variation",
        "type": "List",
        "required": false,
        "name": "是否查询变体asin：Y:否 N:是",
        "description": "Y"
      },
      {
        "field": "page",
        "type": "Integer",
        "required": false,
        "name": "当前页",
        "description": "默认1"
      },
      {
        "field": "size",
        "type": "Integer",
        "required": false,
        "name": "每页显示多少条",
        "description": "固定50"
      },
      {
        "field": "order",
        "type": "Object",
        "required": false,
        "name": "排序",
        "description": ""
      },
      {
        "field": "└field",
        "type": "String",
        "required": false,
        "name": "排序字段",
        "description": "见表2.6"
      },
      {
        "field": "└desc",
        "type": "Boolean",
        "required": false,
        "name": "是否倒序",
        "description": "false"
      }
    ],
    "responseFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": false,
        "name": "市场，见表 1.2",
        "description": "US"
      },
      {
        "field": "keyword",
        "type": "String",
        "required": false,
        "name": "关键词",
        "description": "phone stand for recording"
      },
      {
        "field": "keywordCn",
        "type": "String",
        "required": false,
        "name": "关键词中文翻译",
        "description": "用于录音的电话支架"
      },
      {
        "field": "keywordJp",
        "type": "String",
        "required": false,
        "name": "关键词英文翻译",
        "description": "録音用電話スタンド"
      },
      {
        "field": "asin",
        "type": "String",
        "required": false,
        "name": "所属asin",
        "description": "B0D1FZW65X"
      },
      {
        "field": "searches",
        "type": "Integer",
        "required": false,
        "name": "搜索量",
        "description": "21582"
      },
      {
        "field": "monopolyClickRate",
        "type": "Float",
        "required": false,
        "name": "点击垄断率",
        "description": "0.3"
      },
      {
        "field": "cvsShareRate",
        "type": "Float",
        "required": false,
        "name": "转化共享率",
        "description": "0.3084"
      },
      {
        "field": "searchRank",
        "type": "Integer",
        "required": false,
        "name": "搜索排名",
        "description": "17910"
      },
      {
        "field": "searchRankGv",
        "type": "Integer",
        "required": false,
        "name": "月变化量",
        "description": "5343"
      },
      {
        "field": "searchRankGr",
        "type": "Double",
        "required": false,
        "name": "月变化率",
        "description": "0.3"
      },
      {
        "field": "top3ClickingRate",
        "type": "Float",
        "required": false,
        "name": "前三点击",
        "description": "0.0813"
      },
      {
        "field": "top3ConversionRate",
        "type": "Float",
        "required": false,
        "name": "前三转化",
        "description": "0.2011"
      },
      {
        "field": "conversionType",
        "type": "String",
        "required": false,
        "name": "转化类型：E：转化优质词，S：转化平稳词，L：转化流失词，I：无效曝光词",
        "description": "E"
      }
    ]
  },
  {
    "operation": "TRAFFIC_KEYWORD",
    "domain": "traffic",
    "responseShape": "object",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "市场,见表1.2",
        "description": "US"
      },
      {
        "field": "asin",
        "type": "String",
        "required": true,
        "name": "asin",
        "description": "B07Z82895W"
      },
      {
        "field": "keyword",
        "type": "String",
        "required": false,
        "name": "关键词",
        "description": "phone stand"
      },
      {
        "field": "month",
        "type": "String",
        "required": false,
        "name": "历史月份，不传默认最近30天",
        "description": "202308"
      },
      {
        "field": "badges",
        "type": "List",
        "required": false,
        "name": "流量词类型",
        "description": "见表1.10"
      },
      {
        "field": "trafficKeywordTypes",
        "type": "List",
        "required": false,
        "name": "流量占比类型",
        "description": "见表2.0"
      },
      {
        "field": "conversionKeywordTypes",
        "type": "List",
        "required": false,
        "name": "流量转化类型",
        "description": "见表2.1"
      },
      {
        "field": "page",
        "type": "Integer",
        "required": false,
        "name": "当前页",
        "description": "默认1"
      },
      {
        "field": "size",
        "type": "Integer",
        "required": false,
        "name": "每页显示多少条",
        "description": "默认50，最大100，最多查询2000条数据"
      },
      {
        "field": "order",
        "type": "Object",
        "required": false,
        "name": "排序",
        "description": ""
      },
      {
        "field": "└field",
        "type": "String",
        "required": false,
        "name": "排序字段",
        "description": "默认：rankPosition见表2.3"
      },
      {
        "field": "└desc",
        "type": "Boolean",
        "required": false,
        "name": "是否倒序",
        "description": "false"
      }
    ],
    "responseFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": false,
        "name": "市场编码",
        "description": "见表 1.2"
      },
      {
        "field": "asin",
        "type": "String",
        "required": false,
        "name": "asin",
        "description": "B07Z82895W"
      },
      {
        "field": "total",
        "type": "Integer",
        "required": false,
        "name": "总条数",
        "description": "2685"
      },
      {
        "field": "items",
        "type": "List",
        "required": false,
        "name": "词条",
        "description": "1848"
      },
      {
        "field": "└keyword",
        "type": "String",
        "required": false,
        "name": "关键词",
        "description": "该ASIN近30天或某个自然月进入过亚马逊搜索结果前3页的词"
      },
      {
        "field": "└keywordCn",
        "type": "String",
        "required": false,
        "name": "关键词中文翻译",
        "description": "手机支架"
      },
      {
        "field": "└searches",
        "type": "Integer",
        "required": false,
        "name": "月搜索量",
        "description": "指的是一个自然月的月搜索量，比如2025年8月，该关键词在亚马逊站内的搜索总次数"
      },
      {
        "field": "└products",
        "type": "Integer",
        "required": false,
        "name": "商品数",
        "description": "指搜索该关键词后出现了多少个相关的产品"
      },
      {
        "field": "└purchases",
        "type": "Integer",
        "required": false,
        "name": "月购买量",
        "description": "在亚马逊站内搜索该关键词后产生购买的次数，比如：某用户搜索iphone charger，然后1次购买了1个iphone充电器，2条数据线(关联推荐的商品)，则购买量=1"
      },
      {
        "field": "└purchaseRate",
        "type": "Float",
        "required": false,
        "name": "购买率",
        "description": "指买家输入该搜索词并点击此细分市场中的任意商品后，买家的购买次数占买家输入该搜索词总次数的比例"
      },
      {
        "field": "└bid",
        "type": "Float",
        "required": false,
        "name": "PPC竞价",
        "description": "亚马逊站内广告Bid价格，系统提供【词组匹配】的Bid建议价格以及范围"
      },
      {
        "field": "└bidMax",
        "type": "Float",
        "required": false,
        "name": "PPC竞价范围",
        "description": ""
      },
      {
        "field": "└bidMin",
        "type": "Float",
        "required": false,
        "name": "PPC竞价范围",
        "description": ""
      },
      {
        "field": "└badges",
        "type": "List",
        "required": false,
        "name": "曝光位置",
        "description": "ASIN在对应关键词的搜索结果下曝光的具体位置， 见表1.10"
      },
      {
        "field": "└rankPosition",
        "type": "RankPosition",
        "required": false,
        "name": "自然排名",
        "description": ""
      },
      {
        "field": "└└page",
        "type": "Integer",
        "required": false,
        "name": "第几页",
        "description": "3"
      },
      {
        "field": "└└pageSize",
        "type": "Integer",
        "required": false,
        "name": "每页多少条数据",
        "description": "60"
      },
      {
        "field": "└└index",
        "type": "Integer",
        "required": false,
        "name": "当前页排第几",
        "description": "10"
      },
      {
        "field": "└└position",
        "type": "Integer",
        "required": false,
        "name": "总结果中排第几",
        "description": "106"
      },
      {
        "field": "└└updatedTime",
        "type": "long",
        "required": false,
        "name": "排名时间",
        "description": ""
      },
      {
        "field": "└adPosition",
        "type": "AdPosition",
        "required": false,
        "name": "广告排名",
        "description": ""
      },
      {
        "field": "└└page",
        "type": "Integer",
        "required": false,
        "name": "第几页",
        "description": "2"
      },
      {
        "field": "└└pageSize",
        "type": "Integer",
        "required": false,
        "name": "每页多少条数据",
        "description": "63"
      },
      {
        "field": "└└index",
        "type": "Integer",
        "required": false,
        "name": "当前页排第几",
        "description": "37"
      },
      {
        "field": "└└position",
        "type": "Integer",
        "required": false,
        "name": "总结果中排第几",
        "description": "85"
      },
      {
        "field": "└└updatedTime",
        "type": "long",
        "required": false,
        "name": "排名时间",
        "description": ""
      },
      {
        "field": "└searchesRank",
        "type": "Integer",
        "required": false,
        "name": "周搜索量排名",
        "description": "数据来源于亚马逊ABA数据的关键词搜索频率排名（Search Frequency Rank），数字越小表示排名越靠前，搜索量越高"
      },
      {
        "field": "└searchesRankTimeFrom",
        "type": "Long",
        "required": false,
        "name": "周搜索量排名时间范围",
        "description": ""
      },
      {
        "field": "└searchesRankTimeTo",
        "type": "Long",
        "required": false,
        "name": "searchesRankTimeTo",
        "description": ""
      },
      {
        "field": "└latest1daysAds",
        "type": "Integer",
        "required": false,
        "name": "最近1天广告竞品数",
        "description": "表示近1天内进入过该关键词搜索结果前3页的广告产品总数，包括SP广告、HR广告、品牌广告和视频广告"
      },
      {
        "field": "└latest7daysAds",
        "type": "Integer",
        "required": false,
        "name": "最近7天广告竞品数",
        "description": "表示近7天内进入过该关键词搜索结果前3页的广告产品总数，包括SP广告、HR广告、品牌广告和视频广告"
      },
      {
        "field": "└latest30daysAds",
        "type": "Integer",
        "required": false,
        "name": "最近30天广告竞品数",
        "description": "表示近30天内进入过该关键词搜索结果前3页的广告产品总数，包括SP广告、HR广告、品牌广告和视频广告"
      },
      {
        "field": "└supplyDemandRatio",
        "type": "Float",
        "required": false,
        "name": "供需比",
        "description": "搜索量(需求) / 商品数(供应)，在同类市场中，需供比值越高，则代表该市场需求越强劲"
      },
      {
        "field": "└trafficPercentage",
        "type": "Float",
        "required": false,
        "name": "流量占比",
        "description": "指的是产品通过不同流量词获得的曝光量占比"
      },
      {
        "field": "└trafficKeywordType",
        "type": "String",
        "required": false,
        "name": "流量占比类型",
        "description": "见表2.0"
      },
      {
        "field": "└conversionKeywordType",
        "type": "String",
        "required": false,
        "name": "转换效果类型",
        "description": "见表2.1"
      },
      {
        "field": "└calculatedWeeklySearches",
        "type": "Float",
        "required": false,
        "name": "预估周曝光量",
        "description": "指的是该关键词本周内给产品带来的预估曝光量，非该词在亚马逊的总搜索量"
      },
      {
        "field": "└impressions",
        "type": "Long",
        "required": false,
        "name": "展示量",
        "description": "指一个自然月，比如2024年3月，在某个关键词搜索结果页中所有ASIN的总展示次数，非单个ASIN在关键词下的曝光量"
      },
      {
        "field": "└updatedTime",
        "type": "Long",
        "required": false,
        "name": "更新时间",
        "description": ""
      },
      {
        "field": "└clicks",
        "type": "Integer",
        "required": false,
        "name": "点击量",
        "description": "指一个自然月，比如2024年3月，在某个关键词搜索结果页中被点击的总次数，非单个ASIN在关键词下的点击量"
      },
      {
        "field": "└naturalRatio",
        "type": "Float",
        "required": false,
        "name": "流量分布-自然占比",
        "description": "0.9312"
      },
      {
        "field": "└adRatio",
        "type": "Float",
        "required": false,
        "name": "流量分布-广告占比",
        "description": "0.0688"
      },
      {
        "field": "stats",
        "type": "List",
        "required": false,
        "name": "高频词",
        "description": ""
      },
      {
        "field": "└keywords",
        "type": "String",
        "required": false,
        "name": "词",
        "description": "phone"
      },
      {
        "field": "└total",
        "type": "Integer",
        "required": false,
        "name": "总条数",
        "description": "90"
      },
      {
        "field": "└└page",
        "type": "Integer",
        "required": false,
        "name": "第几页",
        "description": "3"
      },
      {
        "field": "└└pageSize",
        "type": "Integer",
        "required": false,
        "name": "每页多少条数据",
        "description": "60"
      },
      {
        "field": "└└index",
        "type": "Integer",
        "required": false,
        "name": "当前页排第几",
        "description": "10"
      },
      {
        "field": "└└position",
        "type": "Integer",
        "required": false,
        "name": "总结果中排第几",
        "description": "106"
      },
      {
        "field": "└└updatedTime",
        "type": "long",
        "required": false,
        "name": "排名时间",
        "description": ""
      },
      {
        "field": "└└page",
        "type": "Integer",
        "required": false,
        "name": "第几页",
        "description": "2"
      },
      {
        "field": "└└pageSize",
        "type": "Integer",
        "required": false,
        "name": "每页多少条数据",
        "description": "63"
      },
      {
        "field": "└└index",
        "type": "Integer",
        "required": false,
        "name": "当前页排第几",
        "description": "37"
      },
      {
        "field": "└└position",
        "type": "Integer",
        "required": false,
        "name": "总结果中排第几",
        "description": "85"
      },
      {
        "field": "└└updatedTime",
        "type": "long",
        "required": false,
        "name": "排名时间",
        "description": ""
      }
    ]
  },
  {
    "operation": "TRAFFIC_LISTING_PAGE",
    "domain": "traffic",
    "responseShape": "page",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "市场,见表1.2",
        "description": "US"
      },
      {
        "field": "asinList",
        "type": "List",
        "required": true,
        "name": "asin列表",
        "description": "[\"B07Z82895W\"]"
      },
      {
        "field": "relations",
        "type": "List",
        "required": true,
        "name": "关联类型，见表2.2",
        "description": "[\"vav\"]"
      },
      {
        "field": "variations",
        "type": "Boolean",
        "required": false,
        "name": "是否查询变体",
        "description": "false"
      },
      {
        "field": "page",
        "type": "Integer",
        "required": false,
        "name": "页码，从 1 开始",
        "description": "默认：1"
      },
      {
        "field": "size",
        "type": "Integer",
        "required": false,
        "name": "每页条数",
        "description": "默认：50"
      },
      {
        "field": "order",
        "type": "Object",
        "required": false,
        "name": "排序",
        "description": ""
      },
      {
        "field": "└field",
        "type": "String",
        "required": false,
        "name": "排序字段",
        "description": "见表2.2"
      },
      {
        "field": "└desc",
        "type": "boolean",
        "required": false,
        "name": "true为降序 false为升序",
        "description": "默认降序"
      }
    ],
    "responseFields": [
      {
        "field": "asin",
        "type": "String",
        "required": false,
        "name": "asin",
        "description": "B078J8VPVW"
      },
      {
        "field": "brand",
        "type": "String",
        "required": false,
        "name": "品牌",
        "description": "Pampers"
      },
      {
        "field": "brandUrl",
        "type": "String",
        "required": false,
        "name": "品牌 URL",
        "description": "https://www.amazon.com/s?k=HP"
      },
      {
        "field": "imageUrl",
        "type": "String",
        "required": false,
        "name": "图片 URL",
        "description": "https://images-na.ssl-images-amazon.com/images/I/51axlzme6aL .AC_US200.jpg"
      },
      {
        "field": "title",
        "type": "String",
        "required": false,
        "name": "商品标题",
        "description": "Diapers Size 2, 186 Count - Pampers Swaddlers Disposable Baby Diapers, ONE MONTH SUPPLY"
      },
      {
        "field": "parent",
        "type": "String",
        "required": false,
        "name": "父体",
        "description": "B081RGNL17"
      },
      {
        "field": "nodeId",
        "type": "Long",
        "required": false,
        "name": "节点 id",
        "description": "3741281"
      },
      {
        "field": "nodeIdPath",
        "type": "String",
        "required": false,
        "name": "节点 id 路径字符串",
        "description": "2619525011:3741271:3741281"
      },
      {
        "field": "nodeLabelPath",
        "type": "String",
        "required": false,
        "name": "类目",
        "description": "Baby Products:Diapering:Disposable Diapers"
      },
      {
        "field": "bsrId",
        "type": "String",
        "required": false,
        "name": "BSRid",
        "description": "office-products"
      },
      {
        "field": "bsr",
        "type": "Integer",
        "required": false,
        "name": "BSR 排名",
        "description": "1"
      },
      {
        "field": "units",
        "type": "Integer",
        "required": false,
        "name": "月销量",
        "description": "26289"
      },
      {
        "field": "unitsCr",
        "type": "Float",
        "required": false,
        "name": "月销量增长率",
        "description": "-46.3"
      },
      {
        "field": "revenue",
        "type": "Float",
        "required": false,
        "name": "月销售额",
        "description": "1693537.4"
      },
      {
        "field": "price",
        "type": "Float",
        "required": false,
        "name": "价格",
        "description": "64.42"
      },
      {
        "field": "profit",
        "type": "Float",
        "required": false,
        "name": "利润率",
        "description": "63.92"
      },
      {
        "field": "fba",
        "type": "Float",
        "required": false,
        "name": "fba 运费",
        "description": "13.58"
      },
      {
        "field": "ratings",
        "type": "Integer",
        "required": false,
        "name": "评分数",
        "description": "32004"
      },
      {
        "field": "ratingsRate",
        "type": "Float",
        "required": false,
        "name": "留评率",
        "description": "40.57"
      },
      {
        "field": "rating",
        "type": "Float",
        "required": false,
        "name": "评分",
        "description": "4.8"
      },
      {
        "field": "ratingsCv",
        "type": "Integer",
        "required": false,
        "name": "月度增长数",
        "description": "10666"
      },
      {
        "field": "ratingDelta",
        "type": "Integer",
        "required": false,
        "name": "留评数：近 30 天新增评论数",
        "description": "0"
      },
      {
        "field": "availableDate",
        "type": "Long",
        "required": false,
        "name": "上架时间，时间戳格式",
        "description": "1454083200000"
      },
      {
        "field": "fulfillment",
        "type": "String",
        "required": false,
        "name": "配送方式",
        "description": "AMZ or FBA or FBM"
      },
      {
        "field": "variations",
        "type": "Integer",
        "required": false,
        "name": "变体数",
        "description": "7"
      },
      {
        "field": "sellers",
        "type": "Integer",
        "required": false,
        "name": "卖家数",
        "description": "7"
      },
      {
        "field": "sellerId",
        "type": "String",
        "required": false,
        "name": "BuyBox 卖家 id",
        "description": "A1Y8BVAASXO4R7"
      },
      {
        "field": "sellerName",
        "type": "String",
        "required": false,
        "name": "BuyBox 卖家",
        "description": "Amazon"
      },
      {
        "field": "sellerNation",
        "type": "String",
        "required": false,
        "name": "BuyBox 卖家国籍",
        "description": "见表 1.5"
      },
      {
        "field": "badge",
        "type": "Badge",
        "required": false,
        "name": "标识",
        "description": "包括了下面 5 个标识"
      },
      {
        "field": "└bestSeller",
        "type": "String",
        "required": false,
        "name": "Best Seller 标识",
        "description": "Y 或者 N"
      },
      {
        "field": "└amazonChoice",
        "type": "String",
        "required": false,
        "name": "amazon choice 标识",
        "description": "Y 或者 N"
      },
      {
        "field": "└newRelease",
        "type": "String",
        "required": false,
        "name": "release 标识",
        "description": "Y 或者 N"
      },
      {
        "field": "└ebc",
        "type": "String",
        "required": false,
        "name": "A+页面",
        "description": "Y 或者 N"
      },
      {
        "field": "└video",
        "type": "String",
        "required": false,
        "name": "视频介绍",
        "description": "Y 或者 N"
      },
      {
        "field": "weight",
        "type": "String",
        "required": false,
        "name": "重量",
        "description": "8.88 pounds"
      },
      {
        "field": "dimension",
        "type": "String",
        "required": false,
        "name": "尺寸",
        "description": "13.3 x 15.8 x 10.6 inches"
      },
      {
        "field": "dimensionType",
        "type": "String",
        "required": false,
        "name": "尺寸类型",
        "description": "ST,0V"
      },
      {
        "field": "sku",
        "type": "String",
        "required": false,
        "name": "sku",
        "description": "[\"Color: Beige\",\"Size: 47 inches\"]"
      },
      {
        "field": "└amazonChoice",
        "type": "String",
        "required": false,
        "name": "amazon choice 标识",
        "description": "Y 或者 N"
      }
    ]
  },
  {
    "operation": "TRAFFIC_KEYWORD_STAT",
    "domain": "traffic",
    "responseShape": "object",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "市场,见表1.2",
        "description": "US"
      },
      {
        "field": "asin",
        "type": "String",
        "required": true,
        "name": "asin",
        "description": "B07Z82895W"
      },
      {
        "field": "month",
        "type": "String",
        "required": false,
        "name": "查询月份",
        "description": "202605"
      }
    ],
    "responseFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": false,
        "name": "市场",
        "description": "US"
      },
      {
        "field": "asin",
        "type": "String",
        "required": false,
        "name": "asin",
        "description": "B07Z82895W"
      },
      {
        "field": "keywords",
        "type": "Integer",
        "required": false,
        "name": "全部流量词条数",
        "description": "2685"
      },
      {
        "field": "ranks",
        "type": "Integer",
        "required": false,
        "name": "自然流量词条数",
        "description": "1848"
      },
      {
        "field": "ads",
        "type": "Integer",
        "required": false,
        "name": "广告流量词条数",
        "description": "1414"
      },
      {
        "field": "calcTime",
        "type": "Long",
        "required": false,
        "name": "最近计算时间",
        "description": ""
      },
      {
        "field": "badgeCount",
        "type": "Object",
        "required": false,
        "name": "流量词类型统计",
        "description": ""
      },
      {
        "field": "└ns",
        "type": "Integer",
        "required": false,
        "name": "自然搜索词数量",
        "description": "1070"
      },
      {
        "field": "└ac",
        "type": "Integer",
        "required": false,
        "name": "AC推荐词数量",
        "description": "0"
      },
      {
        "field": "└er",
        "type": "Integer",
        "required": false,
        "name": "ER推荐词数量",
        "description": "42"
      },
      {
        "field": "└fs",
        "type": "Integer",
        "required": false,
        "name": "4星推荐词数量",
        "description": "0"
      },
      {
        "field": "└hr",
        "type": "Integer",
        "required": false,
        "name": "HR广告词数量",
        "description": "117"
      },
      {
        "field": "└sb",
        "type": "Integer",
        "required": false,
        "name": "品牌广告词数量",
        "description": "334"
      },
      {
        "field": "└sv",
        "type": "Integer",
        "required": false,
        "name": "视频广告词数量",
        "description": "208"
      },
      {
        "field": "└ad",
        "type": "Integer",
        "required": false,
        "name": "SP广告词数量",
        "description": "764"
      },
      {
        "field": "└ns",
        "type": "Integer",
        "required": false,
        "name": "自然搜索词数量",
        "description": "1070"
      },
      {
        "field": "└ac",
        "type": "Integer",
        "required": false,
        "name": "AC推荐词数量",
        "description": "0"
      },
      {
        "field": "└er",
        "type": "Integer",
        "required": false,
        "name": "ER推荐词数量",
        "description": "42"
      },
      {
        "field": "└fs",
        "type": "Integer",
        "required": false,
        "name": "4星推荐词数量",
        "description": "0"
      },
      {
        "field": "└hr",
        "type": "Integer",
        "required": false,
        "name": "HR广告词数量",
        "description": "117"
      },
      {
        "field": "└sb",
        "type": "Integer",
        "required": false,
        "name": "品牌广告词数量",
        "description": "334"
      },
      {
        "field": "└sv",
        "type": "Integer",
        "required": false,
        "name": "视频广告词数量",
        "description": "208"
      },
      {
        "field": "└ad",
        "type": "Integer",
        "required": false,
        "name": "SP广告词数量",
        "description": "764"
      }
    ]
  },
  {
    "operation": "TRAFFIC_LISTING_STAT",
    "domain": "traffic",
    "responseShape": "object",
    "requestFields": [
      {
        "field": "asin",
        "type": "String",
        "required": true,
        "name": "asin 路径参数",
        "description": "由官方 Http Request URL 定义；官方参数表未单独列出"
      },
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "市场,见表1.2",
        "description": "US"
      },
      {
        "field": "asinList",
        "type": "List",
        "required": false,
        "name": "asin列表",
        "description": "[\"B07Z82895W\"]"
      }
    ],
    "responseFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": false,
        "name": "市场",
        "description": "US"
      },
      {
        "field": "asin",
        "type": "String",
        "required": false,
        "name": "asin",
        "description": "B07Z82895W"
      },
      {
        "field": "relations",
        "type": "Integer",
        "required": false,
        "name": "全部流量",
        "description": "1848"
      },
      {
        "field": "freeRelations",
        "type": "Integer",
        "required": false,
        "name": "免费流量",
        "description": "1414"
      },
      {
        "field": "paidRelations",
        "type": "Integer",
        "required": false,
        "name": "付费流量",
        "description": "286"
      },
      {
        "field": "calcTime",
        "type": "Long",
        "required": false,
        "name": "最近计算时间",
        "description": ""
      },
      {
        "field": "items",
        "type": "List",
        "required": false,
        "name": "统计概要",
        "description": ""
      },
      {
        "field": "└relation",
        "type": "String",
        "required": false,
        "name": "关联类型，见表2.2,忽略大小写",
        "description": "vav"
      },
      {
        "field": "└count",
        "type": "Integer",
        "required": false,
        "name": "数量",
        "description": "3"
      }
    ]
  },
  {
    "operation": "TRAFFIC_SOURCE",
    "domain": "traffic",
    "responseShape": "page",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "市场,见表1.2",
        "description": "US"
      },
      {
        "field": "q",
        "type": "String",
        "required": true,
        "name": "asin 或者 关键词",
        "description": "B07Z82895W"
      },
      {
        "field": "month",
        "type": "String",
        "required": true,
        "name": "筛选日期,yyyyMM格式",
        "description": "202203"
      },
      {
        "field": "page",
        "type": "Integer",
        "required": false,
        "name": "页码，从 1 开始",
        "description": "默认：1"
      },
      {
        "field": "size",
        "type": "Integer",
        "required": false,
        "name": "每页条数",
        "description": "默认：50最大： 100"
      },
      {
        "field": "order",
        "type": "Object",
        "required": false,
        "name": "排序",
        "description": ""
      },
      {
        "field": "└field",
        "type": "String",
        "required": false,
        "name": "排序字段",
        "description": "见表2.4"
      },
      {
        "field": "└desc",
        "type": "boolean",
        "required": false,
        "name": "true为降序 false为升序",
        "description": "默认降序"
      }
    ],
    "responseFields": [
      {
        "field": "keywords",
        "type": "Integer",
        "required": false,
        "name": "全部流量词",
        "description": "1"
      },
      {
        "field": "searchKeywords",
        "type": "Integer",
        "required": false,
        "name": "自然搜索词",
        "description": "12"
      },
      {
        "field": "acKeywords",
        "type": "String",
        "required": false,
        "name": "AC推荐词",
        "description": "13"
      },
      {
        "field": "editorialKeywords",
        "type": "Integer",
        "required": false,
        "name": "ER推荐词",
        "description": "13"
      },
      {
        "field": "fourStarsKeywords",
        "type": "Integer",
        "required": false,
        "name": "4星推荐词",
        "description": "14"
      },
      {
        "field": "hrKeywords",
        "type": "Integer",
        "required": false,
        "name": "HR推荐词",
        "description": "1"
      },
      {
        "field": "adKeywords",
        "type": "Integer",
        "required": false,
        "name": "SP广告词",
        "description": "3"
      },
      {
        "field": "videoKeywords",
        "type": "Integer",
        "required": false,
        "name": "视频广告词",
        "description": "4"
      },
      {
        "field": "brandKeywords",
        "type": "Integer",
        "required": false,
        "name": "品牌广告词",
        "description": "5"
      },
      {
        "field": "badgeLabels",
        "type": "List",
        "required": false,
        "name": "流量来源概览",
        "description": "[“SEARCH”, “OFFICIAL”, “AD”]"
      },
      {
        "field": "badgeDetails",
        "type": "Map",
        "required": false,
        "name": "流量来源明细",
        "description": "{“SEARCH”: [“NATURAL_SEARCHING”],”OFFICIAL”: [“AMAZON_CHOICE”],”AD”: [“SPONSOR_BRAND”,”SPONSOR_VIDEO”,”HIGHLY_RATED”,”ADS”]}"
      },
      {
        "field": "asinInfo",
        "type": "Object",
        "required": false,
        "name": "Asin相关信息",
        "description": ""
      },
      {
        "field": "└asin",
        "type": "String",
        "required": false,
        "name": "asin",
        "description": "B078J8VPVW"
      },
      {
        "field": "└asinUrl",
        "type": "String",
        "required": false,
        "name": "该asin对应亚马逊地址",
        "description": "https://www.amazon.com/dp/B08GHW4TBS"
      },
      {
        "field": "└currency",
        "type": "String",
        "required": false,
        "name": "货币code",
        "description": "$"
      },
      {
        "field": "└price",
        "type": "Float",
        "required": false,
        "name": "价格",
        "description": "23"
      },
      {
        "field": "└rating",
        "type": "Float",
        "required": false,
        "name": "评分",
        "description": "234"
      },
      {
        "field": "└reviews",
        "type": "Integer",
        "required": false,
        "name": "评分数",
        "description": "23"
      },
      {
        "field": "└title",
        "type": "String",
        "required": false,
        "name": "标题",
        "description": "Diapers Size 2, 186 Count - Pampers Swaddlers Disposable Baby Diapers, ONE MONTH SUPPLY"
      },
      {
        "field": "└sku",
        "type": "String",
        "required": false,
        "name": "sku",
        "description": "[\"Color: Beige\",\"Size: 47 inches\"]"
      },
      {
        "field": "└variations",
        "type": "Integer",
        "required": false,
        "name": "变体数",
        "description": "2"
      },
      {
        "field": "└nodeId",
        "type": "Long",
        "required": false,
        "name": "类目ID",
        "description": "12097479011"
      },
      {
        "field": "└nodeIdPath",
        "type": "String",
        "required": false,
        "name": "类目ID路径",
        "description": "172282:24046923011:172541:12097479011"
      },
      {
        "field": "└nodeLabelPath",
        "type": "String",
        "required": false,
        "name": "类目路径",
        "description": "Electronics:Headphones, Earbuds & Accessories:Headphones & Earbuds:Over-Ear Headphones"
      },
      {
        "field": "└bsrRank",
        "type": "Long",
        "required": false,
        "name": "大类排名(BSR)",
        "description": "175204"
      }
    ]
  },
  {
    "operation": "MARKET_RESEARCH",
    "domain": "market",
    "responseShape": "page",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "站点编码",
        "description": "见表 1.2"
      },
      {
        "field": "month",
        "type": "String",
        "required": false,
        "name": "筛选日期,默认最近30天",
        "description": "见表 1.1"
      },
      {
        "field": "topNum",
        "type": "Integer",
        "required": false,
        "name": "头部Listing数量",
        "description": "10"
      },
      {
        "field": "newProduct",
        "type": "Integer",
        "required": false,
        "name": "新品定义",
        "description": "default: 3"
      },
      {
        "field": "nodeIdPath",
        "type": "String",
        "required": false,
        "name": "类目",
        "description": "172282:281407"
      },
      {
        "field": "departmentKeyword",
        "type": "String",
        "required": false,
        "name": "类目关键字",
        "description": "Electronics:Accessories & Supplies"
      },
      {
        "field": "minAvgUnits",
        "type": "Integer",
        "required": false,
        "name": "最低月均销量",
        "description": "100"
      },
      {
        "field": "maxAvgUnits",
        "type": "Integer",
        "required": false,
        "name": "最高均月销量",
        "description": "10000"
      },
      {
        "field": "minAvgRevenue",
        "type": "Float",
        "required": false,
        "name": "最低月均销售额",
        "description": "100"
      },
      {
        "field": "maxAvgRevenue",
        "type": "Float",
        "required": false,
        "name": "最高月均销售额",
        "description": "900"
      },
      {
        "field": "minAvgRatings",
        "type": "Integer",
        "required": false,
        "name": "最低平均评分数",
        "description": "100"
      },
      {
        "field": "maxAvgRatings",
        "type": "Integer",
        "required": false,
        "name": "最高平均评分数",
        "description": "500"
      },
      {
        "field": "minAvgRating",
        "type": "Float",
        "required": false,
        "name": "最低平均评分值",
        "description": "2.5"
      },
      {
        "field": "maxAvgRating",
        "type": "Float",
        "required": false,
        "name": "最高平均评分值",
        "description": "3"
      },
      {
        "field": "minAvgBsr",
        "type": "Integer",
        "required": false,
        "name": "最低平均BSR排名",
        "description": "50"
      },
      {
        "field": "maxAvgBsr",
        "type": "Integer",
        "required": false,
        "name": "最高平均BSR排名",
        "description": "100"
      },
      {
        "field": "minAvgPrice",
        "type": "Float",
        "required": false,
        "name": "最低平均价格",
        "description": "30"
      },
      {
        "field": "maxAvgPrice",
        "type": "Float",
        "required": false,
        "name": "最高平均价格",
        "description": "50"
      },
      {
        "field": "minWeight",
        "type": "Float",
        "required": false,
        "name": "最低重量",
        "description": "30"
      },
      {
        "field": "maxWeight",
        "type": "Float",
        "required": false,
        "name": "最高重量",
        "description": "60"
      },
      {
        "field": "minVolume",
        "type": "Float",
        "required": false,
        "name": "最低体积",
        "description": "20"
      },
      {
        "field": "maxVolume",
        "type": "Float",
        "required": false,
        "name": "最高体积",
        "description": "50"
      },
      {
        "field": "minAvgProfit",
        "type": "Float",
        "required": false,
        "name": "最低平均毛利率",
        "description": "20"
      },
      {
        "field": "maxAvgProfit",
        "type": "Float",
        "required": false,
        "name": "最高平均毛利率",
        "description": "70"
      },
      {
        "field": "minTopAvgUnits",
        "type": "Integer",
        "required": false,
        "name": "最低头部月均销量",
        "description": "200"
      },
      {
        "field": "maxTopAvgUnits",
        "type": "Integer",
        "required": false,
        "name": "最高头部均月销量",
        "description": "300"
      },
      {
        "field": "minTopAvgRevenue",
        "type": "Float",
        "required": false,
        "name": "最低头部月均销售额",
        "description": "2000"
      },
      {
        "field": "maxTopAvgRevenue",
        "type": "Float",
        "required": false,
        "name": "最高头部月均销售额",
        "description": "3000"
      },
      {
        "field": "minTopAvgBsr",
        "type": "Integer",
        "required": false,
        "name": "最低头部平均BSR",
        "description": "68"
      },
      {
        "field": "maxTopAvgBsr",
        "type": "Integer",
        "required": false,
        "name": "最高头部平均BSR",
        "description": "998"
      },
      {
        "field": "minGoodsCount",
        "type": "Integer",
        "required": false,
        "name": "最低商品数量",
        "description": "40"
      },
      {
        "field": "maxGoodsCount",
        "type": "Integer",
        "required": false,
        "name": "最高商品数量",
        "description": "90"
      },
      {
        "field": "minBrands",
        "type": "Integer",
        "required": false,
        "name": "最小品牌数量",
        "description": "10"
      },
      {
        "field": "maxBrands",
        "type": "Integer",
        "required": false,
        "name": "最大品牌数量",
        "description": "20"
      },
      {
        "field": "minSellers",
        "type": "Integer",
        "required": false,
        "name": "最小卖家数量",
        "description": "6"
      },
      {
        "field": "maxSellers",
        "type": "Integer",
        "required": false,
        "name": "最大卖家数量",
        "description": "10"
      },
      {
        "field": "minAvgSellers",
        "type": "Float",
        "required": false,
        "name": "最小平均卖家数量",
        "description": "4.4"
      },
      {
        "field": "maxAvgSellers",
        "type": "Float",
        "required": false,
        "name": "最大平均卖家数量",
        "description": "10.4"
      },
      {
        "field": "minGoodsCrn",
        "type": "Float",
        "required": false,
        "name": "最小商品集中度",
        "description": "45"
      },
      {
        "field": "maxGoodsCrn",
        "type": "Float",
        "required": false,
        "name": "最大商品集中度",
        "description": "55"
      },
      {
        "field": "minBrandCrn",
        "type": "Float",
        "required": false,
        "name": "最小品牌集中度",
        "description": "45"
      },
      {
        "field": "maxBrandCrn",
        "type": "Float",
        "required": false,
        "name": "最大品牌集中度",
        "description": "55"
      },
      {
        "field": "maxSellerCrn",
        "type": "Float",
        "required": false,
        "name": "最小卖家集中度",
        "description": "45"
      },
      {
        "field": "minSellerCrn",
        "type": "Float",
        "required": false,
        "name": "最大卖家集中度",
        "description": "55"
      },
      {
        "field": "minEbcProportion",
        "type": "Float",
        "required": false,
        "name": "最小A+数量占比",
        "description": "34"
      },
      {
        "field": "maxEbcProportion",
        "type": "Float",
        "required": false,
        "name": "最大A+数量占比",
        "description": "54"
      },
      {
        "field": "minFbaProportion",
        "type": "Float",
        "required": false,
        "name": "最小FBA占比",
        "description": "34"
      },
      {
        "field": "maxFbaProportion",
        "type": "Float",
        "required": false,
        "name": "最大FBA占比",
        "description": "54"
      },
      {
        "field": "minFbmProportion",
        "type": "Float",
        "required": false,
        "name": "最小FBM占比",
        "description": "34"
      },
      {
        "field": "maxFbmProportion",
        "type": "Float",
        "required": false,
        "name": "最大FBM占比",
        "description": "54"
      },
      {
        "field": "minAmazonSelfProportion",
        "type": "Float",
        "required": false,
        "name": "最小Amazon自营占比",
        "description": "34"
      },
      {
        "field": "maxAmazonSelfProportion",
        "type": "Float",
        "required": false,
        "name": "最大Amazon自营占比",
        "description": "56"
      },
      {
        "field": "sellerLocation",
        "type": "String",
        "required": false,
        "name": "卖家所属地，见表1.3",
        "description": "US,GB"
      },
      {
        "field": "minNewProportion",
        "type": "Float",
        "required": false,
        "name": "最小新品数量占比",
        "description": "34"
      },
      {
        "field": "maxNewProportion",
        "type": "Float",
        "required": false,
        "name": "最大新品数量占比",
        "description": "56"
      },
      {
        "field": "minNewCount",
        "type": "Integer",
        "required": false,
        "name": "最小新品数量",
        "description": "4"
      },
      {
        "field": "maxNewCount",
        "type": "Integer",
        "required": false,
        "name": "最大新品数量",
        "description": "20"
      },
      {
        "field": "minNewAvgRatings",
        "type": "Integer",
        "required": false,
        "name": "最小新品平均评分数",
        "description": "23"
      },
      {
        "field": "maxNewAvgRatings",
        "type": "Integer",
        "required": false,
        "name": "最大新品平均评分数",
        "description": "554"
      },
      {
        "field": "minNewAvgPrice",
        "type": "Float",
        "required": false,
        "name": "最小新品平均价格",
        "description": "34"
      },
      {
        "field": "maxNewAvgPrice",
        "type": "Float",
        "required": false,
        "name": "最大新品平均价格",
        "description": "45"
      },
      {
        "field": "minNewAvgRating",
        "type": "Float",
        "required": false,
        "name": "最小新品平均星级",
        "description": "4"
      },
      {
        "field": "maxNewAvgRating",
        "type": "Float",
        "required": false,
        "name": "最大新品平均星级",
        "description": "4.5"
      },
      {
        "field": "minNewAvgUnits",
        "type": "Float",
        "required": false,
        "name": "最低新品月均销量",
        "description": "400"
      },
      {
        "field": "maxNewAvgUnits",
        "type": "Float",
        "required": false,
        "name": "最高新品月均销量",
        "description": "800"
      },
      {
        "field": "minNewAvgRevenue",
        "type": "Float",
        "required": false,
        "name": "最低新品月均销售额",
        "description": "900"
      },
      {
        "field": "maxNewAvgRevenue",
        "type": "Float",
        "required": false,
        "name": "最高新品月均销售额",
        "description": "2000"
      },
      {
        "field": "page",
        "type": "Integer",
        "required": false,
        "name": "页码，从 1 开始",
        "description": "默认：1"
      },
      {
        "field": "size",
        "type": "Integer",
        "required": false,
        "name": "每页条数",
        "description": "默认：50，最大：200"
      },
      {
        "field": "order",
        "type": "Object",
        "required": false,
        "name": "排序",
        "description": ""
      },
      {
        "field": "└field",
        "type": "String",
        "required": false,
        "name": "排序字段",
        "description": "见表1.6"
      },
      {
        "field": "└desc",
        "type": "boolean",
        "required": false,
        "name": "true为降序 false为升序",
        "description": "默认降序"
      }
    ],
    "responseFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": false,
        "name": "市场标志",
        "description": "US"
      },
      {
        "field": "currency",
        "type": "String",
        "required": false,
        "name": "该市场的货币类型",
        "description": "USD"
      },
      {
        "field": "nodeId",
        "type": "String",
        "required": false,
        "name": "节点ID",
        "description": "3732981"
      },
      {
        "field": "nodeLabelName",
        "type": "String",
        "required": false,
        "name": "节点名称",
        "description": "Mattresses"
      },
      {
        "field": "nodeIdPath",
        "type": "String",
        "required": false,
        "name": "节点ID路径",
        "description": "1055398:1063306:1063308:3732961:3732981"
      },
      {
        "field": "nodeLabelPath",
        "type": "String",
        "required": false,
        "name": "节点名称路径",
        "description": "Home & Kitchen:Furniture:Bedroom Furniture:Mattresses & Box Springs:Mattresses"
      },
      {
        "field": "nodeLabelLocale",
        "type": "String",
        "required": false,
        "name": "节点名称翻译",
        "description": "床垫"
      },
      {
        "field": "nodeLabelPathLocale",
        "type": "String",
        "required": false,
        "name": "节点名称路径翻译",
        "description": "家居用品 厨房:家具:家具卧室:床垫:床垫"
      },
      {
        "field": "totalProducts",
        "type": "Integer",
        "required": false,
        "name": "商品总数",
        "description": "1000"
      },
      {
        "field": "ranking",
        "type": "Integer",
        "required": false,
        "name": "排名",
        "description": "1"
      },
      {
        "field": "topProducts",
        "type": "Integer",
        "required": false,
        "name": "样本数量",
        "description": "100"
      },
      {
        "field": "brands",
        "type": "Integer",
        "required": false,
        "name": "品牌数量",
        "description": "34"
      },
      {
        "field": "sellers",
        "type": "Integer",
        "required": false,
        "name": "卖家数量",
        "description": "60"
      },
      {
        "field": "totalUnits",
        "type": "Integer",
        "required": false,
        "name": "月总销量",
        "description": "539009"
      },
      {
        "field": "totalRevenue",
        "type": "Float",
        "required": false,
        "name": "月总销售额",
        "description": "179950610.4"
      },
      {
        "field": "avgUnits",
        "type": "Integer",
        "required": false,
        "name": "月均销量",
        "description": "5390"
      },
      {
        "field": "avgRevenue",
        "type": "Float",
        "required": false,
        "name": "月均销售额",
        "description": "1799506"
      },
      {
        "field": "avgPrice",
        "type": "Float",
        "required": false,
        "name": "平均价格",
        "description": "296.11"
      },
      {
        "field": "avgRatings",
        "type": "Integer",
        "required": false,
        "name": "平均评分数",
        "description": "14591"
      },
      {
        "field": "avgRating",
        "type": "Float",
        "required": false,
        "name": "平均评分值",
        "description": "4.5"
      },
      {
        "field": "avgBsr",
        "type": "Integer",
        "required": false,
        "name": "平均BSR",
        "description": "198077"
      },
      {
        "field": "baseAvgVolume",
        "type": "Float",
        "required": false,
        "name": "平均体积(cm³)",
        "description": "529430.46"
      },
      {
        "field": "avgVolume",
        "type": "Float",
        "required": false,
        "name": "平均体积(in³)",
        "description": "32307.87"
      },
      {
        "field": "baseAvgWeight",
        "type": "Float",
        "required": false,
        "name": "平均重量(g)",
        "description": "35301.19"
      },
      {
        "field": "avgWeight",
        "type": "Float",
        "required": false,
        "name": "平均重量(pound)",
        "description": "77.8259"
      },
      {
        "field": "avgProfit",
        "type": "Float",
        "required": false,
        "name": "平均利润率",
        "description": "68.76"
      },
      {
        "field": "avgSellers",
        "type": "Float",
        "required": false,
        "name": "平均卖家数",
        "description": "3.3"
      },
      {
        "field": "ebcProportion",
        "type": "Float",
        "required": false,
        "name": "A+商品占比,百分比",
        "description": "80"
      },
      {
        "field": "amazonSelfProportion",
        "type": "Float",
        "required": false,
        "name": "Amazon自营占比,百分比",
        "description": "55"
      },
      {
        "field": "fbaProportion",
        "type": "Float",
        "required": false,
        "name": "FBA占比,百分比",
        "description": "22"
      },
      {
        "field": "fbmProportion",
        "type": "Float",
        "required": false,
        "name": "FBM占比,百分比",
        "description": "14"
      },
      {
        "field": "sellerNation",
        "type": "String",
        "required": false,
        "name": "最多卖家归属地 code，见表1.3",
        "description": "US"
      },
      {
        "field": "sellerNationLabel",
        "type": "String",
        "required": false,
        "name": "最多卖家归属地 label",
        "description": "美国"
      },
      {
        "field": "sellerProportion",
        "type": "Float",
        "required": false,
        "name": "最多卖家归属地 占比",
        "description": "59.3"
      },
      {
        "field": "top10Images",
        "type": "List",
        "required": false,
        "name": "前10商品的图片",
        "description": ""
      },
      {
        "field": "└asin",
        "type": "String",
        "required": false,
        "name": "asin",
        "description": "B01IU6RJYA"
      },
      {
        "field": "└image",
        "type": "String",
        "required": false,
        "name": "asin图片链接",
        "description": "https://images-na.ssl-images-amazon.com/images/I/51+5VVLcXSL._AC_US200_.jpg"
      },
      {
        "field": "returnRatio",
        "type": "Float",
        "required": false,
        "name": "退货率",
        "description": "3.51"
      },
      {
        "field": "avgReturnRatio",
        "type": "Float",
        "required": false,
        "name": "退货率类目平均值",
        "description": "5.54"
      },
      {
        "field": "searchToPurchaseRatio",
        "type": "Float",
        "required": false,
        "name": "搜索购买比,千分比",
        "description": "0.94926"
      },
      {
        "field": "top3ProductSales",
        "type": "Integer",
        "required": false,
        "name": "头部Listing前3名产品总销量",
        "description": ""
      },
      {
        "field": "top3BrandSales",
        "type": "Integer",
        "required": false,
        "name": "头部Listing前3名品牌总销量",
        "description": ""
      },
      {
        "field": "top3SellerSales",
        "type": "Integer",
        "required": false,
        "name": "头部Listing前3名卖家总销量",
        "description": ""
      },
      {
        "field": "top3ProductRevenue",
        "type": "Double",
        "required": false,
        "name": "头部Listing前3名产品总销售额",
        "description": ""
      },
      {
        "field": "top3BrandRevenue",
        "type": "Double",
        "required": false,
        "name": "头部Listing前3名品牌总销售额",
        "description": ""
      },
      {
        "field": "top3SellerRevenue",
        "type": "Double",
        "required": false,
        "name": "头部Listing前3名卖家总销售额",
        "description": ""
      },
      {
        "field": "top3ProductCrn",
        "type": "Double",
        "required": false,
        "name": "头部Listing前3名商品集中度",
        "description": ""
      },
      {
        "field": "top3BrandCrn",
        "type": "Double",
        "required": false,
        "name": "头部Listing前3名品牌集中度",
        "description": ""
      },
      {
        "field": "top3SellerCrn",
        "type": "Double",
        "required": false,
        "name": "头部Listing前3名卖家集中度",
        "description": ""
      },
      {
        "field": "top5ProductSales",
        "type": "Integer",
        "required": false,
        "name": "头部Listing前5名产品总销量",
        "description": ""
      },
      {
        "field": "top5BrandSales",
        "type": "Integer",
        "required": false,
        "name": "头部Listing前5名品牌总销量",
        "description": ""
      },
      {
        "field": "top5SellerSales",
        "type": "Integer",
        "required": false,
        "name": "头部Listing前5名卖家总销量",
        "description": ""
      },
      {
        "field": "top5ProductRevenue",
        "type": "Double",
        "required": false,
        "name": "头部Listing前5名产品总销售额",
        "description": ""
      },
      {
        "field": "top5BrandRevenue",
        "type": "Double",
        "required": false,
        "name": "头部Listing前5名品牌总销售额",
        "description": ""
      },
      {
        "field": "top5SellerRevenue",
        "type": "Double",
        "required": false,
        "name": "头部Listing前5名卖家总销售额",
        "description": ""
      },
      {
        "field": "top5ProductCrn",
        "type": "Double",
        "required": false,
        "name": "头部Listing前5名商品集中度",
        "description": ""
      },
      {
        "field": "top5BrandCrn",
        "type": "Double",
        "required": false,
        "name": "头部Listing前5名品牌集中度",
        "description": ""
      },
      {
        "field": "top5SellerCrn",
        "type": "Double",
        "required": false,
        "name": "头部Listing前5名卖家集中度",
        "description": ""
      },
      {
        "field": "top10ProductSales",
        "type": "Integer",
        "required": false,
        "name": "头部Listing前10名产品总销量",
        "description": ""
      },
      {
        "field": "top10BrandSales",
        "type": "Integer",
        "required": false,
        "name": "头部Listing前10名品牌总销量",
        "description": ""
      },
      {
        "field": "top10SellerSales",
        "type": "Integer",
        "required": false,
        "name": "头部Listing前10名卖家总销量",
        "description": ""
      },
      {
        "field": "top10ProductRevenue",
        "type": "Double",
        "required": false,
        "name": "头部Listing前10名产品总销售额",
        "description": ""
      },
      {
        "field": "top10BrandRevenue",
        "type": "Double",
        "required": false,
        "name": "头部Listing前10名品牌总销售额",
        "description": ""
      },
      {
        "field": "top10SellerRevenue",
        "type": "Double",
        "required": false,
        "name": "头部Listing前10名卖家总销售额",
        "description": ""
      },
      {
        "field": "top10ProductCrn",
        "type": "Double",
        "required": false,
        "name": "头部Listing前10名商品集中度",
        "description": ""
      },
      {
        "field": "top10BrandCrn",
        "type": "Double",
        "required": false,
        "name": "头部Listing前10名品牌集中度",
        "description": ""
      },
      {
        "field": "top10SellerCrn",
        "type": "Double",
        "required": false,
        "name": "头部Listing前10名卖家集中度",
        "description": ""
      },
      {
        "field": "top20ProductSales",
        "type": "Integer",
        "required": false,
        "name": "头部Listing前20名产品总销量",
        "description": ""
      },
      {
        "field": "top20BrandSales",
        "type": "Integer",
        "required": false,
        "name": "头部Listing前20名品牌总销量",
        "description": ""
      },
      {
        "field": "top20SellerSales",
        "type": "Integer",
        "required": false,
        "name": "头部Listing前20名卖家总销量",
        "description": ""
      },
      {
        "field": "top20ProductRevenue",
        "type": "Double",
        "required": false,
        "name": "头部Listing前20名产品总销售额",
        "description": ""
      },
      {
        "field": "top20BrandRevenue",
        "type": "Double",
        "required": false,
        "name": "头部Listing前20名品牌总销售额",
        "description": ""
      },
      {
        "field": "top20SellerRevenue",
        "type": "Double",
        "required": false,
        "name": "头部Listing前20名卖家总销售额",
        "description": ""
      },
      {
        "field": "top20ProductCrn",
        "type": "Double",
        "required": false,
        "name": "头部Listing前20名商品集中度",
        "description": ""
      },
      {
        "field": "top20BrandCrn",
        "type": "Double",
        "required": false,
        "name": "头部Listing前20名品牌集中度",
        "description": ""
      },
      {
        "field": "top20SellerCrn",
        "type": "Double",
        "required": false,
        "name": "头部Listing前20名卖家集中度",
        "description": ""
      },
      {
        "field": "l1NewRatio",
        "type": "Double",
        "required": false,
        "name": "最近1个月新品数量占比",
        "description": ""
      },
      {
        "field": "l1NewCount",
        "type": "Integer",
        "required": false,
        "name": "最近1个月新品数量",
        "description": ""
      },
      {
        "field": "l1NewAvgPrice",
        "type": "Double",
        "required": false,
        "name": "最近1个月新品平均价格",
        "description": ""
      },
      {
        "field": "l1NewAvgReviews",
        "type": "Integer",
        "required": false,
        "name": "最近1个月新品平均评论数",
        "description": ""
      },
      {
        "field": "l1NewAvgRating",
        "type": "Double",
        "required": false,
        "name": "最近1个月新品平均星级",
        "description": ""
      },
      {
        "field": "l1NewAvgSales",
        "type": "Integer",
        "required": false,
        "name": "最近1个月新品月均销量",
        "description": ""
      },
      {
        "field": "l1NewAvgRevenue",
        "type": "Double",
        "required": false,
        "name": "最近1个月新品月均销售额",
        "description": ""
      },
      {
        "field": "l3NewRatio",
        "type": "Double",
        "required": false,
        "name": "最近3个月新品数量占比",
        "description": ""
      },
      {
        "field": "l3NewCount",
        "type": "Integer",
        "required": false,
        "name": "最近3个月新品数量",
        "description": ""
      },
      {
        "field": "l3NewAvgPrice",
        "type": "Double",
        "required": false,
        "name": "最近3个月新品平均价格",
        "description": ""
      },
      {
        "field": "l3NewAvgReviews",
        "type": "Integer",
        "required": false,
        "name": "最近3个月新品平均评论数",
        "description": ""
      },
      {
        "field": "l3NewAvgRating",
        "type": "Double",
        "required": false,
        "name": "最近3个月新品平均星级",
        "description": ""
      },
      {
        "field": "l3NewAvgSales",
        "type": "Integer",
        "required": false,
        "name": "最近3个月新品月均销量",
        "description": ""
      },
      {
        "field": "l3NewAvgRevenue",
        "type": "Double",
        "required": false,
        "name": "最近3个月新品月均销售额",
        "description": ""
      },
      {
        "field": "l6NewRatio",
        "type": "Double",
        "required": false,
        "name": "最近6个月新品数量占比",
        "description": ""
      },
      {
        "field": "l6NewCount",
        "type": "Integer",
        "required": false,
        "name": "最近6个月新品数量",
        "description": ""
      },
      {
        "field": "l6NewAvgPrice",
        "type": "Double",
        "required": false,
        "name": "最近6个月新品平均价格",
        "description": ""
      },
      {
        "field": "l6NewAvgReviews",
        "type": "Integer",
        "required": false,
        "name": "最近6个月新品平均评论数",
        "description": ""
      },
      {
        "field": "l6NewAvgRating",
        "type": "Double",
        "required": false,
        "name": "最近6个月新品平均星级",
        "description": ""
      },
      {
        "field": "l6NewAvgSales",
        "type": "Integer",
        "required": false,
        "name": "最近6个月新品月均销量",
        "description": ""
      },
      {
        "field": "l6NewAvgRevenue",
        "type": "Double",
        "required": false,
        "name": "最近6个月新品月均销售额",
        "description": ""
      },
      {
        "field": "l12NewRatio",
        "type": "Double",
        "required": false,
        "name": "最近12个月新品数量占比",
        "description": ""
      },
      {
        "field": "l12NewCount",
        "type": "Integer",
        "required": false,
        "name": "最近12个月新品数量",
        "description": ""
      },
      {
        "field": "l12NewAvgPrice",
        "type": "Double",
        "required": false,
        "name": "最近12个月新品平均价格",
        "description": ""
      },
      {
        "field": "l12NewAvgReviews",
        "type": "Integer",
        "required": false,
        "name": "最近12个月新品平均评论数",
        "description": ""
      },
      {
        "field": "l12NewAvgRating",
        "type": "Double",
        "required": false,
        "name": "最近12个月新品平均星级",
        "description": ""
      },
      {
        "field": "l12NewAvgSales",
        "type": "Integer",
        "required": false,
        "name": "最近12个月新品月均销量",
        "description": ""
      },
      {
        "field": "l12NewAvgRevenue",
        "type": "Double",
        "required": false,
        "name": "最近12个月新品月均销售额",
        "description": ""
      }
    ]
  },
  {
    "operation": "MARKET_STATISTICS",
    "domain": "market",
    "responseShape": "object",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "站点编码",
        "description": "见表 1.2"
      },
      {
        "field": "month",
        "type": "String",
        "required": false,
        "name": "筛选日期,默认最近30天",
        "description": "见表 1.1"
      },
      {
        "field": "topN",
        "type": "Integer",
        "required": false,
        "name": "头部Listing数量",
        "description": "10"
      },
      {
        "field": "newProduct",
        "type": "Integer",
        "required": false,
        "name": "新品定义",
        "description": "6"
      },
      {
        "field": "nodeIdPath",
        "type": "String",
        "required": true,
        "name": "节点 id 路径字符串",
        "description": "1064954:1069242:1069784:1069820:1069838:1069828"
      }
    ],
    "responseFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": false,
        "name": "市场标志",
        "description": "US"
      },
      {
        "field": "currency",
        "type": "String",
        "required": false,
        "name": "该市场的货币类型",
        "description": "USD"
      },
      {
        "field": "nodeIdPath",
        "type": "String",
        "required": false,
        "name": "节点ID路径",
        "description": "1064954:1069242:1069784:1069820:1069838:1069828"
      },
      {
        "field": "nodeLabelPath",
        "type": "String",
        "required": false,
        "name": "节点名称路径",
        "description": "Office Products:Office & School Supplies:Writing & Correction Supplies:Pens & Refills:Rollerball Pens:Gel Ink Rollerball Pens"
      },
      {
        "field": "nodeLabelLocale",
        "type": "String",
        "required": false,
        "name": "节点名称翻译",
        "description": "办公产品:办公室:写作:钢笔:滚珠笔:中性笔"
      },
      {
        "field": "countryCode",
        "type": "String",
        "required": false,
        "name": "国家二简码",
        "description": "US"
      },
      {
        "field": "totalProducts",
        "type": "Integer",
        "required": false,
        "name": "商品总数",
        "description": "5127"
      },
      {
        "field": "products",
        "type": "Integer",
        "required": false,
        "name": "样品商品数",
        "description": "100"
      },
      {
        "field": "brands",
        "type": "Integer",
        "required": false,
        "name": "品牌数",
        "description": "4"
      },
      {
        "field": "sellers",
        "type": "Integer",
        "required": false,
        "name": "卖家数",
        "description": "58"
      },
      {
        "field": "avgBsr",
        "type": "Integer",
        "required": false,
        "name": "平均BSR",
        "description": "41970"
      },
      {
        "field": "baseAvgVolume",
        "type": "Float",
        "required": false,
        "name": "平均体积(cm³)",
        "description": "819942.68"
      },
      {
        "field": "avgVolume",
        "type": "Float",
        "required": false,
        "name": "平均体积(in³)",
        "description": "50035.97"
      },
      {
        "field": "baseAvgWeight",
        "type": "Float",
        "required": false,
        "name": "平均重量(g)",
        "description": "2460.95"
      },
      {
        "field": "avgWeight",
        "type": "Float",
        "required": false,
        "name": "平均重量(pound)",
        "description": "5.4255"
      },
      {
        "field": "avgProfit",
        "type": "Float",
        "required": false,
        "name": "平均利润率",
        "description": "66.03"
      },
      {
        "field": "avgUnits",
        "type": "Integer",
        "required": false,
        "name": "月均销量",
        "description": "26255"
      },
      {
        "field": "avgRevenue",
        "type": "Float",
        "required": false,
        "name": "月均销售额",
        "description": "344369"
      },
      {
        "field": "avgPrice",
        "type": "Float",
        "required": false,
        "name": "平均价格",
        "description": "13.91"
      },
      {
        "field": "avgRatingsCv",
        "type": "Integer",
        "required": false,
        "name": "月评论平均增长数",
        "description": "0"
      },
      {
        "field": "avgRatings",
        "type": "Integer",
        "required": false,
        "name": "平均评分数",
        "description": "19071"
      },
      {
        "field": "avgRating",
        "type": "Float",
        "required": false,
        "name": "平均星级",
        "description": "4.7"
      },
      {
        "field": "avgSellers",
        "type": "Float",
        "required": false,
        "name": "平均卖家数",
        "description": "5.2"
      },
      {
        "field": "hlProducts",
        "type": "Integer",
        "required": false,
        "name": "头部Listing前N名商品样本数",
        "description": "5"
      },
      {
        "field": "hlAvgBsr",
        "type": "Integer",
        "required": false,
        "name": "头部Listing前N名商品平均BSR",
        "description": "13126"
      },
      {
        "field": "hlAvgUnits",
        "type": "Integer",
        "required": false,
        "name": "头部Listing前N名商品月均销量",
        "description": "1123"
      },
      {
        "field": "hlAvgRevenue",
        "type": "Float",
        "required": false,
        "name": "头部Listing前N名商品月均销售额",
        "description": "12342.85"
      },
      {
        "field": "hlAvgPrice",
        "type": "Float",
        "required": false,
        "name": "头部Listing前N名商品平均价格",
        "description": "11.77"
      },
      {
        "field": "hlAvgRatingsCv",
        "type": "Integer",
        "required": false,
        "name": "头部Listing前N名商品月评论平均增长数",
        "description": "0"
      },
      {
        "field": "hlAvgRatings",
        "type": "Integer",
        "required": false,
        "name": "头部Listing前N名商品平均评论数",
        "description": "2794"
      },
      {
        "field": "hlAvgRating",
        "type": "Float",
        "required": false,
        "name": "头部Listing前N名商品平均星级",
        "description": "4.7"
      },
      {
        "field": "newProducts",
        "type": "Integer",
        "required": false,
        "name": "新品数量",
        "description": "67"
      },
      {
        "field": "newProductProportion",
        "type": "Float",
        "required": false,
        "name": "新品数量占比",
        "description": "67"
      },
      {
        "field": "newAvgPrice",
        "type": "Float",
        "required": false,
        "name": "新品平均价格",
        "description": "14.14"
      },
      {
        "field": "newAvgRatings",
        "type": "Integer",
        "required": false,
        "name": "新品平均评分数",
        "description": "24295"
      },
      {
        "field": "minNewRatings",
        "type": "Integer",
        "required": false,
        "name": "最低新品评分数",
        "description": "24"
      },
      {
        "field": "maxNewRatings",
        "type": "Integer",
        "required": false,
        "name": "最高新品评分数",
        "description": "6432"
      },
      {
        "field": "newAvgRating",
        "type": "Float",
        "required": false,
        "name": "新品平均星级",
        "description": "4.7"
      },
      {
        "field": "newAvgUnits",
        "type": "Integer",
        "required": false,
        "name": "新品月均销量",
        "description": "26425"
      },
      {
        "field": "newAvgRevenue",
        "type": "Float",
        "required": false,
        "name": "新品月均销售额",
        "description": "350209.91"
      },
      {
        "field": "firstShelfDate",
        "type": "String",
        "required": false,
        "name": "商品首次上架日期",
        "description": "2014-10-30"
      },
      {
        "field": "lastShelfDate",
        "type": "String",
        "required": false,
        "name": "商品最新上架日期",
        "description": "2021-04-28"
      }
    ]
  },
  {
    "operation": "MARKET_GOODS",
    "domain": "market",
    "responseShape": "list",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "站点编码",
        "description": "见表 1.2"
      },
      {
        "field": "month",
        "type": "String",
        "required": false,
        "name": "筛选日期,默认最近30天",
        "description": "见表 1.1"
      },
      {
        "field": "asins",
        "type": "List",
        "required": false,
        "name": "过滤asin",
        "description": "[\"B00P19MFYE\"]"
      },
      {
        "field": "topN",
        "type": "Integer",
        "required": false,
        "name": "头部Listing数量",
        "description": "10"
      },
      {
        "field": "newProduct",
        "type": "Integer",
        "required": false,
        "name": "新品定义",
        "description": "6"
      },
      {
        "field": "nodeIdPath",
        "type": "String",
        "required": true,
        "name": "节点 id 路径字符串",
        "description": "1064954:1069242:1069784:1069820:1069838:1069828"
      }
    ],
    "responseFields": [
      {
        "field": "title",
        "type": "String",
        "required": false,
        "name": "标题",
        "description": "Pilot G2, Dr. Grip Gel/Ltd, ExecuGel G6, Q7 Rollerball Gel Ink Pen Refills, 0.7mm, Fine Point, Black Ink, 3 Packs of 2"
      },
      {
        "field": "asin",
        "type": "String",
        "required": false,
        "name": "asin",
        "description": "B00P19MFYE"
      },
      {
        "field": "asinUrl",
        "type": "String",
        "required": false,
        "name": "asin链接",
        "description": "https://www.amazon.com/dp/B00P19MFYE"
      },
      {
        "field": "imageUrl",
        "type": "String",
        "required": false,
        "name": "图片链接",
        "description": "https://images-na.ssl-images-amazon.com/images/I/51hxvoxGnjL._AC_US200_.jpg"
      },
      {
        "field": "ranking",
        "type": "Integer",
        "required": false,
        "name": "排名",
        "description": "1"
      },
      {
        "field": "brand",
        "type": "String",
        "required": false,
        "name": "品牌",
        "description": "PILOT"
      },
      {
        "field": "sellerName",
        "type": "String",
        "required": false,
        "name": "卖家名称",
        "description": "JA Wholesale LLC"
      },
      {
        "field": "sellerType",
        "type": "String",
        "required": false,
        "name": "卖家类型",
        "description": "FBA"
      },
      {
        "field": "price",
        "type": "Float",
        "required": false,
        "name": "价格",
        "description": "6.19"
      },
      {
        "field": "shelfDate",
        "type": "String",
        "required": false,
        "name": "上架时间",
        "description": "2014-10-30"
      },
      {
        "field": "ratings",
        "type": "Integer",
        "required": false,
        "name": "评分数",
        "description": "5695"
      },
      {
        "field": "reviews",
        "type": "Integer",
        "required": false,
        "name": "评论数",
        "description": "133"
      },
      {
        "field": "rating",
        "type": "Float",
        "required": false,
        "name": "评论值",
        "description": "4.8"
      },
      {
        "field": "newFlag",
        "type": "Integer",
        "required": false,
        "name": "是否新品 1新品，0非新品",
        "description": "0"
      },
      {
        "field": "totalUnits",
        "type": "Integer",
        "required": false,
        "name": "总销量",
        "description": "2515"
      },
      {
        "field": "totalRevenue",
        "type": "Float",
        "required": false,
        "name": "总销额",
        "description": "18837.35"
      },
      {
        "field": "totalUnitsRatio",
        "type": "Float",
        "required": false,
        "name": "总销量占比",
        "description": "0.4478"
      },
      {
        "field": "totalRevenueRatio",
        "type": "Float",
        "required": false,
        "name": "总销额占比",
        "description": "0.3052"
      }
    ]
  },
  {
    "operation": "MARKET_BRAND",
    "domain": "market",
    "responseShape": "list",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "站点编码",
        "description": "见表 1.2"
      },
      {
        "field": "month",
        "type": "String",
        "required": false,
        "name": "筛选日期,默认最近30天",
        "description": "见表 1.1"
      },
      {
        "field": "topN",
        "type": "Integer",
        "required": false,
        "name": "头部Listing数量",
        "description": "10"
      },
      {
        "field": "newProduct",
        "type": "Integer",
        "required": false,
        "name": "新品定义",
        "description": "6"
      },
      {
        "field": "nodeIdPath",
        "type": "String",
        "required": true,
        "name": "节点 id 路径字符串",
        "description": "1064954:1069242:1069784:1069820:1069838:1069828"
      }
    ],
    "responseFields": [
      {
        "field": "brand",
        "type": "String",
        "required": false,
        "name": "品牌名称",
        "description": "PILOT"
      },
      {
        "field": "ranking",
        "type": "Integer",
        "required": false,
        "name": "排名",
        "description": "1"
      },
      {
        "field": "asins",
        "type": "List",
        "required": false,
        "name": "包含的商品ASIN集合",
        "description": "[\"B00P19MFYE\"]"
      },
      {
        "field": "products",
        "type": "Integer",
        "required": false,
        "name": "商品数量，包含新品",
        "description": "4"
      },
      {
        "field": "newProducts",
        "type": "Integer",
        "required": false,
        "name": "新品数量",
        "description": "1"
      },
      {
        "field": "newUnits",
        "type": "Integer",
        "required": false,
        "name": "新品销量",
        "description": "45"
      },
      {
        "field": "newRevenue",
        "type": "Float",
        "required": false,
        "name": "新品销售额",
        "description": "2342"
      },
      {
        "field": "newUnitsRatio",
        "type": "Float",
        "required": false,
        "name": "新品销量占比",
        "description": "4.3"
      },
      {
        "field": "newRevenueRatio",
        "type": "Float",
        "required": false,
        "name": "新品销售额占比",
        "description": "4"
      },
      {
        "field": "avgPrice",
        "type": "Float",
        "required": false,
        "name": "平均价格",
        "description": "6.19"
      },
      {
        "field": "ratings",
        "type": "Integer",
        "required": false,
        "name": "评分数",
        "description": "5695"
      },
      {
        "field": "rating",
        "type": "Float",
        "required": false,
        "name": "评分值",
        "description": "4.8"
      },
      {
        "field": "reviews",
        "type": "Integer",
        "required": false,
        "name": "评论数",
        "description": "234"
      },
      {
        "field": "totalUnits",
        "type": "Integer",
        "required": false,
        "name": "总销量",
        "description": "32342"
      },
      {
        "field": "totalRevenue",
        "type": "Float",
        "required": false,
        "name": "总销额",
        "description": "18837.35"
      },
      {
        "field": "totalUnitsRatio",
        "type": "Float",
        "required": false,
        "name": "总销量占比",
        "description": "0.4478"
      },
      {
        "field": "totalRevenueRatio",
        "type": "Float",
        "required": false,
        "name": "总销额占比",
        "description": "0.3052"
      }
    ]
  },
  {
    "operation": "MARKET_SELLER_LOCATION",
    "domain": "market",
    "responseShape": "list",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "站点编码",
        "description": "见表 1.2"
      },
      {
        "field": "month",
        "type": "String",
        "required": false,
        "name": "筛选日期,默认最近30天",
        "description": "见表 1.1"
      },
      {
        "field": "topN",
        "type": "Integer",
        "required": false,
        "name": "头部Listing数量",
        "description": "10"
      },
      {
        "field": "newProduct",
        "type": "Integer",
        "required": false,
        "name": "新品定义",
        "description": "6"
      },
      {
        "field": "nodeIdPath",
        "type": "String",
        "required": true,
        "name": "节点 id 路径字符串",
        "description": "1064954:1069242:1069784:1069820:1069838:1069828"
      }
    ],
    "responseFields": [
      {
        "field": "label",
        "type": "String",
        "required": false,
        "name": "类型说明",
        "description": "美国"
      },
      {
        "field": "country",
        "type": "String",
        "required": false,
        "name": "国家",
        "description": "美国"
      },
      {
        "field": "asins",
        "type": "List",
        "required": false,
        "name": "包含的asin列表",
        "description": "[\"B00P19MFYE\"]"
      },
      {
        "field": "products",
        "type": "Integer",
        "required": false,
        "name": "产品数",
        "description": "3"
      },
      {
        "field": "revenue",
        "type": "Float",
        "required": false,
        "name": "销售额",
        "description": "47492.83"
      },
      {
        "field": "units",
        "type": "Integer",
        "required": false,
        "name": "销量",
        "description": "4107"
      },
      {
        "field": "unitsRatio",
        "type": "Float",
        "required": false,
        "name": "销量占比",
        "description": "0.7313"
      },
      {
        "field": "revenueRatio",
        "type": "Float",
        "required": false,
        "name": "销售额占比",
        "description": "0.7794"
      }
    ]
  },
  {
    "operation": "MARKET_SELLER",
    "domain": "market",
    "responseShape": "list",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "站点编码",
        "description": "见表 1.2"
      },
      {
        "field": "month",
        "type": "String",
        "required": false,
        "name": "筛选日期,默认最近30天",
        "description": "见表 1.1"
      },
      {
        "field": "topN",
        "type": "Integer",
        "required": false,
        "name": "头部Listing数量",
        "description": "10"
      },
      {
        "field": "newProduct",
        "type": "Integer",
        "required": false,
        "name": "新品定义",
        "description": "6"
      },
      {
        "field": "nodeIdPath",
        "type": "String",
        "required": true,
        "name": "节点 id 路径字符串",
        "description": "1064954:1069242:1069784:1069820:1069838:1069828"
      }
    ],
    "responseFields": [
      {
        "field": "name",
        "type": "String",
        "required": false,
        "name": "卖家名称",
        "description": "JA Wholesale LLC"
      },
      {
        "field": "ranking",
        "type": "Integer",
        "required": false,
        "name": "排名",
        "description": "1"
      },
      {
        "field": "asinSet",
        "type": "List",
        "required": false,
        "name": "包含的商品ASIN集合",
        "description": "[\"B00P19MFYE\"]"
      },
      {
        "field": "products",
        "type": "Integer",
        "required": false,
        "name": "商品数量，包含新品",
        "description": "4"
      },
      {
        "field": "newProducts",
        "type": "Integer",
        "required": false,
        "name": "新品数量",
        "description": "1"
      },
      {
        "field": "newUnits",
        "type": "Integer",
        "required": false,
        "name": "新品销量",
        "description": "45"
      },
      {
        "field": "newRevenue",
        "type": "Float",
        "required": false,
        "name": "新品销售额",
        "description": "2342"
      },
      {
        "field": "newUnitsRatio",
        "type": "Float",
        "required": false,
        "name": "新品销量占比",
        "description": "4.3"
      },
      {
        "field": "newRevenueRatio",
        "type": "Float",
        "required": false,
        "name": "新品销售额占比",
        "description": "4"
      },
      {
        "field": "avgPrice",
        "type": "Float",
        "required": false,
        "name": "平均价格",
        "description": "6.19"
      },
      {
        "field": "ratings",
        "type": "Integer",
        "required": false,
        "name": "评分数",
        "description": "5695"
      },
      {
        "field": "rating",
        "type": "Float",
        "required": false,
        "name": "评分值",
        "description": "4.8"
      },
      {
        "field": "reviews",
        "type": "Integer",
        "required": false,
        "name": "评论数",
        "description": "234"
      },
      {
        "field": "totalUnits",
        "type": "Integer",
        "required": false,
        "name": "总销量",
        "description": "32342"
      },
      {
        "field": "totalRevenue",
        "type": "Float",
        "required": false,
        "name": "总销额",
        "description": "18837.35"
      },
      {
        "field": "totalUnitsRatio",
        "type": "Float",
        "required": false,
        "name": "总销量占比",
        "description": "0.4478"
      },
      {
        "field": "totalRevenueRatio",
        "type": "Float",
        "required": false,
        "name": "总销额占比",
        "description": "0.3052"
      }
    ]
  },
  {
    "operation": "MARKET_SELLER_TYPE",
    "domain": "market",
    "responseShape": "list",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "站点编码",
        "description": "见表 1.2"
      },
      {
        "field": "month",
        "type": "String",
        "required": false,
        "name": "筛选日期,默认最近30天",
        "description": "见表 1.1"
      },
      {
        "field": "topN",
        "type": "Integer",
        "required": false,
        "name": "头部Listing数量",
        "description": "10"
      },
      {
        "field": "newProduct",
        "type": "Integer",
        "required": false,
        "name": "新品定义",
        "description": "6"
      },
      {
        "field": "nodeIdPath",
        "type": "String",
        "required": true,
        "name": "节点 id 路径字符串",
        "description": "1064954:1069242:1069784:1069820:1069838:1069828"
      }
    ],
    "responseFields": [
      {
        "field": "label",
        "type": "String",
        "required": false,
        "name": "类型说明",
        "description": "Amazon自营"
      },
      {
        "field": "asinNum",
        "type": "Integer",
        "required": false,
        "name": "ASIN数量",
        "description": "4"
      },
      {
        "field": "asinRatio",
        "type": "Float",
        "required": false,
        "name": "ASIN数量占比",
        "description": "0.03"
      },
      {
        "field": "units",
        "type": "Integer",
        "required": false,
        "name": "月销量",
        "description": "79875"
      },
      {
        "field": "unitsRatio",
        "type": "Float",
        "required": false,
        "name": "月销量占比",
        "description": "0.0345"
      },
      {
        "field": "ratings",
        "type": "Integer",
        "required": false,
        "name": "评分数",
        "description": "6607"
      },
      {
        "field": "rating",
        "type": "Float",
        "required": false,
        "name": "评分值",
        "description": "4.7"
      },
      {
        "field": "productNum",
        "type": "Integer",
        "required": false,
        "name": "商品总数",
        "description": "3"
      }
    ]
  },
  {
    "operation": "MARKET_PERFORMANCE",
    "domain": "market",
    "responseShape": "object",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "市场 id",
        "description": "见表 1.2"
      },
      {
        "field": "month",
        "type": "String",
        "required": false,
        "name": "筛选日期,默认最近30天，最早查询时间为2021年7月份",
        "description": "见表 1.1"
      },
      {
        "field": "topN",
        "type": "Integer",
        "required": false,
        "name": "头部Listing数量",
        "description": "10"
      },
      {
        "field": "nodeIdPath",
        "type": "String",
        "required": true,
        "name": "节点 id 路径字符串",
        "description": "1064954:1069242:1069784:1069820:1069838:1069828"
      }
    ],
    "responseFields": [
      {
        "field": "asinCount",
        "type": "String",
        "required": false,
        "name": "asin数量",
        "description": "22187"
      },
      {
        "field": "returnRatio",
        "type": "String",
        "required": false,
        "name": "退货率，百分比",
        "description": "1.38"
      },
      {
        "field": "searchToPurchaseRatio",
        "type": "BigDecimal",
        "required": false,
        "name": "搜索购买比，千分比",
        "description": "3.17875"
      },
      {
        "field": "avgReturnRatio",
        "type": "BigDecimal",
        "required": false,
        "name": "类目平均退货率，百分比",
        "description": "2.72"
      },
      {
        "field": "avgSearchToPurchaseRatio",
        "type": "Float",
        "required": false,
        "name": "类目平均搜索购买比，千分比",
        "description": "2.6"
      },
      {
        "field": "items",
        "type": "List",
        "required": false,
        "name": "月浏览趋势",
        "description": ""
      },
      {
        "field": "└date",
        "type": "String",
        "required": false,
        "name": "时间，yyyy-MM-dd格式",
        "description": "2022-09-10"
      },
      {
        "field": "└glanceViews",
        "type": "Integer",
        "required": false,
        "name": "浏览量",
        "description": "2"
      }
    ]
  },
  {
    "operation": "MARKET_SHELF_TIME",
    "domain": "market",
    "responseShape": "list",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "站点编码",
        "description": "见表 1.2"
      },
      {
        "field": "month",
        "type": "String",
        "required": false,
        "name": "筛选日期,默认最近30天",
        "description": "见表 1.1"
      },
      {
        "field": "topN",
        "type": "Integer",
        "required": false,
        "name": "头部Listing数量",
        "description": "10"
      },
      {
        "field": "newProduct",
        "type": "Integer",
        "required": false,
        "name": "新品定义",
        "description": "6"
      },
      {
        "field": "nodeIdPath",
        "type": "String",
        "required": true,
        "name": "节点 id 路径字符串",
        "description": "1064954:1069242:1069784:1069820:1069838:1069828"
      }
    ],
    "responseFields": [
      {
        "field": "label",
        "type": "String",
        "required": false,
        "name": "类型说明",
        "description": "3年以上"
      },
      {
        "field": "shelfTime",
        "type": "String",
        "required": false,
        "name": "上架时间",
        "description": "3年以上"
      },
      {
        "field": "asins",
        "type": "List",
        "required": false,
        "name": "包含的asin列表",
        "description": "[\"B00P19MFYE\"]"
      },
      {
        "field": "products",
        "type": "Integer",
        "required": false,
        "name": "产品数",
        "description": "B07Z82895W"
      },
      {
        "field": "revenue",
        "type": "Float",
        "required": false,
        "name": "销售额",
        "description": "40846.76"
      },
      {
        "field": "units",
        "type": "Integer",
        "required": false,
        "name": "销量",
        "description": "4684"
      },
      {
        "field": "unitsRatio",
        "type": "Float",
        "required": false,
        "name": "销量占比",
        "description": "0.834"
      }
    ]
  },
  {
    "operation": "MARKET_SHELF_TREND",
    "domain": "market",
    "responseShape": "list",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": false,
        "name": "站点编码",
        "description": "见表 1.2"
      },
      {
        "field": "month",
        "type": "String",
        "required": false,
        "name": "筛选日期,默认最近30天",
        "description": "见表 1.1"
      },
      {
        "field": "topN",
        "type": "Integer",
        "required": false,
        "name": "头部Listing数量",
        "description": "10"
      },
      {
        "field": "newProduct",
        "type": "Integer",
        "required": false,
        "name": "新品定义",
        "description": "6"
      },
      {
        "field": "nodeIdPath",
        "type": "String",
        "required": false,
        "name": "节点 id 路径字符串",
        "description": "1064954:1069242:1069784:1069820:1069838:1069828"
      }
    ],
    "responseFields": [
      {
        "field": "label",
        "type": "String",
        "required": false,
        "name": "类型说明",
        "description": "2014"
      },
      {
        "field": "year",
        "type": "String",
        "required": false,
        "name": "年份，yyyy格式",
        "description": "2014"
      },
      {
        "field": "asins",
        "type": "List",
        "required": false,
        "name": "包含的asin列表",
        "description": "[\"B00P19MFYE\"]"
      },
      {
        "field": "products",
        "type": "Integer",
        "required": false,
        "name": "产品数",
        "description": "1"
      },
      {
        "field": "revenue",
        "type": "Float",
        "required": false,
        "name": "销售额",
        "description": "2515"
      },
      {
        "field": "units",
        "type": "Integer",
        "required": false,
        "name": "销量",
        "description": "18837.35"
      },
      {
        "field": "unitsRatio",
        "type": "Float",
        "required": false,
        "name": "销量占比",
        "description": "0.4478"
      }
    ]
  },
  {
    "operation": "MARKET_RATINGS",
    "domain": "market",
    "responseShape": "list",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "站点编码",
        "description": "见表 1.2"
      },
      {
        "field": "month",
        "type": "String",
        "required": false,
        "name": "筛选日期,默认最近30天",
        "description": "见表 1.1"
      },
      {
        "field": "topN",
        "type": "Integer",
        "required": false,
        "name": "头部Listing数量",
        "description": "10"
      },
      {
        "field": "newProduct",
        "type": "Integer",
        "required": false,
        "name": "新品定义",
        "description": "6"
      },
      {
        "field": "nodeIdPath",
        "type": "String",
        "required": true,
        "name": "节点 id 路径字符串",
        "description": "1064954:1069242:1069784:1069820:1069838:1069828"
      }
    ],
    "responseFields": [
      {
        "field": "label",
        "type": "String",
        "required": false,
        "name": "类型说明",
        "description": "500以上"
      },
      {
        "field": "asins",
        "type": "List",
        "required": false,
        "name": "包含的asin列表",
        "description": "5"
      },
      {
        "field": "products",
        "type": "Integer",
        "required": false,
        "name": "产品数",
        "description": "[\"B00P19MFYE\"]"
      },
      {
        "field": "revenue",
        "type": "Float",
        "required": false,
        "name": "销售额",
        "description": "61714.24"
      },
      {
        "field": "units",
        "type": "Integer",
        "required": false,
        "name": "销量",
        "description": "5616"
      },
      {
        "field": "unitsRatio",
        "type": "Float",
        "required": false,
        "name": "销量占比",
        "description": "0.9743"
      }
    ]
  },
  {
    "operation": "MARKET_RATING",
    "domain": "market",
    "responseShape": "list",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "站点编码",
        "description": "见表 1.2"
      },
      {
        "field": "month",
        "type": "String",
        "required": false,
        "name": "筛选日期,默认最近30天",
        "description": "见表 1.1"
      },
      {
        "field": "topN",
        "type": "Integer",
        "required": false,
        "name": "头部Listing数量",
        "description": "10"
      },
      {
        "field": "newProduct",
        "type": "Integer",
        "required": false,
        "name": "新品定义",
        "description": "6"
      },
      {
        "field": "nodeIdPath",
        "type": "String",
        "required": true,
        "name": "节点 id 路径字符串",
        "description": "1064954:1069242:1069784:1069820:1069838:1069828"
      }
    ],
    "responseFields": [
      {
        "field": "label",
        "type": "String",
        "required": false,
        "name": "类型说明",
        "description": "4.5以上"
      },
      {
        "field": "asins",
        "type": "List",
        "required": false,
        "name": "包含的asin列表",
        "description": "[\"B00P19MFYE\"]"
      },
      {
        "field": "products",
        "type": "Integer",
        "required": false,
        "name": "产品数",
        "description": "5"
      },
      {
        "field": "revenue",
        "type": "Float",
        "required": false,
        "name": "销售额",
        "description": "59934.22"
      },
      {
        "field": "units",
        "type": "Integer",
        "required": false,
        "name": "销量",
        "description": "5418"
      },
      {
        "field": "unitsRatio",
        "type": "Float",
        "required": false,
        "name": "销量占比",
        "description": "0.9647"
      }
    ]
  },
  {
    "operation": "MARKET_PRICE",
    "domain": "market",
    "responseShape": "list",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "站点编码",
        "description": "见表 1.2"
      },
      {
        "field": "month",
        "type": "String",
        "required": false,
        "name": "筛选日期,默认最近30天",
        "description": "见表 1.1"
      },
      {
        "field": "topN",
        "type": "Integer",
        "required": false,
        "name": "头部Listing数量",
        "description": "10"
      },
      {
        "field": "newProduct",
        "type": "Integer",
        "required": false,
        "name": "新品定义",
        "description": "6"
      },
      {
        "field": "nodeIdPath",
        "type": "String",
        "required": true,
        "name": "节点 id 路径字符串",
        "description": "1064954:1069242:1069784:1069820:1069838:1069828"
      }
    ],
    "responseFields": [
      {
        "field": "label",
        "type": "String",
        "required": false,
        "name": "类型说明",
        "description": "5-10"
      },
      {
        "field": "asins",
        "type": "List",
        "required": false,
        "name": "包含的asin列表",
        "description": "[\"B00P19MFYE\"]"
      },
      {
        "field": "products",
        "type": "Integer",
        "required": false,
        "name": "产品数",
        "description": "3"
      },
      {
        "field": "revenue",
        "type": "Float",
        "required": false,
        "name": "销售额",
        "description": "33058.76"
      },
      {
        "field": "units",
        "type": "Integer",
        "required": false,
        "name": "销量",
        "description": "4024"
      },
      {
        "field": "unitsRatio",
        "type": "Float",
        "required": false,
        "name": "销量占比",
        "description": "0.7165"
      }
    ]
  },
  {
    "operation": "MARKET_EBC",
    "domain": "market",
    "responseShape": "list",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "站点编码",
        "description": "见表 1.2"
      },
      {
        "field": "month",
        "type": "String",
        "required": false,
        "name": "筛选日期,默认最近30天",
        "description": "见表 1.1"
      },
      {
        "field": "topN",
        "type": "Integer",
        "required": false,
        "name": "头部Listing数量",
        "description": "10"
      },
      {
        "field": "newProduct",
        "type": "Integer",
        "required": false,
        "name": "新品定义",
        "description": "6"
      },
      {
        "field": "nodeIdPath",
        "type": "String",
        "required": true,
        "name": "节点 id 路径字符串",
        "description": "1064954:1069242:1069784:1069820:1069838:1069828"
      }
    ],
    "responseFields": [
      {
        "field": "label",
        "type": "String",
        "required": false,
        "name": "类型说明",
        "description": "有A+有视频"
      },
      {
        "field": "products",
        "type": "Integer",
        "required": false,
        "name": "产品数",
        "description": "1"
      },
      {
        "field": "productsRatio",
        "type": "Float",
        "required": false,
        "name": "类目名称产品占比",
        "description": "20"
      },
      {
        "field": "units",
        "type": "Integer",
        "required": false,
        "name": "销量",
        "description": "1311"
      },
      {
        "field": "unitsRatio",
        "type": "Float",
        "required": false,
        "name": "销量占比",
        "description": "23.34"
      }
    ]
  },
  {
    "operation": "REVIEW_LIST",
    "domain": "review",
    "responseShape": "page",
    "requestFields": [
      {
        "field": "marketplace",
        "type": "String",
        "required": true,
        "name": "市场",
        "description": "见表 1.2"
      },
      {
        "field": "asin",
        "type": "String",
        "required": true,
        "name": "ASIN",
        "description": ""
      },
      {
        "field": "starList",
        "type": "List",
        "required": false,
        "name": "评论星级",
        "description": "1: 一星, 2: 二星, 3: 三星, 4: 四星, 5: 五星"
      },
      {
        "field": "typeList",
        "type": "List",
        "required": false,
        "name": "评论类型",
        "description": "1：图片评论, 2：视频评论, 3：VP评论, 4：vine评论"
      },
      {
        "field": "page",
        "type": "Integer",
        "required": false,
        "name": "页码，从 1 开始",
        "description": "默认：1"
      },
      {
        "field": "size",
        "type": "Integer",
        "required": false,
        "name": "每页条数，最大10",
        "description": "默认：5"
      }
    ],
    "responseFields": [
      {
        "field": "author",
        "type": "String",
        "required": false,
        "name": "用户",
        "description": ""
      },
      {
        "field": "title",
        "type": "String",
        "required": false,
        "name": "标题",
        "description": ""
      },
      {
        "field": "content",
        "type": "String",
        "required": false,
        "name": "评论内容",
        "description": ""
      },
      {
        "field": "date",
        "type": "Long",
        "required": false,
        "name": "日期（时间戳）",
        "description": "1772380800000"
      },
      {
        "field": "star",
        "type": "Integer",
        "required": false,
        "name": "星级",
        "description": ""
      },
      {
        "field": "authorLabels",
        "type": "List",
        "required": false,
        "name": "评论人标签",
        "description": ""
      },
      {
        "field": "skus",
        "type": "List",
        "required": false,
        "name": "sku信息",
        "description": ""
      },
      {
        "field": "images",
        "type": "List",
        "required": false,
        "name": "图片链接",
        "description": ""
      },
      {
        "field": "videos",
        "type": "List",
        "required": false,
        "name": "视频链接",
        "description": ""
      },
      {
        "field": "likes",
        "type": "Integer",
        "required": false,
        "name": "点赞数",
        "description": ""
      },
      {
        "field": "image",
        "type": "Boolean",
        "required": false,
        "name": "是否图片评论",
        "description": ""
      },
      {
        "field": "video",
        "type": "Boolean",
        "required": false,
        "name": "是否视频评论",
        "description": ""
      },
      {
        "field": "verified",
        "type": "Boolean",
        "required": false,
        "name": "是否实际购买评论",
        "description": ""
      },
      {
        "field": "vine",
        "type": "Boolean",
        "required": false,
        "name": "是否特邀评论",
        "description": ""
      },
      {
        "field": "free",
        "type": "Boolean",
        "required": false,
        "name": "是否免费评论",
        "description": ""
      },
      {
        "field": "experience",
        "type": "Boolean",
        "required": false,
        "name": "是否抢先体验评论",
        "description": ""
      }
    ]
  },
  {
    "operation": "OCR",
    "domain": "tool",
    "responseShape": "object",
    "requestFields": [
      {
        "field": "type",
        "type": "Integer",
        "required": true,
        "name": "0：远程图片；1：base64字符串；2：图片文件",
        "description": "2"
      },
      {
        "field": "fn",
        "type": "String",
        "required": true,
        "name": "需要识别的语言种类 CHINESE:中文 LATIN:拉丁文",
        "description": "CHINESE"
      },
      {
        "field": "url",
        "type": "String",
        "required": false,
        "name": "远程url",
        "description": "https://o.sellersprite.com/docs/202310/sellersprite-2023101210394300742.jpg"
      },
      {
        "field": "base64",
        "type": "String",
        "required": false,
        "name": "base64字符串",
        "description": ""
      },
      {
        "field": "image",
        "type": "File",
        "required": false,
        "name": "上传的文件",
        "description": "C:\\fakepath\\人像.jpeg"
      }
    ],
    "responseFields": [
      {
        "field": "data",
        "type": "String",
        "required": false,
        "name": "识别的文字",
        "description": "卖家精灵"
      },
      {
        "field": "code",
        "type": "Integer",
        "required": false,
        "name": "状态码",
        "description": "OK"
      },
      {
        "field": "message",
        "type": "String",
        "required": false,
        "name": "状态码描述",
        "description": "成功"
      }
    ]
  },
  {
    "operation": "GLOBAL_BRAND_RANGE",
    "domain": "trademark",
    "responseShape": "list",
    "requestFields": [],
    "responseFields": [
      {
        "field": "office",
        "type": "String",
        "required": false,
        "name": "简码",
        "description": "AD"
      },
      {
        "field": "officeLabel",
        "type": "String",
        "required": false,
        "name": "中文名称",
        "description": "安道尔"
      }
    ]
  },
  {
    "operation": "GLOBAL_BRAND_DETAIL",
    "domain": "trademark",
    "responseShape": "object",
    "requestFields": [
      {
        "field": "office",
        "type": "String",
        "required": true,
        "name": "数据范围，见上一个接口",
        "description": "US"
      },
      {
        "field": "brandId",
        "type": "String",
        "required": true,
        "name": "id,见列表接口",
        "description": "US502022097612203"
      }
    ],
    "responseFields": [
      {
        "field": "id",
        "type": "String",
        "required": false,
        "name": "id",
        "description": "US502022097612203"
      },
      {
        "field": "applicant",
        "type": "List",
        "required": false,
        "name": "申请人",
        "description": "ANKER INC"
      },
      {
        "field": "applicantCountryCode",
        "type": "Integer",
        "required": false,
        "name": "申请人国家",
        "description": "US"
      },
      {
        "field": "applicants",
        "type": "List",
        "required": false,
        "name": "申请人详情",
        "description": "格式同office,结构见下表"
      },
      {
        "field": "applicationDate",
        "type": "String",
        "required": false,
        "name": "申请日期",
        "description": "2022-09-29"
      },
      {
        "field": "applicationLanguageCode",
        "type": "String",
        "required": false,
        "name": "申请语言",
        "description": "en"
      },
      {
        "field": "applicationNumber",
        "type": "String",
        "required": false,
        "name": "申请编号",
        "description": "97612203"
      },
      {
        "field": "registrationNumber",
        "type": "String",
        "required": false,
        "name": "注册号",
        "description": "4590785"
      },
      {
        "field": "applicationRefNumber",
        "type": "List",
        "required": false,
        "name": "申请参考号",
        "description": ""
      },
      {
        "field": "brandName",
        "type": "List",
        "required": false,
        "name": "品牌名",
        "description": "[ \"1ST AID\"]"
      },
      {
        "field": "collection",
        "type": "String",
        "required": false,
        "name": "数据集",
        "description": "ustm"
      },
      {
        "field": "designatedCountries",
        "type": "List",
        "required": false,
        "name": "指定国家",
        "description": "[\"US\"]"
      },
      {
        "field": "designation",
        "type": "List",
        "required": false,
        "name": "指定国家",
        "description": "[\"US\"]"
      },
      {
        "field": "filingPlace",
        "type": "String",
        "required": false,
        "name": "申请地点",
        "description": ""
      },
      {
        "field": "kind",
        "type": "List",
        "required": false,
        "name": "商标类别",
        "description": "[\"Individual\"]"
      },
      {
        "field": "logos",
        "type": "List",
        "required": false,
        "name": "logo",
        "description": ""
      },
      {
        "field": "└logo",
        "type": "String",
        "required": false,
        "name": "logo",
        "description": ""
      },
      {
        "field": "└logoUrl",
        "type": "String",
        "required": false,
        "name": "logo url",
        "description": ""
      },
      {
        "field": "markFeature",
        "type": "String",
        "required": false,
        "name": "商标种类",
        "description": "Combined"
      },
      {
        "field": "niceClass",
        "type": "List",
        "required": false,
        "name": "尼斯分类",
        "description": "[5]"
      },
      {
        "field": "office",
        "type": "String",
        "required": false,
        "name": "知识产权局",
        "description": "US"
      },
      {
        "field": "status",
        "type": "String",
        "required": false,
        "name": "状态",
        "description": "Pending"
      },
      {
        "field": "statusDate",
        "type": "String",
        "required": false,
        "name": "状态更新日期",
        "description": "2023-05-02"
      },
      {
        "field": "type",
        "type": "String",
        "required": false,
        "name": "类型",
        "description": "TRADEMARK"
      },
      {
        "field": "appeals",
        "type": "List",
        "required": false,
        "name": "上诉信息",
        "description": ""
      },
      {
        "field": "└date",
        "type": "String",
        "required": false,
        "name": "日期",
        "description": ""
      },
      {
        "field": "└kind",
        "type": "String",
        "required": false,
        "name": "分类",
        "description": ""
      },
      {
        "field": "correspondence",
        "type": "AddressDto",
        "required": false,
        "name": "通信地址",
        "description": ""
      },
      {
        "field": "events",
        "type": "List",
        "required": false,
        "name": "事件",
        "description": ""
      },
      {
        "field": "└date",
        "type": "String",
        "required": false,
        "name": "日期",
        "description": ""
      },
      {
        "field": "└officeKind",
        "type": "String",
        "required": false,
        "name": "产权局分类",
        "description": ""
      },
      {
        "field": "└gbdKind",
        "type": "String",
        "required": false,
        "name": "品牌分析",
        "description": ""
      },
      {
        "field": "└doc",
        "type": "String",
        "required": false,
        "name": "文档",
        "description": ""
      },
      {
        "field": "└country",
        "type": "String",
        "required": false,
        "name": "国家",
        "description": ""
      },
      {
        "field": "expiryDate",
        "type": "String",
        "required": false,
        "name": "过期时间",
        "description": ""
      },
      {
        "field": "extra",
        "type": "String",
        "required": false,
        "name": "扩展信息",
        "description": ""
      },
      {
        "field": "gbdStatus",
        "type": "String",
        "required": false,
        "name": "品牌状态",
        "description": ""
      },
      {
        "field": "goodsServicesClassification",
        "type": "Object",
        "required": false,
        "name": "商品分类信息",
        "description": ""
      },
      {
        "field": "└kind",
        "type": "String",
        "required": false,
        "name": "类型",
        "description": ""
      },
      {
        "field": "└version",
        "type": "String",
        "required": false,
        "name": "版本",
        "description": ""
      },
      {
        "field": "└classification",
        "type": "String",
        "required": false,
        "name": "详情",
        "description": ""
      },
      {
        "field": "└└code",
        "type": "String",
        "required": false,
        "name": "code码",
        "description": ""
      },
      {
        "field": "└└terms",
        "type": "Map",
        "required": false,
        "name": "说明",
        "description": ""
      },
      {
        "field": "goodsServicesUnclassified",
        "type": "Map",
        "required": false,
        "name": "商品未分类信息",
        "description": ""
      },
      {
        "field": "markDescriptionDetails",
        "type": "List",
        "required": false,
        "name": "商标描述细节",
        "description": ""
      },
      {
        "field": "└text",
        "type": "String",
        "required": false,
        "name": "描述",
        "description": "ANKER INC"
      },
      {
        "field": "└languageCode",
        "type": "String",
        "required": false,
        "name": "语言",
        "description": "en"
      },
      {
        "field": "markDisclaimerDetails",
        "type": "List",
        "required": false,
        "name": "商标免责声明",
        "description": ""
      },
      {
        "field": "└text",
        "type": "String",
        "required": false,
        "name": "描述",
        "description": "ANKER INC"
      },
      {
        "field": "└languageCode",
        "type": "String",
        "required": false,
        "name": "语言",
        "description": "en"
      },
      {
        "field": "markImageDetails",
        "type": "JSONArray",
        "required": false,
        "name": "商标图形分类",
        "description": ""
      },
      {
        "field": "nationalGoodsServicesClassification",
        "type": "Object",
        "required": false,
        "name": "国际商品分类信息",
        "description": ""
      },
      {
        "field": "└kind",
        "type": "String",
        "required": false,
        "name": "类型",
        "description": ""
      },
      {
        "field": "└version",
        "type": "String",
        "required": false,
        "name": "版本",
        "description": ""
      },
      {
        "field": "└classification",
        "type": "String",
        "required": false,
        "name": "详情",
        "description": ""
      },
      {
        "field": "└└code",
        "type": "String",
        "required": false,
        "name": "code码",
        "description": ""
      },
      {
        "field": "└└terms",
        "type": "Map",
        "required": false,
        "name": "说明",
        "description": ""
      },
      {
        "field": "officeStatus",
        "type": "String",
        "required": false,
        "name": "办公状态",
        "description": ""
      },
      {
        "field": "priorities",
        "type": "List",
        "required": false,
        "name": "优先事项",
        "description": ""
      },
      {
        "field": "└severity",
        "type": "String",
        "required": false,
        "name": "级别",
        "description": ""
      },
      {
        "field": "└code",
        "type": "String",
        "required": false,
        "name": "code码",
        "description": ""
      },
      {
        "field": "└field",
        "type": "String",
        "required": false,
        "name": "字段",
        "description": ""
      },
      {
        "field": "└type",
        "type": "String",
        "required": false,
        "name": "类型",
        "description": ""
      },
      {
        "field": "└message",
        "type": "String",
        "required": false,
        "name": "说明",
        "description": ""
      },
      {
        "field": "publicationDate",
        "type": "String",
        "required": false,
        "name": "发表日期",
        "description": ""
      },
      {
        "field": "publications",
        "type": "List",
        "required": false,
        "name": "发表详情",
        "description": ""
      },
      {
        "field": "└date",
        "type": "String",
        "required": false,
        "name": "日期",
        "description": ""
      },
      {
        "field": "└identifier",
        "type": "String",
        "required": false,
        "name": "标志",
        "description": ""
      },
      {
        "field": "└section",
        "type": "String",
        "required": false,
        "name": "内容",
        "description": ""
      },
      {
        "field": "qc",
        "type": "List",
        "required": false,
        "name": "审核意见",
        "description": ""
      },
      {
        "field": "└severity",
        "type": "String",
        "required": false,
        "name": "级别",
        "description": ""
      },
      {
        "field": "└code",
        "type": "String",
        "required": false,
        "name": "code码",
        "description": ""
      },
      {
        "field": "└field",
        "type": "String",
        "required": false,
        "name": "字段",
        "description": ""
      },
      {
        "field": "└type",
        "type": "String",
        "required": false,
        "name": "类型",
        "description": ""
      },
      {
        "field": "└message",
        "type": "String",
        "required": false,
        "name": "说明",
        "description": ""
      },
      {
        "field": "reference",
        "type": "Object",
        "required": false,
        "name": "参考信息",
        "description": ""
      },
      {
        "field": "└office",
        "type": "String",
        "required": false,
        "name": "机构code",
        "description": ""
      },
      {
        "field": "└application",
        "type": "Object",
        "required": false,
        "name": "申请信息",
        "description": ""
      },
      {
        "field": "└└date",
        "type": "String",
        "required": false,
        "name": "日期",
        "description": ""
      },
      {
        "field": "└└number",
        "type": "String",
        "required": false,
        "name": "编号",
        "description": ""
      },
      {
        "field": "└registration",
        "type": "Object",
        "required": false,
        "name": "注册信息",
        "description": ""
      },
      {
        "field": "└└date",
        "type": "String",
        "required": false,
        "name": "日期",
        "description": ""
      },
      {
        "field": "└└number",
        "type": "String",
        "required": false,
        "name": "编号",
        "description": ""
      },
      {
        "field": "refOffice",
        "type": "String",
        "required": false,
        "name": "参考办公室",
        "description": ""
      },
      {
        "field": "registrationDate",
        "type": "String",
        "required": false,
        "name": "注册日期",
        "description": ""
      },
      {
        "field": "registrationOfficeCode",
        "type": "String",
        "required": false,
        "name": "注册国家",
        "description": ""
      },
      {
        "field": "registrationRefNumber",
        "type": "List",
        "required": false,
        "name": "注册参考号",
        "description": ""
      },
      {
        "field": "representatives",
        "type": "List",
        "required": false,
        "name": "代表信息",
        "description": ""
      },
      {
        "field": "secondLanguageCode",
        "type": "String",
        "required": false,
        "name": "第二语言",
        "description": ""
      },
      {
        "field": "st13",
        "type": "String",
        "required": false,
        "name": "id",
        "description": ""
      },
      {
        "field": "terminationDate",
        "type": "String",
        "required": false,
        "name": "终止日期",
        "description": ""
      },
      {
        "field": "wordMarkSpecification",
        "type": "Object",
        "required": false,
        "name": "文字商标说明",
        "description": ""
      },
      {
        "field": "└markTransliteration",
        "type": "String",
        "required": false,
        "name": "markTransliteration",
        "description": ""
      },
      {
        "field": "└markTranslation",
        "type": "Object",
        "required": false,
        "name": "商标翻译",
        "description": ""
      },
      {
        "field": "└└text",
        "type": "String",
        "required": false,
        "name": "内容",
        "description": ""
      },
      {
        "field": "└└languageCode",
        "type": "String",
        "required": false,
        "name": "语言类型",
        "description": ""
      },
      {
        "field": "└markVerbalElement",
        "type": "Object",
        "required": false,
        "name": "markVerbalElement",
        "description": ""
      },
      {
        "field": "└└text",
        "type": "String",
        "required": false,
        "name": "内容",
        "description": ""
      },
      {
        "field": "└└languageCode",
        "type": "String",
        "required": false,
        "name": "语言类型",
        "description": ""
      },
      {
        "field": "└markSignificantVerbalElement",
        "type": "Object",
        "required": false,
        "name": "markSignificantVerbalElement",
        "description": ""
      },
      {
        "field": "└└text",
        "type": "String",
        "required": false,
        "name": "内容",
        "description": "SONICARE"
      },
      {
        "field": "└└languageCode",
        "type": "String",
        "required": false,
        "name": "语言类型",
        "description": "en"
      },
      {
        "field": "appeals",
        "type": "List",
        "required": false,
        "name": "上诉信息",
        "description": ""
      }
    ]
  },
  {
    "operation": "GLOBAL_BRAND_LIST",
    "domain": "trademark",
    "responseShape": "page",
    "requestFields": [
      {
        "field": "office",
        "type": "List",
        "required": false,
        "name": "数据范围，见上一个接口",
        "description": "[\"US\"]"
      },
      {
        "field": "text",
        "type": "String",
        "required": true,
        "name": "查询文本",
        "description": "CHINESE"
      },
      {
        "field": "imageBase64",
        "type": "String",
        "required": false,
        "name": "base64字符串",
        "description": ""
      },
      {
        "field": "imageFile",
        "type": "File",
        "required": false,
        "name": "上传的文件",
        "description": "C:\\fakepath\\人像.jpeg"
      },
      {
        "field": "brandName",
        "type": "List",
        "required": false,
        "name": "品牌名，字段参数见统计接口",
        "description": "[\"ADVENTURE CLUB\"]"
      },
      {
        "field": "status",
        "type": "List",
        "required": false,
        "name": "状态，字段参数见统计接口",
        "description": "[\"Registered\"]"
      },
      {
        "field": "applicant",
        "type": "List",
        "required": false,
        "name": "申请人，字段参数见统计接口",
        "description": "[\"ANKER INC\"]"
      },
      {
        "field": "niceClass",
        "type": "List",
        "required": false,
        "name": "尼斯分类，字段参数见统计接口",
        "description": "[5]"
      },
      {
        "field": "applicationYear",
        "type": "List",
        "required": false,
        "name": "申请年份，字段参数见统计接口",
        "description": "[\"1985\"]"
      },
      {
        "field": "expiryYear",
        "type": "List",
        "required": false,
        "name": "过期年份，字段参数见统计接口",
        "description": "[\"2026\"]"
      },
      {
        "field": "order.field",
        "type": "String",
        "required": false,
        "name": "排序字段，默认相关度，applicationDate申请日期",
        "description": ""
      },
      {
        "field": "order.desc",
        "type": "Boolean",
        "required": false,
        "name": "true降序，false升序，默认true",
        "description": ""
      },
      {
        "field": "order.field",
        "type": "String",
        "required": false,
        "name": "排序字段，默认相关度，applicationDate申请日期",
        "description": ""
      },
      {
        "field": "order.desc",
        "type": "Boolean",
        "required": false,
        "name": "true降序，false升序，默认true",
        "description": ""
      },
      {
        "field": "page",
        "type": "Integer",
        "required": false,
        "name": "页码",
        "description": "1"
      },
      {
        "field": "size",
        "type": "Integer",
        "required": false,
        "name": "每页条数，最大100",
        "description": "20"
      }
    ],
    "responseFields": [
      {
        "field": "id",
        "type": "String",
        "required": false,
        "name": "id",
        "description": "US502022097612203"
      },
      {
        "field": "applicant",
        "type": "List",
        "required": false,
        "name": "申请人",
        "description": "ANKER INC"
      },
      {
        "field": "applicantCountryCode",
        "type": "Integer",
        "required": false,
        "name": "申请人国家",
        "description": "US"
      },
      {
        "field": "applicants",
        "type": "List",
        "required": false,
        "name": "申请人详情",
        "description": "格式同office"
      },
      {
        "field": "└kind",
        "type": "String",
        "required": false,
        "name": "类型",
        "description": "Legal Entity"
      },
      {
        "field": "└identifier",
        "type": "String",
        "required": false,
        "name": "标识",
        "description": "33744042"
      },
      {
        "field": "└countryCode",
        "type": "String",
        "required": false,
        "name": "国家编码",
        "description": "US"
      },
      {
        "field": "└contact",
        "type": "JSONObject",
        "required": false,
        "name": "联系方式",
        "description": ""
      },
      {
        "field": "└fullAddress",
        "type": "List",
        "required": false,
        "name": "完整地址",
        "description": ""
      },
      {
        "field": "└└text",
        "type": "String",
        "required": false,
        "name": "描述",
        "description": ""
      },
      {
        "field": "└└languageCode",
        "type": "String",
        "required": false,
        "name": "语言",
        "description": "en"
      },
      {
        "field": "└└imageUrl",
        "type": "String",
        "required": false,
        "name": "图片URL",
        "description": "https://o.sellersprite.com/w/brands/ustm/US502022097612203/ee45f.jpg"
      },
      {
        "field": "└fullName",
        "type": "List",
        "required": false,
        "name": "完整名称",
        "description": ""
      },
      {
        "field": "└└text",
        "type": "String",
        "required": false,
        "name": "描述",
        "description": "ANKER INC"
      },
      {
        "field": "└└languageCode",
        "type": "String",
        "required": false,
        "name": "语言",
        "description": "en"
      },
      {
        "field": "applicationDate",
        "type": "String",
        "required": false,
        "name": "申请日期",
        "description": "2022-09-29"
      },
      {
        "field": "applicationLanguageCode",
        "type": "String",
        "required": false,
        "name": "申请语言",
        "description": "en"
      },
      {
        "field": "applicationNumber",
        "type": "String",
        "required": false,
        "name": "申请编号",
        "description": "97612203"
      },
      {
        "field": "registrationNumber",
        "type": "String",
        "required": false,
        "name": "注册号",
        "description": "4590785"
      },
      {
        "field": "applicationRefNumber",
        "type": "List",
        "required": false,
        "name": "申请参考号",
        "description": ""
      },
      {
        "field": "brandName",
        "type": "List",
        "required": false,
        "name": "品牌名",
        "description": "[ \"1ST AID\"]"
      },
      {
        "field": "collection",
        "type": "String",
        "required": false,
        "name": "数据集",
        "description": "ustm"
      },
      {
        "field": "designatedCountries",
        "type": "List",
        "required": false,
        "name": "指定国家",
        "description": "[\"US\"]"
      },
      {
        "field": "designation",
        "type": "List",
        "required": false,
        "name": "指定国家",
        "description": "[\"US\"]"
      },
      {
        "field": "filingPlace",
        "type": "String",
        "required": false,
        "name": "申请地点",
        "description": ""
      },
      {
        "field": "kind",
        "type": "List",
        "required": false,
        "name": "商标类别",
        "description": "[\"Individual\"]"
      },
      {
        "field": "logos",
        "type": "List",
        "required": false,
        "name": "logo",
        "description": ""
      },
      {
        "field": "└logo",
        "type": "String",
        "required": false,
        "name": "logo",
        "description": ""
      },
      {
        "field": "└logoUrl",
        "type": "String",
        "required": false,
        "name": "logo url",
        "description": ""
      },
      {
        "field": "markFeature",
        "type": "String",
        "required": false,
        "name": "商标种类",
        "description": "Combined"
      },
      {
        "field": "niceClass",
        "type": "List",
        "required": false,
        "name": "尼斯分类",
        "description": "[5]"
      },
      {
        "field": "office",
        "type": "String",
        "required": false,
        "name": "知识产权局",
        "description": "US"
      },
      {
        "field": "status",
        "type": "String",
        "required": false,
        "name": "状态",
        "description": "Pending"
      },
      {
        "field": "statusDate",
        "type": "String",
        "required": false,
        "name": "状态更新日期",
        "description": "2023-05-02"
      },
      {
        "field": "type",
        "type": "String",
        "required": false,
        "name": "类型",
        "description": "TRADEMARK"
      }
    ]
  },
  {
    "operation": "GLOBAL_BRAND_STATS",
    "domain": "trademark",
    "responseShape": "object",
    "requestFields": [
      {
        "field": "office",
        "type": "List",
        "required": true,
        "name": "数据范围，见上一个接口",
        "description": "[\"US\"]"
      },
      {
        "field": "text",
        "type": "String",
        "required": true,
        "name": "查询文本",
        "description": "CHINESE"
      },
      {
        "field": "imageBase64",
        "type": "String",
        "required": false,
        "name": "base64字符串",
        "description": ""
      },
      {
        "field": "imageFile",
        "type": "File",
        "required": false,
        "name": "上传的文件",
        "description": "C:\\fakepath\\人像.jpeg"
      }
    ],
    "responseFields": [
      {
        "field": "office",
        "type": "List",
        "required": false,
        "name": "知识产权局",
        "description": "[{\"key\":\"US\",\"count\":2}]"
      },
      {
        "field": "└key",
        "type": "String",
        "required": false,
        "name": "值",
        "description": "US"
      },
      {
        "field": "└count",
        "type": "Integer",
        "required": false,
        "name": "数量",
        "description": "2"
      },
      {
        "field": "brandName",
        "type": "List",
        "required": false,
        "name": "品牌名",
        "description": "格式同office"
      },
      {
        "field": "└key",
        "type": "String",
        "required": false,
        "name": "值",
        "description": "ADVENTURE CLUB"
      },
      {
        "field": "└count",
        "type": "Integer",
        "required": false,
        "name": "数量",
        "description": "4"
      },
      {
        "field": "status",
        "type": "List",
        "required": false,
        "name": "状态",
        "description": "格式同office"
      },
      {
        "field": "└key",
        "type": "String",
        "required": false,
        "name": "值",
        "description": "Registered"
      },
      {
        "field": "└count",
        "type": "Integer",
        "required": false,
        "name": "数量",
        "description": "12"
      },
      {
        "field": "applicant",
        "type": "List",
        "required": false,
        "name": "申请人",
        "description": "格式同office"
      },
      {
        "field": "└key",
        "type": "String",
        "required": false,
        "name": "值",
        "description": "ANKER INC"
      },
      {
        "field": "└count",
        "type": "Integer",
        "required": false,
        "name": "数量",
        "description": "4"
      },
      {
        "field": "niceClass",
        "type": "List",
        "required": false,
        "name": "尼斯分类",
        "description": "格式同office"
      },
      {
        "field": "└key",
        "type": "String",
        "required": false,
        "name": "值",
        "description": "5"
      },
      {
        "field": "└count",
        "type": "Integer",
        "required": false,
        "name": "数量",
        "description": "2"
      },
      {
        "field": "└label",
        "type": "String",
        "required": false,
        "name": "分类名称",
        "description": "医药用品"
      },
      {
        "field": "applicationYear",
        "type": "List",
        "required": false,
        "name": "申请年份",
        "description": "格式同office"
      },
      {
        "field": "└key",
        "type": "String",
        "required": false,
        "name": "值",
        "description": "1985"
      },
      {
        "field": "└count",
        "type": "Integer",
        "required": false,
        "name": "数量",
        "description": "5"
      },
      {
        "field": "expiryYear",
        "type": "List",
        "required": false,
        "name": "过期年份",
        "description": "格式同office"
      },
      {
        "field": "└key",
        "type": "String",
        "required": false,
        "name": "值",
        "description": "2026"
      },
      {
        "field": "└count",
        "type": "Integer",
        "required": false,
        "name": "数量",
        "description": "2"
      }
    ]
  }
] as const

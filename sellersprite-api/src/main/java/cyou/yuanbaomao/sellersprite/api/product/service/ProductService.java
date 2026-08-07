// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.product.service;

import cyou.yuanbaomao.sellersprite.api.product.model.dto.CompetitorLookupRequest;
import cyou.yuanbaomao.sellersprite.api.product.model.dto.ProductNodeRequest;
import cyou.yuanbaomao.sellersprite.api.product.model.dto.ProductResearchRequest;
import cyou.yuanbaomao.sellersprite.api.product.model.vo.CompetitorLookupVo;
import cyou.yuanbaomao.sellersprite.api.product.model.vo.ProductNodeVo;
import cyou.yuanbaomao.sellersprite.api.product.model.vo.ProductResearchVo;
import java.util.List;

/**
 * SellerSprite 产品分析接口封装。
 */
public interface ProductService {

    /**
     * 查竞品。
     *
     * <p>调用 SellerSprite 官方 POST /v1/product/competitor-lookup，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 查竞品的强类型请求参数
     * @return 查竞品的强类型响应数据
     */
    CompetitorLookupVo lookupCompetitors(CompetitorLookupRequest request);

    /**
     * 选产品。
     *
     * <p>调用 SellerSprite 官方 POST /v1/product/research，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 选产品的强类型请求参数
     * @return 选产品的强类型响应数据
     */
    ProductResearchVo researchProducts(ProductResearchRequest request);

    /**
     * 查产品类目。
     *
     * <p>调用 SellerSprite 官方 GET /v1/product/node，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 查产品类目的强类型请求参数
     * @return 查产品类目的强类型响应数据
     */
    List<ProductNodeVo> listProductNodes(ProductNodeRequest request);

}

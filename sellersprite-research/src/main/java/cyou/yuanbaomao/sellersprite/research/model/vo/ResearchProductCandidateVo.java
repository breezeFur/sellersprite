package cyou.yuanbaomao.sellersprite.research.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResearchProductCandidateVo {

    private Integer rank;
    private String asin;
    private String imageUrl;
    private String title;
    private String brand;
    private String category;
    private String units;
    private String revenue;
    private String price;
    private String rating;
    private String ratings;
}

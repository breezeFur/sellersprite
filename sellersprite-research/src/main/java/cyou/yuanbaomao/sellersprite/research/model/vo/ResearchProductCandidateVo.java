package cyou.yuanbaomao.sellersprite.research.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResearchProductCandidateVo {

    private Integer rank;
    private String asin;
    private String parentAsin;
    private String variations;
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

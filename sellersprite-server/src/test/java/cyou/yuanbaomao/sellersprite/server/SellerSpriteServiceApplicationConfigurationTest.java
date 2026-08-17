package cyou.yuanbaomao.sellersprite.server;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;

class SellerSpriteServiceApplicationConfigurationTest {

    @Test
    void shouldDeclareBusinessMapperScanOnApplicationClass() {
        MapperScan mapperScan = SellerSpriteServiceApplication.class.getAnnotation(MapperScan.class);

        assertThat(mapperScan).isNotNull();
        assertThat(mapperScan.value()).containsExactly("cyou.yuanbaomao.sellersprite.db.mapper");
    }
}

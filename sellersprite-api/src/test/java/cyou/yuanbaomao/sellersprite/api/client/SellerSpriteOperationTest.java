package cyou.yuanbaomao.sellersprite.api.client;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.counting;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class SellerSpriteOperationTest {

    @Test
    void shouldDeclareExactlyFortyFiveUniqueOperationsAcrossNineDomains() {
        List<SellerSpriteOperation> operations = List.of(SellerSpriteOperation.values());

        assertThat(operations).hasSize(45);
        assertThat(operations)
                .extracting(operation -> operation.getMethod() + " " + operation.getPath())
                .doesNotHaveDuplicates();
        assertThat(operations.stream().map(SellerSpriteOperation::getDomain).collect(toSet()))
                .containsExactlyInAnyOrderElementsOf(EnumSet.allOf(SellerSpriteDomain.class));

        Map<SellerSpriteDomain, Long> expectedCounts = new EnumMap<>(SellerSpriteDomain.class);
        expectedCounts.put(SellerSpriteDomain.ACCOUNT, 1L);
        expectedCounts.put(SellerSpriteDomain.PRODUCT, 3L);
        expectedCounts.put(SellerSpriteDomain.ASIN, 7L);
        expectedCounts.put(SellerSpriteDomain.KEYWORD, 9L);
        expectedCounts.put(SellerSpriteDomain.TRAFFIC, 5L);
        expectedCounts.put(SellerSpriteDomain.MARKET, 14L);
        expectedCounts.put(SellerSpriteDomain.REVIEW, 1L);
        expectedCounts.put(SellerSpriteDomain.TRADEMARK, 4L);
        expectedCounts.put(SellerSpriteDomain.TOOL, 1L);

        assertThat(operations.stream().collect(groupingBy(SellerSpriteOperation::getDomain, counting())))
                .containsExactlyInAnyOrderEntriesOf(expectedCounts);
    }
}

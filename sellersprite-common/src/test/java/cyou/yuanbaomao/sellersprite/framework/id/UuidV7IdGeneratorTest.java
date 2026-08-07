package cyou.yuanbaomao.sellersprite.framework.id;

import static org.assertj.core.api.Assertions.assertThat;

import cyou.yuanbaomao.base.id.UuidV7IdGenerator;
import org.junit.jupiter.api.Test;

class UuidV7IdGeneratorTest {

    @Test
    void shouldGenerateUuidV7String() {
        UuidV7IdGenerator generator = new UuidV7IdGenerator();
        String id = generator.nextId();

        assertThat(id).hasSize(36);
        assertThat(id.charAt(14)).isEqualTo('7');
    }
}

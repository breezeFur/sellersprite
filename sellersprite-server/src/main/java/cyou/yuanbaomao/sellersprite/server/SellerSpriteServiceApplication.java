package cyou.yuanbaomao.sellersprite.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("cyou.yuanbaomao.sellersprite.db.mapper")
@SpringBootApplication(scanBasePackages = "cyou.yuanbaomao.sellersprite")
public class SellerSpriteServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SellerSpriteServiceApplication.class, args);
    }
}

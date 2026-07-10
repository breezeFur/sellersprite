package com.yuanbaomao.sellersprite.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.yuanbaomao.sellersprite")
public class SellerSpriteServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SellerSpriteServiceApplication.class, args);
    }
}

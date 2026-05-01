package com.vending;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@MapperScan("com.vending.module.*.mapper")
@EnableScheduling
public class VendingApplication {

    public static void main(String[] args) {
        String hash = new BCryptPasswordEncoder().encode("123456");
        System.out.println("========== Bcrypt Hash for 123456 ==========");
        System.out.println(hash);
        System.out.println("============================================");
        SpringApplication.run(VendingApplication.class, args);
    }
}
package vn.avq.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AvqShopApplication {
    public static void main(String[] args) { SpringApplication.run(AvqShopApplication.class, args); }
}

package com.example.brandbeacon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class BrandbeaconApplication {

    public static void main(String[] args) {
        SpringApplication.run(BrandbeaconApplication.class, args);
    }

}

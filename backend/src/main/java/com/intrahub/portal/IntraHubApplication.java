package com.intrahub.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IntraHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(IntraHubApplication.class, args);
    }
}

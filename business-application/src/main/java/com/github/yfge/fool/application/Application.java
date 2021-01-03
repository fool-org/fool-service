package com.github.yfge.fool.application;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.github.yfge.fool.common", "com.github.yfge.fool.dao", "com.github.yfge.fool.auth", "com.github.yfge.fool.application"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

package com.unble.budget;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UnbleBudgetApplication {
    public static void main(String[] args) {
        SpringApplication.run(UnbleBudgetApplication.class, args);
        System.out.println("🚀 Unble 가계부 서버가 시작되었습니다!");
        System.out.println("📱 접속: http://localhost:9090");
    }
}
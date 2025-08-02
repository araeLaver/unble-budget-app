package com.unble.budget;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration;
import org.springframework.boot.autoconfigure.websocket.reactive.WebSocketReactiveAutoConfiguration;

@SpringBootApplication(exclude = {
    SpringApplicationAdminJmxAutoConfiguration.class,
    JmxAutoConfiguration.class,
    GsonAutoConfiguration.class,
    WebSocketServletAutoConfiguration.class,
    WebSocketReactiveAutoConfiguration.class
})
public class UnbleBudgetApplication {
    public static void main(String[] args) {
        System.setProperty("spring.jmx.enabled", "false");
        SpringApplication app = new SpringApplication(UnbleBudgetApplication.class);
        app.run(args);
        System.out.println("🚀 Unble 가계부 서버가 시작되었습니다!");
        System.out.println("📱 접속: http://localhost:9090");
    }
}
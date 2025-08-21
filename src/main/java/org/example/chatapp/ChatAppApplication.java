package org.example.chatapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

//Tesztek írása
//GITHUB
//Meg kell csinálni az Ecommerce api-t is

@SpringBootApplication
@EnableAsync
@EnableCaching
@EnableRetry
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class ChatAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatAppApplication.class, args);
    }

}

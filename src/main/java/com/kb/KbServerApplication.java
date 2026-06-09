package com.kb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * RAG企业内部知识库问答系统启动类
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class KbServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(KbServerApplication.class, args);
    }

}
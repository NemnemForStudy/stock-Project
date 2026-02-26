package com.stock.stockwatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling; // 추가

@SpringBootApplication
@EnableScheduling // 스케줄링 기능 활성화
@EnableCaching // 이 어노테이션이 반드시 있어야 Redis 캐시가 작동함!
public class StockWatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockWatchApplication.class, args);
    }

}

package com.stock.stockwatch.repository;

import com.stock.stockwatch.entity.TradeEvent; // TradeEvent 엔티티 임포트 경로 수정 (Entity -> entity)
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime; // 추가
import java.util.List; // 추가

@Repository
public interface TradeEventRepository extends JpaRepository<TradeEvent, Long> {
    // DailyTopTradedService에서 특정 기간의 TradeEvent를 조회하기 위한 쿼리 메서드입니다.
    List<TradeEvent> findByTradeTimestampBetween(LocalDateTime start, LocalDateTime end);
}

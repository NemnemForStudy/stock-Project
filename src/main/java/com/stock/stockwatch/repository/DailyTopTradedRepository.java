package com.stock.stockwatch.repository;

import com.stock.stockwatch.entity.DailyTopTraded; // DailyTopTraded 엔티티 임포트 경로 수정 (Entity -> entity)
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyTopTradedRepository extends JpaRepository<DailyTopTraded, Long> {
    List<DailyTopTraded> findByTradeDateOrderByRankAsc(LocalDate tradeDate);
    void deleteByTradeDate(LocalDate tradeDate);
}

package com.stock.stockwatch.krStock.repository;

import com.stock.stockwatch.entity.DomesticStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KrStockRepository extends JpaRepository<DomesticStock, Long> {
    boolean existsByCode(String code);
    // DB에 저장된 "전체 종목 코드"만 리스트로 뽑아오는 쿼리
    @Query("SELECT s.code FROM DomesticStock s")
    List<String> findAllStockCodes();
}

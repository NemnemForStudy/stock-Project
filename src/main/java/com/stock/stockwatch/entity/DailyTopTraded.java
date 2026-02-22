package com.stock.stockwatch.entity; // 패키지 선언 변경 (Entity -> entity)

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "daily_top_traded")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyTopTraded {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate tradeDate; // Top 10이 계산된 날짜

    @Column(nullable = false)
    private String code; // 암호화폐 코드 (예: KRW-BTC)

    @Column(nullable = false)
    private double totalTradedAmount; // 해당 날짜의 총 거래 대금 (KRW) // 필드 이름 수정 (totalTradeAmount -> totalTradedAmount)

    @Column(nullable = false)
    private int rank; // 해당 날짜의 순위 (1위부터 10위까지)

    // Top 10 계산 시 사용될 커스텀 생성자입니다.
    public DailyTopTraded(LocalDate tradeDate, String code, double totalTradedAmount, int rank) { // 생성자 필드 이름 수정
        this.tradeDate = tradeDate;
        this.code = code;
        this.totalTradedAmount = totalTradedAmount;
        this.rank = rank;
    }
}

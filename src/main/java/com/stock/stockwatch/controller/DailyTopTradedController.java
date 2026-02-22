package com.stock.stockwatch.controller;

import com.stock.stockwatch.entity.DailyTopTraded;
import com.stock.stockwatch.repository.DailyTopTradedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000") // 리액트 주소 허용
@RestController
@RequestMapping("/api")
public class DailyTopTradedController {
    // DailyTopTreaded 엔티티에 대한 데이터 접근 위한 레포
    // Spring이 자동으로 이 의존성 주입
    @Autowired
    private DailyTopTradedRepository dailyTopTradedRepository;

    // 생성자 주입 방식 (시니어급 관례)
    public DailyTopTradedController(DailyTopTradedRepository dailyTopTradedRepository) {
        this.dailyTopTradedRepository = dailyTopTradedRepository;
    }

    /**
     * 특정 날짜의 Top 10 거래 데이터를 조회하는 API 엔드포인트입니다.
     *
     * @param date 조회할 날짜 (선택 사항). "yyyy-MM-dd" 형식.
     *             제공되지 않으면 어제 날짜의 Top 10을 반환합니다.
     * @return 해당 날짜의 DailyTopTraded 목록 (순위 오름차순).
     */
    @GetMapping("/daily-top-traded") // get 요청 처리하며, 기본경로에 매핑됨.
    public List<Map<String, Object>> getDailyTopTraded(@RequestParam(required = false) String date) {
        System.out.println("조회 요청 날짜: " + date);

        List<Map<String, Object>> mockList = new ArrayList<>();

        // 가짜 데이터 1번: 비트코인
        Map<String, Object> btc = new HashMap<>();
        btc.put("id", 1);
        btc.put("rank", 1);
        btc.put("code", "KRW-BTC");
        btc.put("totalTradedAmount", 150000000000.0); // 1500억
        mockList.add(btc);

        // 가짜 데이터 2번: 이더리움
        Map<String, Object> eth = new HashMap<>();
        eth.put("id", 2);
        eth.put("rank", 2);
        eth.put("code", "KRW-ETH");
        eth.put("totalTradedAmount", 80000000000.0); // 800억
        mockList.add(eth);

        return mockList;
    }
}

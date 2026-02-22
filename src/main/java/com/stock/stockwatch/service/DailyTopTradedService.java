package com.stock.stockwatch.service; // 패키지 선언 변경

import com.stock.stockwatch.entity.DailyTopTraded; // 엔티티 임포트 경로 변경
import com.stock.stockwatch.entity.TradeEvent; // 엔티티 임포트 경로 변경
import com.stock.stockwatch.repository.DailyTopTradedRepository; // 리포지토리 임포트 경로 변경
import com.stock.stockwatch.repository.TradeEventRepository; // 리포지토리 임포트 경로 변경
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DailyTopTradedService {
    @Autowired
    private TradeEventRepository tradeEventRepository;

    @Autowired
    private DailyTopTradedRepository dailyTopTradedRepository;

    // 매일 자정에 실행되도록 스케줄링.
    // cron 표현식: 초 분 시 일 월 요일
    // "0 0 0 * * *"는 매일 0시 0분 0초를 의미함.
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void calculateAndSaveDailyTopTraded() {
        LocalDate yesterday = LocalDate.now().minusDays(1); // 어제 날짜 가져옴
        LocalDateTime startOfDay = yesterday.atStartOfDay(); // 어제의 시작 시간 (00:00:00)
        LocalDateTime endOfDay = yesterday.atTime(LocalTime.MAX); // 어제의 마지막 시간 (23:59:59.999999999)

        System.out.println("Calculating daily top traded for: " + yesterday);

        // 전날의 모든 TradeEvent 데이터를 조회함.
        List<TradeEvent> tradeEvents = tradeEventRepository.findByTradeTimestampBetween(startOfDay, endOfDay);

        // 코인 코드별로 총 거래 대금 집계
        Map<String, Double> totalAmountsByCode = tradeEvents.stream()
                .collect(Collectors.groupingBy(TradeEvent::getCode,
                        Collectors.summingDouble(TradeEvent::getTotalKrw)
                ));

        // 집계된 데이터를 총 거래 대금 기준으로 내림차순 정렬하고 Top 10 추출
        List<DailyTopTraded> topTradedList = totalAmountsByCode.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())) // 총 거래대금 내림차순 정렬
                .limit(10)
                .map(entry -> new DailyTopTraded(yesterday, entry.getKey(), entry.getValue(), 0)) // 초기 순위는 0으로 설정
                .collect(Collectors.toList());

        // 순위 매기고 DB에 저장함.
        // 기존 해당 날짜의 Top  10 데이터가 있다면 삭제하고 새로 지정.
        dailyTopTradedRepository.deleteByTradeDate(yesterday);
        int rank = 1;
        for(DailyTopTraded topTraded : topTradedList) {
            topTraded.setRank(rank++); // 순위 설정
            dailyTopTradedRepository.save(topTraded); // db 저장
        }

        System.out.println("Daily top traded calculation completed for: " + yesterday + ". Saved " + topTradedList.size() + " entries.");
    }
}

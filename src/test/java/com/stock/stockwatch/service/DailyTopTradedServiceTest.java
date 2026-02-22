package com.stock.stockwatch.service;

import com.stock.stockwatch.entity.DailyTopTraded;
import com.stock.stockwatch.entity.TradeEvent;
import com.stock.stockwatch.repository.DailyTopTradedRepository;
import com.stock.stockwatch.repository.TradeEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Mockito 사용해 Mock 객체 주입받기 위한 설정
public class DailyTopTradedServiceTest {
    @Mock
    private TradeEventRepository tradeEventRepository;

    @Mock
    private DailyTopTradedRepository dailyTopTradedRepository;

    @InjectMocks // Mock 객체들을 DailyTopTradedService 인스턴스에 주입
    private DailyTopTradedService dailyTopTradedService;

    private LocalDate yesterday;
    private LocalDateTime startOfDay;
    private LocalDateTime endOfDay;

    @BeforeEach // 각 테스트 메서드 실행 전 초기화
    void setUp() {
        yesterday = LocalDate.now().minusDays(1);
        startOfDay = yesterday.atStartOfDay();
        endOfDay = yesterday.atTime(LocalTime.MAX);
    }

    @Test
    void calculateAndSaveDailyTraded_shouldCalculateAndSaveTop10() {
        // Given: Mock TradeEvent 데이터 설정
        // 어제 날짜의 거래 이벤트들을 생성합니다.
        List<TradeEvent> mockTradeEvents = Arrays.asList(
                new TradeEvent("KRW-BTC", 100000000.0, 10.0, 1000000000.0, yesterday.atTime(10, 0).atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()),
                new TradeEvent("KRW-ETH", 5000000.0, 50.0, 250000000.0, yesterday.atTime(10, 1).atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()),
                new TradeEvent("KRW-BTC", 100000000.0, 5.0, 500000000.0, yesterday.atTime(10, 2).atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()),
                new TradeEvent("KRW-XRP", 500.0, 1000000.0, 500000000.0, yesterday.atTime(10, 3).atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()),
                new TradeEvent("KRW-ETH", 5000000.0, 20.0, 100000000.0, yesterday.atTime(10, 4).atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()),
                new TradeEvent("KRW-ADA", 1000.0, 300000.0, 300000000.0, yesterday.atTime(10, 5).atZone(ZoneId.of("UTC")).toInstant().toEpochMilli())
        );

        when(tradeEventRepository.findByTradeTimestampBetween(startOfDay, endOfDay))
                .thenReturn(mockTradeEvents);

        // dailyTopTradedRepository.save 호출 시 어떤 DailyTopTraded 객체가 저장되는지 캡처
        ArgumentCaptor<DailyTopTraded> captor = ArgumentCaptor.forClass(DailyTopTraded.class);

        dailyTopTradedService.calculateAndSaveDailyTopTraded();

        // Then:
        // tradeEventRepository.findByTradeTimestampBetween이 올바른 인자로 호출되었는지 검증
        verify(tradeEventRepository, times(1)).findByTradeTimestampBetween(startOfDay, endOfDay);

        // dailyTopTradedRepository.deleteByTradeDate가 올바른 인자로 호출되었는지 검증
        verify(dailyTopTradedRepository, times(1)).deleteByTradeDate(yesterday);

        // dailyTopTradedRepository.save가 캡처된 DailyTopTraded 객체들로 3번 호출되었는지 검증 (BTC, ETH, XRP, ADA)
        // 실제로는 Top 10에 해당하는 모든 고유 코인에 대해 호출됩니다.
        // 여기서는 예시 데이터에 따라 4개의 고유 코인이 있으므로 4번 호출될 것입니다.
        verify(dailyTopTradedRepository, times(4)).save(captor.capture());

        // 저장된 DailyTopTraded 객체들의 내용 검증
        List<DailyTopTraded> savedTopTradedList = captor.getAllValues();
        assertEquals(4, savedTopTradedList.size()); // 예시 데이터에 따른 고유 코인 수

        // 정렬 후 예상 순서: BTC, XRP, ETH, ADA
        assertEquals("KRW-BTC", savedTopTradedList.get(0).getCode());
        assertEquals(1, savedTopTradedList.get(0).getRank());
        assertEquals(1500000000.0, savedTopTradedList.get(0).getTotalTradedAmount());

        assertEquals("KRW-XRP", savedTopTradedList.get(1).getCode());
        assertEquals(2, savedTopTradedList.get(1).getRank());
        assertEquals(500000000.0, savedTopTradedList.get(1).getTotalTradedAmount());

        assertEquals("KRW-ETH", savedTopTradedList.get(2).getCode());
        assertEquals(3, savedTopTradedList.get(2).getRank());
        assertEquals(350000000.0, savedTopTradedList.get(2).getTotalTradedAmount());

        assertEquals("KRW-ADA", savedTopTradedList.get(3).getCode());
        assertEquals(4, savedTopTradedList.get(3).getRank());
        assertEquals(300000000.0, savedTopTradedList.get(3).getTotalTradedAmount());
    }
}

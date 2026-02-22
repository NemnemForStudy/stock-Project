package com.stock.stockwatch.controller;

import com.stock.stockwatch.entity.DailyTopTraded;
import com.stock.stockwatch.repository.DailyTopTradedRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DailyTopTradedController.class) // DailyTopTradedController만 테스트하도록 설정
public class DailyTopTradedControllerTest {
    @Autowired
    private MockMvc mockMvc; // 컨트롤러 HTTP 요청을 시뮬레이션하는 데 사용

    // Mock 객체를 Bean으로 등록
    @TestConfiguration
    static class TestConfig {
        @Bean
        public DailyTopTradedRepository dailyTopTradedRepository() {
            return mock(DailyTopTradedRepository.class); // Mockito.mock 사용해 Mock 객체 생성.
        }
    }

    @Autowired // TestConfig에서 생성된 Mock Bean을 주입받음
    private DailyTopTradedRepository dailyTopTradedRepository;

    @Test
    void getDailyTopTraded_shouldReturnYesterdayTop10_whenNoDateProvided() throws Exception {
        // Given : 어제 날짜와 Mock 데이터 설정
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<DailyTopTraded> mockTop10 = Arrays.asList(
                new DailyTopTraded(yesterday, "KRW-BTC", 1000000000.0, 1), // id 필드 제거, totalTradedAmount 사용
                new DailyTopTraded(yesterday, "KRW-ETH", 500000000.0, 2)
        );

        when(dailyTopTradedRepository.findByTradeDateOrderByRankAsc(any(LocalDate.class)))
                .thenReturn(mockTop10);

        // When & Then : Http GET 요청 수행하고 응답 검증
        mockMvc.perform(get("/api/daily-top-traded"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].code").value("KRW-BTC"))
                .andExpect(jsonPath("$[1].code").value("KRW-ETH"))
                .andExpect(jsonPath("$.length()").value(2)); // 반환된 리스트 크기 검증

    }

    @Test
    void getDailyTopTraded_shouldReturnSpecificDateTop10_whenDateProvided() throws Exception {
        // 특정 날짜의 Top 10 데이터 Mock 설정
        LocalDate specificDate = LocalDate.of(2023, 1, 15);
        List<DailyTopTraded> mockTop10 = Arrays.asList(
                new DailyTopTraded(specificDate, "KRW-XRP", 800000000.0, 1), // id 필드 제거, totalTradedAmount 사용
                new DailyTopTraded(specificDate, "KRW-ADA", 400000000.0, 2)
        );

        when(dailyTopTradedRepository.findByTradeDateOrderByRankAsc(specificDate))
                .thenReturn(mockTop10);

        mockMvc.perform(get("/api/daily-top-traded").param("date", "2023-01-15"))
                .andDo(print()) // 요청 및 응답 내용 출력
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // Content-Type 검증
                .andExpect(jsonPath("$[0].code").value("KRW-XRP"))
                .andExpect(jsonPath("$[1].code").value("KRW-ADA"))
                .andExpect(jsonPath("$.length()").value(2)); // 배열 크기 검증
    }
}

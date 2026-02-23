package com.stock.stockwatch.exchangeRate.service;

import com.stock.stockwatch.common.auth.KisAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {
    private final KisAuthService authService;
    private final RestTemplate restTemplate = new RestTemplate();
    private List<Map<String, Object>> cachedRates = null;
    private LocalDateTime lastFetched = LocalDateTime.MIN;

    @Value("${stock_app_key}")
    private String appKey;
    @Value("${stock_app_secret}")
    private String appSecret;

    public synchronized List<Map<String, Object>> getAllExchangeRates() {
        if (cachedRates != null && lastFetched.isAfter(LocalDateTime.now().minusMinutes(10))) {
            return cachedRates;
        }

        // 대시보드에 필요한 환율 코드 리스트 (FX@ 접두어 필수)
        String[] targetSymbols = {"FX@USDKRW", "FX@EURKRW", "FX@JPYKRW", "FX@CNYKRW", "FX@GBPKRW"};
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (String symbol : targetSymbols) {
            Map<String, Object> rateData = fetchSingleExchangeRate(symbol);
            if (!rateData.isEmpty()) {
                resultList.add(rateData);
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return resultList;
    }

    private Map fetchSingleExchangeRate(String symbol) {
        // 주요 환율 코드 리스트
        String url = "https://openapi.koreainvestment.com:9443/uapi/overseas-price/v1/quotations/inquire-daily-chartprice";

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String startDate = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", authService.getAccessToken());
        headers.set("appkey", appKey);
        headers.set("appsecret", appSecret);
        headers.set("tr_id", "FHKST03030100");
        headers.set("custtype", "P");

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("FID_COND_MRKT_DIV_CODE", "X") // X: 환율
                .queryParam("FID_INPUT_ISCD", symbol)     // FX@USDKRW (그대로 전달)
                .queryParam("FID_INPUT_DATE_1", startDate)
                .queryParam("FID_INPUT_DATE_2", today)
                .queryParam("FID_PERIOD_DIV_CODE", "D");   // D: 일별

        System.out.println(builder);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    builder.toUriString(), HttpMethod.GET, entity, Map.class
            );

            System.out.println("Response Body: " + response.getBody());
            System.out.println("====== [KIS API Request End] ======");

            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }
}

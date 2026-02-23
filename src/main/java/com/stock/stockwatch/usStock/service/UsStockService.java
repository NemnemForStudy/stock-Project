package com.stock.stockwatch.usStock.service;

import com.stock.stockwatch.common.AssetDto;
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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UsStockService {
    private final KisAuthService authService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${stock_app_key}")
    private String appKey;
    @Value("${stock_app_secret}")
    private String appSecret;

    public Map<String, List<AssetDto>> getUsStockTop10() {
        // 1. 뉴욕 시간 및 시장 상태 로그 (참고용)
        ZonedDateTime nyTime = ZonedDateTime.now(ZoneId.of("America/New_York"));
        int hour = nyTime.getHour();
        int minute = nyTime.getMinute();
        boolean isRegular = (hour > 9 || (hour == 9 && minute >= 30)) && hour < 16;

        // 2. 정확한 미국주식 상승률/하락률 API URL
        String url = "https://openapi.koreainvestment.com:9443/uapi/overseas-stock/v1/ranking/updown-rate";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("authorization", authService.getAccessToken());
        headers.set("appkey", appKey);
        headers.set("appsecret", appSecret);
        headers.set("tr_id", "HHDFS76290000"); // 🚩 명세서의 정확한 TR_ID로 수정
        headers.set("custtype", "P");

        // 3. 파라미터 설정 (나스닥 기준)
        // builder 부분을 이렇게 4개의 파라미터가 다 들어가게 수정하세요.
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("AUTH", "")
                .queryParam("EXCD", "NAS")
                .queryParam("GUBN", "0")  // 0: 상승률순, 1: 하락률순
                .queryParam("NDAY", "1")  // [NDAY] 에러 해결: 1일 기준(당일)
                .queryParam("VOL_RANG", "0")
                .queryParam("KEYB", "")
                .queryParam("QUES", "");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    builder.toUriString(), HttpMethod.GET, entity, Map.class
            );

            // 4. 데이터 키값 'output2'로 변경
            if (response.getBody() == null || response.getBody().get("output2") == null) {
                return Collections.emptyMap();
            }

            List<Map<String, Object>> output2 = (List<Map<String, Object>>) response.getBody().get("output2");

            List<AssetDto> all = output2.stream().map(data -> {
                        try {
                            double rate = Double.parseDouble(String.valueOf(data.get("rate")));

                            return new AssetDto(
                                    String.valueOf(data.get("symb")),           // 종목코드
                                    String.valueOf(data.get("name")),           // 종목명
                                    Double.parseDouble(String.valueOf(data.get("last"))), // 현재가
                                    rate / 100,                                 // 등락률
                                    (rate > 0 ? "RISE" : (rate < 0 ? "FALL" : "EVEN")), // 상태
                                    Double.parseDouble(String.valueOf(data.get("tvol")))  // 거래량 필드 tvol로 수정
                            );
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();

            // 상위 10개씩 분리하여 반환
            return AssetDto.createResponse(
                    all.stream().filter(a -> !"FALL".equals(a.changeType())).limit(10).toList(),
                    all.stream().filter(a -> "FALL".equals(a.changeType())).limit(10).toList()
            );

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }
}
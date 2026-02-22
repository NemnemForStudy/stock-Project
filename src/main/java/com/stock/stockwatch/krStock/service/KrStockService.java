package com.stock.stockwatch.krStock.service;

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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class KrStockService {
    private final KisAuthService authService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${stock_app_key}")
    private String appKey;
    @Value("${stock_app_secret}")
    private String appSecret;

    public Map<String, List<AssetDto>> getKrStockTop10() {
        String url = "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/quotations/volume-rank";

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("authorization", authService.getAccessToken());
        headers.set("appkey", appKey);
        headers.set("appsecret", appSecret);
        headers.set("tr_id", "FHPST01710000"); // 국내주식 거래량 순위 TR ID TOP 10
        headers.set("custtype", "P"); // 개인

        // 쿼리 파라미터 설정
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("FID_COND_MRKT_DIV_CODE", "J") // 주식
                .queryParam("FID_COND_SCR_DIV_CODE", "20171") // 화면번호
                .queryParam("FID_INPUT_ISCD", "0000") // 전체
                .queryParam("FID_DIV_CLS_CODE", "0")
                .queryParam("FID_BLNG_CLS_CODE", "0")
                .queryParam("FID_TRGT_CLS_CODE", "0")
                .queryParam("FID_TRGT_EXLS_CLS_CODE", "0")
                .queryParam("FID_INPUT_PRICE_1", "")
                .queryParam("FID_INPUT_PRICE_2", "")
                .queryParam("FID_VOL_CNT", "");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    builder.toUriString(), HttpMethod.GET, entity, Map.class
            );

            // 응답 데이터 검증
            if (response.getBody() == null || response.getBody().get("output") == null) {
                return Collections.emptyMap();
            }

            List<Map<String, Object>> output = (List<Map<String, Object>>) response.getBody().get("output");

            List<AssetDto> all = output.stream().map(data -> {
                try {
                    String sign = String.valueOf(data.get("prdy_vrss_sign"));
                    String changeType = convertSignToChangeType(sign);
                    ;

                    return new AssetDto(
                            String.valueOf(data.get("mksc_shrn_iscd")), // 종목코드
                            String.valueOf(data.get("hts_kor_isnm")),  // 종목명
                            Double.parseDouble(String.valueOf(data.get("stck_prpr"))), // 현재가
                            Double.parseDouble(String.valueOf(data.get("prdy_ctrt"))) / 100, // 등락률
                            changeType, // 🚩 결정된 상태값
                            Double.parseDouble(String.valueOf(data.get("acml_vol"))) // 누적거래량
                    );
                } catch (Exception e) {
                    System.err.println("파싱 에러 종목: " + data.get("hts_kor_isnm"));
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .toList();

            return AssetDto.createResponse(
                    all.stream().filter(a -> !"FALL".equals(a.changeType())).limit(10).toList(),
                    all.stream().filter(a -> "FALL".equals(a.changeType())).limit(10).toList()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    /**
     * KIS 등락 부호 코드를 시스템 공통 changeType으로 변환
     * 1: 상한, 2: 상승, 3: 보합, 4: 하한, 5: 하락
     */
    private String convertSignToChangeType(String sign) {
        return switch (sign) {
            case "1", "2" -> "RISE";
            case "4", "5" -> "FALL";
            default -> "EVEN";
        };
    }
}

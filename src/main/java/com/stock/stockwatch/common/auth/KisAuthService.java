package com.stock.stockwatch.common.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class KisAuthService {
    @Value("${stock_app_key}")
    private String appKey;

    @Value("${stock_app_secret}")
    private String appSecret;

    private String accessToken;

    /**
     * KIS 토큰은 유효기간이 길기 때문에(24시간), 한 번 받아두면 계속 쓸 수 있습니다.
     * accessToken이 null일 때만 새로 받아오는 'Lazy Loading' 방식을 씁니다.
     */

    public String getAccessToken() {
        if(this.accessToken == null) {
            fetchNewToken();
        }
        return "Bearer " + this.accessToken;
    }

    private void fetchNewToken() {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://openapi.koreainvestment.com:9443/oauth2/tokenP";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 요청 바디 구성
        Map<String, String> body = Map.of(
            "grant_type", "client_credentials",
            "appkey", appKey,
            "appsecret", appSecret
        );

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Map.class
            );
            Map<String, Object> result = response.getBody();
            if (result != null && result.containsKey("access_token")) {
                this.accessToken = result.get("access_token").toString();
                System.out.println("토큰 발급 성공");
            } else {
                System.out.println("토큰 없음 - 응답 확인 필요");
            }
        } catch (Exception e) {
            throw new RuntimeException("KIS 토큰 발급 실패 : " + e.getMessage());
        }
    }
}

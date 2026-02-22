package com.stock.stockwatch.common.auth;

import org.springframework.beans.factory.annotation.Value;
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

        // 요청 바디 구성
        Map<String, String> body = Map.of(
            "grant_type", "client_credentials",
            "appkey", appKey,
            "appsecret", appSecret
        );

        try {
            Map<String, Object> response = restTemplate.postForObject(url, body, Map.class);
            if(response != null && response.containsKey("access_token")) {
                this.accessToken = response.get("access_token").toString();
            }
        } catch (Exception e) {
            throw new RuntimeException("KIS 토큰 발급 실패 : " + e.getMessage());
        }
    }
}

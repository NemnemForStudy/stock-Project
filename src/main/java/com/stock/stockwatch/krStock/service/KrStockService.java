package com.stock.stockwatch.krStock.service;

import com.stock.stockwatch.common.AssetDto;
import com.stock.stockwatch.common.auth.KisAuthService;
import com.stock.stockwatch.entity.DomesticStock;
import com.stock.stockwatch.krStock.repository.KrStockRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class KrStockService {
    private final KisAuthService authService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final KrStockRepository krStockRepository;

    @Value("${stock_app_key}")
    private String appKey;
    @Value("${stock_app_secret}")
    private String appSecret;

    public Map<String, List<AssetDto>> getKrStockTop10() {
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Seoul"));
        String trId;
        String scrDivCode;

        if (now.getHour() == 8) {
            trId = "VHPST01710000";    // 넥스트레이드(ATS) 거래량 순위 TR ID
            scrDivCode = "20171";      // 넥장용 화면번호 (매뉴얼 확인 필요)
            System.out.println("--- 현재 넥스트레이드(ATS) 데이터를 가져옵니다 ---");
        } else {
            trId = "FHPST01710000";    // 본장(KRX) 거래량 순위 TR ID
            scrDivCode = "20171";
            System.out.println("--- 현재 한국거래소(KRX) 본장 데이터를 가져옵니다 ---");
        }

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
                .queryParam("FID_COND_SCR_DIV_CODE", scrDivCode) // 화면번호
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

    @Scheduled(fixedRate = 3600000)
    @PostConstruct // 서버 켜질 때 딱 한 번 즉시 실행
    @Transactional // 데이터 한꺼번에 넣어야 하니 트랜잭션 필수
    public void syncDomesticStocks() {
        log.info("국내 주식 종목 마스터 동기화 시작...");

        Map<String, List<AssetDto>> top10Data = getKrStockTop10();
        top10Data.values().forEach(assetList -> {
            for(AssetDto dto : assetList) {
                // 이미 있는 종목인지 체크하고 없으면 저장
                if(!krStockRepository.existsByCode(dto.code())) {
                    DomesticStock newStock = new DomesticStock(
                            dto.code(),
                            dto.name(),
                            "J" // 코스피로 설정
                    );
                    krStockRepository.save(newStock);
                    log.info("신규 종목 등록: {} ({})", dto.name(), dto.code());

                }
            }
        });

        log.info(">>>> 국내 주식 종목 마스터 동기화 완료!");
    }
}

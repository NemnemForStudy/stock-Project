package com.stock.stockwatch.upbit.service;

/*
* REST API를 호출해 상위 10개 코인 리스트를 뽑아주는 역할
* */

import com.stock.stockwatch.common.AssetDto;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service // 비즈니스 로직을 담당하는 서비스 객체임을 스프링에 알림.
public class UpbitMarketService {
    // RestTemplate : 스프링에서 제공하는 HTTP 통신 도구임.
    // 브라우저 주소창을 치는 것처럼, 자바가 API에 접속하게 해줌.
    private final RestTemplate restTemplate;

    // 이렇게 RestTemplateBuilder를 주입받아 빌드해야 @RestClientTest가 인식함.
    public UpbitMarketService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    /**
     *업비트에서 거래대금이 가장 많은 상위 10개 코인의 코드를 가져옴.
     * */
    public Map<String, List<AssetDto>> getTop10MarketCodes() {
        // 1. 업비트 모든 마켓 종류 가져오기
        String marketUrl = "https://api.upbit.com/v1/market/all";

        // restTemplate.getForObject: 해당 URL에 GET 요청을 보내고 결과를 List로 가져옴.
        List<Map<String, String>> allMarkets = restTemplate.getForObject(marketUrl, List.class);

        if (allMarkets == null) {
            return Collections.emptyMap();
        }

        // Java Stream API : 리스트 데이터를 필터링하거나 가공할 때 쓰는 강력한 도구.
        // stream()을 쓰면 필터링 -> 변환 -> 합쳐라 라는 것을 한 줄로 쓸 수 있어서 가독성이 좋아짐/
        Map<String, String> nameMap = allMarkets.stream()
                .filter(m -> m.get("market").startsWith("KRW-"))
                // 업비트 상세조회 API가 이런 콤마 형식을 원해서 이렇게 씀.
                .collect(Collectors.toMap(
                        m -> m.get("market"),
                        m -> m.get("korean_name")
                ));

        // 3. 티커 조회를 위한 코드 리스트
        // keySet() -> 맵이 가지고 있는 모든 키들만 모아서 보여달라는 뜻임.
        String krwMarketCodes = String.join(",", nameMap.keySet());

        // 4. 상세 정보(거래대금) 조회
        // tickerUrl 주소로 JSON을 응답함.(이 코인의 현재 상태는 이 상태이다! 라는 값을 보내줌)
        String tickerUrl = "https://api.upbit.com/v1/ticker?markets=" + krwMarketCodes;
        List<Map<String, Object>> tickerList = restTemplate.getForObject(tickerUrl, List.class);

        // 전체 데이터를 DTO리스트로 변환
        List<AssetDto> allAssets = tickerList.stream()
                .map(t -> new AssetDto(
                        t.get("market").toString().split("-")[1],
                        nameMap.get(t.get("market").toString()),
                        Double.valueOf(t.get("trade_price").toString()),
                        Double.valueOf(t.get("signed_change_rate").toString()),
                        t.get("change").toString(),
                        Double.valueOf(t.get("acc_trade_price_24h").toString())
                )).toList();

        return AssetDto.createResponse(
                filterAndSort(allAssets, true),
                filterAndSort(allAssets, false)
        );
    }

    private List<AssetDto> filterAndSort(List<AssetDto> list, boolean isBuy) {
        return list.stream()
                .filter(a -> isBuy ? !a.changeType().equals("FALL") : a.changeType().equals("FALL"))
                .sorted((a, b) -> b.volume().compareTo(a.volume()))
                .limit(10)
                .toList();
    }

}

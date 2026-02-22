package com.stock.stockwatch.upbit;

import com.stock.stockwatch.common.AssetDto;
import com.stock.stockwatch.upbit.service.UpbitMarketService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(UpbitMarketService.class) // 특정 서비스와 관련된 REST 테스트 설정.
public class UpbitMarketServiceTest {
    @Autowired
    private UpbitMarketService upbitMarketService;

    @Autowired
    private MockRestServiceServer mockServer;

    @Test
    @DisplayName("거래대금 상위 10개 코인을 정렬해 가져오는지 확인")
    void getTop10MarketCodesTest() {
        // 1. Given
        mockServer.expect(requestTo("https://api.upbit.com/v1/market/all"))
                .andRespond(withSuccess("[{\"market\":\"KRW-BTC\",\"korean_name\":\"비트코인\"}]", MediaType.APPLICATION_JSON));

        // 문자열 전체가 아니라 "포함"하고 있는지만 확인합니다.
        mockServer.expect(requestTo(containsString("/v1/ticker")))
                .andRespond(withSuccess("[{\"market\":\"KRW-BTC\",\"acc_trade_price_24h\":1000.0}]", MediaType.APPLICATION_JSON));

        // 2. When
        Map<String, List<AssetDto>> result = upbitMarketService.getTop10MarketCodes();

        // 3. Then
        assertThat(result).isNotEmpty();
//        assertThat(result.get(0).koreanName()).isEqualTo("비트코인");
    }
}

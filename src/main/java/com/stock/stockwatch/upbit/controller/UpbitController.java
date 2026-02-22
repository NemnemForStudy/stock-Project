package com.stock.stockwatch.upbit.controller;

import com.stock.stockwatch.common.AssetDto;
import com.stock.stockwatch.upbit.service.UpbitMarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * [Upbit API 컨트롤러]
 * 프론트엔드(React)에서 보내는 HTTP 요청을 받아 Upbit 데이터를 돌려주는 창구 역할을 합니다.
 */
@RestController
@RequestMapping("/api/upbit")
@RequiredArgsConstructor // final로 선언된 필드를 자동으로 연결
@CrossOrigin(origins = "http://localhost:3000")
public class UpbitController {
    private final UpbitMarketService upbitMarketService;

    /**
     * [상위 10개 코인 정보 조회]
     * GET http://localhost:8080/api/upbit/top10 주소로 요청하면 호출됩니다.
     */
    @GetMapping("/top10") // 가져올 때 GET을 사용.
    public Map<String, List<AssetDto>> getTop10Markets() {
        return upbitMarketService.getTop10MarketCodes();
    }
}

package com.stock.stockwatch.common;

import java.util.List;
import java.util.Map;

/**
 * [왜 이렇게 만드나요?]
 * 1. UI 일관성: 프론트엔드의 AssetTable 컴포넌트는 자산 종류와 상관없이 같은 형태를 기대합니다.
 * 2. 확장성: 나중에 금, 은, 부동산 데이터를 추가해도 이 DTO 하나로 대응 가능합니다.
 * 3. 코드 재사용: 프론트에서 데이터 가공 로직을 하나로 통일할 수 있습니다.
 */
public record AssetDto (
    String code,
    String name,
    Double price,
    Double changeRate,
    String changeType,
    Double volume
) {
    // 상승 하락 리스트 한번에 담아 보내기
    public static Map<String, List<AssetDto>> createResponse(List<AssetDto> buy, List<AssetDto> sell) {
        return Map.of("buy", buy, "sell", sell);
    }
}

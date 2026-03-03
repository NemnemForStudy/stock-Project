package com.stock.stockwatch.krStock.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock.stockwatch.entity.TradeEvent;
import com.stock.stockwatch.repository.TradeEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Kafka에서 데이터를 꺼내 진짜 DB에 저장하는 일꾼을 만듦.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KrTradeEventConsumerService {
    private final TradeEventRepository tradeEventRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Kafka에서 메시지 가져오는 리스너
    @KafkaListener(topics = "upbit-trades", groupId = "stock-group", concurrency = "3") // 파티션 하나씩 맡게.
    public void consumeBatch(List<String> messages) {
        log.info("[스레드: {}] {}건의 데이터 처리 중", Thread.currentThread().getName(), messages.size());
        List<TradeEvent> events = messages.stream()
                .map(this::convertToEntity)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!events.isEmpty()) {
            tradeEventRepository.saveAll(events);
            log.info(">>>> {}건의 데이터를 한 번에 저장했습니다.", events.size());
        }
    }

    private TradeEvent convertToEntity(String message) {
        try {
            JsonNode rootNode = objectMapper.readTree(message);

            String code = rootNode.path("code").asText();
            double tradePrice = rootNode.path("trade_price").asDouble();
            double tradeVolume = rootNode.path("trade_volume").asDouble();
            long timestamp = rootNode.path("trade_timestamp").asLong();
            double totalKrw = tradePrice * tradeVolume;

            // Entity 생성 (기존 생성자 활용)
            return new TradeEvent(code, tradePrice, tradeVolume, totalKrw, timestamp);

        } catch (Exception e) {
            log.error("메시지 변환 에러: {}", e.getMessage());
            // 에러 발생 시 null을 리턴하거나 예외를 던집니다.
            // stream에서 처리하기 위해 null을 리턴하고 나중에 필터링하는 것이 안전합니다.
            return null;
        }
    }
}

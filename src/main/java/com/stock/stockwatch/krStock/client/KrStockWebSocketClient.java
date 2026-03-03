package com.stock.stockwatch.krStock.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock.stockwatch.common.auth.KisAuthService;
import com.stock.stockwatch.krStock.repository.KrStockRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class KrStockWebSocketClient {
    private static final String KIS_WEBSOCKET_URI = "ws://ops.koreainvestment.com:21000";
    private static final String KAFKA_TOPIC = "korea-stock-trades"; // 국내주식 분리

    private WebSocketClient webSocketClient;
    private final KisAuthService authService; // approval_key 획득용
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private ExecutorService virtualThreadExecutor;
    private KrStockRepository krStockRepository;

    // 중요: 휘발성 플래그 (메모리 가시성 보장)
    private volatile boolean isRunning = true;

    @PostConstruct
    public void init() {
        this.isRunning = true; // 시작 시 true
        virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
        connectWebSocket();
    }

    // 🚩 중복된 stop()은 지우고 destroy() 하나로 통합합니다.
    @PreDestroy
    public void destroy() {
        System.out.println("Shutting down UpbitWebSocketClient...");

        // 1. 플래그를 먼저 꺼서 새로운 메시지 처리를 차단합니다.
        this.isRunning = false;

        // 2. 웹소켓 연결 종료
        if (webSocketClient != null) {
            webSocketClient.close();
        }

        // 3. 실행 중인 가상 스레드들 정리
        if (virtualThreadExecutor != null) {
            virtualThreadExecutor.shutdown();
            try {
                // 3초간 대기 후 강제 종료
                if (!virtualThreadExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
                    virtualThreadExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                virtualThreadExecutor.shutdownNow();
            }
        }
        System.out.println("UpbitWebSocketClient stopped safely.");
    }

    private void connectWebSocket() {
        try {
            webSocketClient = new WebSocketClient(new URI(KIS_WEBSOCKET_URI)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    log.info("Connected to KIS Stock WebSocket");
                    subscribeToStocks();
                }

                @Override
                public void onMessage(String message) {
                    if (isRunning) {
                        virtualThreadExecutor.submit(() -> processMessage(message));
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    if (isRunning) {
                        log.warn("KIS WebSocket closed. Reconnecting...");
                        new Thread(() -> reconnect()).start();
                    }
                }

                @Override
                public void onError(Exception e) {
                    if (isRunning) {
                        log.error("KIS WebSocket error: {}", e.getMessage());
                        new Thread(() -> reconnect()).start();
                    }
                }
            };
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            System.err.println("Invalid WebSocket URI: {}" + e.getMessage());
        }
    }

    private void subscribeToStocks() {
        if (!isRunning) return;
        try {
            // KIS는 approval_key가 필요하다.
            String approvalKey = authService.getApprovalKey();

            List<String> stockCodes = krStockRepository.findAllStockCodes();

            for (String code : stockCodes) {
                String subscribeMessage = String.format(
                        "{\"header\":{\"approval_key\":\"%s\",\"custtype\":\"P\",\"tr_type\":\"1\",\"content-type\":\"utf-8\"}," +
                                "\"body\":{\"input\":{\"tr_id\":\"H0STCNT0\",\"tr_key\":\"%s\"}}}",
                        approvalKey, code
                );

                webSocketClient.send(subscribeMessage);
                Thread.sleep(100); // KIS 서버 부하 방지
            }
        } catch (Exception e) {
            log.error("Failed to send KIS subscription: {}", e.getMessage());
        }
    }

    private void processMessage(String message) {
        // DB 저장 직전 한 번 더 체크 (Context Closed 방어)
        if (!isRunning) return;

        // KIS 실시간 데이터는 보통 '0|H0STCNT0|...' 형태의 파이프(|) 구분자 문자열로 옵니다.
        // 일단 원본 메시지를 Kafka로 던지고, 파싱은 Consumer에서 처리하는 것이 효율적입니다.
        try {
            if(message.contains("H0STCNT0")) { // 실시간 체결 데이터인 경우에만 전송
                kafkaTemplate.send(KAFKA_TOPIC, "KOREA_STOCK", message);
            }
        } catch (Exception e) {
            if (isRunning) log.error("Error processing KIS message: {}", e.getMessage());
        }
    }

    private void reconnect() {
        if (!isRunning) return;
        try {
            TimeUnit.SECONDS.sleep(5);
            connectWebSocket();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
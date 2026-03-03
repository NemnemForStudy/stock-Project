package com.stock.stockwatch.upbit.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpbitWebSocketClient {
    private static final String UPBIT_WEBSOCKET_URI = "wss://api.upbit.com/websocket/v1";
    private static final String KAFKA_TOPIC = "upbit-trades";

    private WebSocketClient webSocketClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private ExecutorService virtualThreadExecutor;

    // 중요: 휘발성 플래그 (메모리 가시성 보장)
    private volatile boolean isRunning = true;

//    @Autowired
//    private TradeEventRepository tradeEventRepository;
    // 대신 Kafka 넣음.
    private final KafkaTemplate<String, String> kafkaTemplate;

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
            webSocketClient = new WebSocketClient(new URI(UPBIT_WEBSOCKET_URI)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("Connected to Upbit WebSocket");
                    subscribeToTrades();
                }

                @Override
                public void onMessage(String message) {
                    // 🚩 앱이 종료 중이면 스레드 할당 자체를 안 함
                    if (isRunning) {
                        virtualThreadExecutor.submit(() -> processMessage(message));
                    }
                }

                @Override
                public void onMessage(ByteBuffer bytes) {
                    if (isRunning) {
                        String message = StandardCharsets.UTF_8.decode(bytes).toString();
                        virtualThreadExecutor.submit(() -> processMessage(message));
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    // 🚩 앱이 종료 중이 아닐 때만 재연결 시도
                    if (isRunning) {
                        new Thread(() -> reconnect()).start();
                    }
                }

                @Override
                public void onError(Exception e) {
                    if (isRunning) {
                        System.err.println("Upbit WebSocket error: " + e.getMessage());
                        new Thread(() -> reconnect()).start();
                    }
                }
            };
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            System.err.println("Invalid WebSocket URI: " + e.getMessage());
        }
    }

    private void subscribeToTrades() {
        if (!isRunning) return;
        try {
            // ticket은 고유값, codes는 구독할 코인 리스트입니다.
            String subscribeMessage = "[{\"ticket\":\"test\"},{\"type\":\"trade\",\"codes\":[\"KRW-BTC\",\"KRW-ETH\"]},{\"format\":\"DEFAULT\"}]";
            webSocketClient.send(subscribeMessage);
            System.out.println("Subscribed to KRW-BTC and KRW-ETH trade events.");
        } catch (Exception e) {
            System.err.println("Failed to send subscription message: " + e.getMessage());
        }
    }

    private void processMessage(String message) {
        // 🚩 DB 저장 직전 한 번 더 체크 (Context Closed 방어)
        if (!isRunning) return;

        try {
            JsonNode rootNode = objectMapper.readTree(message);

            if ("trade".equals(rootNode.path("type").asText())) {
                String code = rootNode.path("code").asText();

                if (isRunning) {
                    // DB 저장 지우고 Kafka로 전송만 함.
                    // 전송할 때 코인 코드를 Key로 사용하면 특정 코인 데이터 순서대로 처리 가능.
                    kafkaTemplate.send(KAFKA_TOPIC, code, message);
                }
            }
        } catch (Exception e) {
            if (isRunning) { // 앱이 정상일 때만 에러 출력
                System.err.println("Error processing message: " + e.getMessage());
            }
        }
    }

    private void reconnect() {
        if (!isRunning) return;

        try {
            System.out.println("Attempting to reconnect in 5 seconds...");
            TimeUnit.SECONDS.sleep(5);
            connectWebSocket();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
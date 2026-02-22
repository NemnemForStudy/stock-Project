package com.stock.stockwatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock.stockwatch.entity.TradeEvent; // 임포트 경로 수정
import com.stock.stockwatch.repository.TradeEventRepository; // 임포트 경로 수정
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class UpbitWebSocketClient {
    private static final String UPBIT_WEBSOCKET_URI = "wss://api.upbit.com/websocket/v1";

    private WebSocketClient webSocketClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private ExecutorService virtualThreadExecutor;

    @Autowired
    private TradeEventRepository tradeEventRepository;

    @PostConstruct
    public void init() {
        virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
        connectWebSocket();
    }

    private void connectWebSocket() {
        try {
            webSocketClient = new WebSocketClient(new URI(UPBIT_WEBSOCKET_URI)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) { // ServerHandshake -> ServerHandshakedata
                    System.out.println("Connected to Upbit WebSocket");
                    subscribeToTrades();
                }

                @Override
                public void onMessage(String message) {
                    virtualThreadExecutor.submit(() -> processMessage(message));
                }

                @Override
                public void onMessage(ByteBuffer bytes) {
                    String message = StandardCharsets.UTF_8.decode(bytes).toString();
                    virtualThreadExecutor.submit(() -> processMessage(message));
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Disconnected from Upbit WebSocket: " + reason + "(Code: " + code + ")");
                    reconnect();
                }

                @Override
                public void onError(Exception e) {
                    System.err.println("Upbit WebSocket error: " + e.getMessage());
                    e.printStackTrace();
                    reconnect();
                }
            };
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            System.err.println("Invalid WebSocket URI: " + e.getMessage());
        }
    }

    private void subscribeToTrades() {
        try {
            String subscribeMessage = "[{\"ticket\":\"test\"},{\"type\":\"trade\",\"codes\":[\"KRW-BTC\",\"KRW-ETH\"]},{\"format\":\"DEFAULT\"}]";
            webSocketClient.send(subscribeMessage);
            System.out.println("Subscribed to KRW-BTC and KRW-ETH trade events.");
        } catch (Exception e) {
            System.err.println("Failed to send subscription message: " + e.getMessage());
        }
    }

    private void processMessage(String message) {
        try {
            JsonNode rootNode = objectMapper.readTree(message);
            String type = rootNode.path("type").asText();

            if("trade".equals(type)) {
                double tradePrice = rootNode.path("trade_price").asDouble();
                double tradeVolume = rootNode.path("trade_volume").asDouble();
                String code = rootNode.path("code").asText();
                long timestamp = rootNode.path("trade_timestamp").asLong();
                double totalKrw = tradePrice * tradeVolume;

                TradeEvent tradeEvent = new TradeEvent(code, tradePrice, tradeVolume, totalKrw, timestamp);
                tradeEventRepository.save(tradeEvent);
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void reconnect() {
        try {
            System.out.println("Attempting to reconnect in 3 seconds...");
            TimeUnit.SECONDS.sleep(3);
            connectWebSocket();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Reconnection attempt interrupted.");
        }
    }

    @PreDestroy
    public void destroy() {
        if(webSocketClient != null) {
            webSocketClient.close();
        }

        if(virtualThreadExecutor != null) {
            virtualThreadExecutor.shutdownNow();
        }
        System.out.println("UpbitWebSocketClient stopped.");
    }
}

package com.stock.stockwatch.exchangeRate.controller;

import com.stock.stockwatch.exchangeRate.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:3000")
public class ExchangeRatesController {
    private final ExchangeRateService exchangeRateService;

    @GetMapping("/exchange-rates")
    public ResponseEntity<Map<String, Double>> getExchangeRate() {
        return ResponseEntity.ok(exchangeRateService.getAllExchangeRates());
    }
}

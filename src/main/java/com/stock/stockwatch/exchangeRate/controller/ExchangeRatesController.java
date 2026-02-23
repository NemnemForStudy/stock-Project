package com.stock.stockwatch.exchangeRate.controller;

import com.stock.stockwatch.exchangeRate.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:3000")
public class ExchangeRatesController {
    private final ExchangeRateService exchangeRateService;

    @GetMapping("/exchange-rates")
    public List<Map<String, Object>> getAllExchangeRates() {
        return exchangeRateService.getAllExchangeRates();
    }
}

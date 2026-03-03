package com.stock.stockwatch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "domestic_stocks")
@Getter
@NoArgsConstructor
public class DomesticStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    private String marketType;
    public DomesticStock(String code, String name, String marketType) {
        this.code = code;
        this.name = name;
        this.marketType = marketType;
    }
}

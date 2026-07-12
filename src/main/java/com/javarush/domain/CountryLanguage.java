package com.javarush.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(schema = "world", name = "country_language")
public class CountryLanguage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @Column(name = "language", nullable = false, length = 30)
    private String language;

    @Column(name = "is_official", nullable = false)
    private Boolean isOfficial;

    @Column(name = "percentage", nullable = false, precision = 4, scale = 1)
    private BigDecimal percentage;
}

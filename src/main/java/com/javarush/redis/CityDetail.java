package com.javarush.redis;

import com.javarush.domain.Continent;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Класс-модель, содержащий детальную информацию о городе и связанной с ним стране.
 * Используется для кэширования и сериализации данных в формат JSON в Redis.
 */
@Getter
@Setter
public class CityDetail {
    private Integer id;

    private String cityName;

    private String district;

    private Integer cityPopulation;

    private String countryCode;

    private String countryAltCode;

    private String countryName;

    private Continent continent;

    private String countryRegion;

    private BigDecimal countrySurfaceArea;

    private Integer countryPopulation;

    private Set<Language> languages;
}

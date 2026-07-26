package com.javarush.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Класс-модель, описывающий язык, используемый в определенной стране,
 * включая статус официального языка и процент говорящего населения.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Language {
    private String language;

    private Boolean isOfficial;

    private BigDecimal percentage;
}

package com.javarush.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Сущность (Entity), представляющая город в базе даннных world.
 * Мапится на таблицу "city".
 * Города связаны с таблицей стран {@link Country} через отношение Many-to-One.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="city")
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name="name", nullable = false, length = 35)
    private String name;

    @ManyToOne @JoinColumn(name="country_id", nullable = false)
    private Country country;

    @Column(name="district", nullable = false, length = 20)
    private String district;

    @Column(name="population", nullable = false)
    private Integer population;
}

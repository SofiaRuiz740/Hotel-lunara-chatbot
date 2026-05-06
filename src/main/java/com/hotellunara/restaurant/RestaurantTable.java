package com.hotellunara.restaurant;

import com.hotellunara.common.enums.RestaurantTableLocation;
import com.hotellunara.common.enums.RestaurantTableStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "restaurant_tables")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private int numero;

    @Column(nullable = false)
    private int capacidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RestaurantTableLocation ubicacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RestaurantTableStatus estado = RestaurantTableStatus.LIBRE;

    @Column(nullable = false)
    @Builder.Default
    private boolean activa = true;
}

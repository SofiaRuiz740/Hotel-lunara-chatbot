package com.hotellunara.restaurant.dto;

import com.hotellunara.common.enums.RestaurantTableLocation;
import com.hotellunara.common.enums.RestaurantTableStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantTableResponseDTO {

    private Long id;
    private int numero;
    private int capacidad;
    private RestaurantTableLocation ubicacion;
    private RestaurantTableStatus estado;
    private boolean activa;
}

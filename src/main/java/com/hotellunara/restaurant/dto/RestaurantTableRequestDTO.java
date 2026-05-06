package com.hotellunara.restaurant.dto;

import com.hotellunara.common.enums.RestaurantTableLocation;
import com.hotellunara.common.enums.RestaurantTableStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantTableRequestDTO {

    @Min(value = 1, message = "El numero debe ser positivo")
    private int numero;

    @Min(value = 1, message = "La capacidad debe ser positiva")
    private int capacidad;

    @NotNull(message = "La ubicacion es obligatoria")
    private RestaurantTableLocation ubicacion;

    private RestaurantTableStatus estado;
    private Boolean activa;
}

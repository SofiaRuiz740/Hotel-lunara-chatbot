package com.hotellunara.restaurant.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantAvailabilityResponse {

    private LocalDate fecha;
    private LocalTime hora;
    private int cantidadPersonas;
    private List<RestaurantTableResponseDTO> mesasDisponibles;
}

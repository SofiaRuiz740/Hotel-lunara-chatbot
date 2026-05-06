package com.hotellunara.restaurant.dto;

import com.hotellunara.common.enums.RestaurantOccasion;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantReservationRequestDTO {

    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(message = "La fecha debe ser hoy o futura")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria")
    private LocalTime hora;

    @Min(value = 1, message = "La cantidad de personas debe ser positiva")
    private int cantidadPersonas;

    private RestaurantOccasion ocasionEspecial;

    @Size(max = 255)
    private String peticiones;

    private Long reservationHotelId;
}

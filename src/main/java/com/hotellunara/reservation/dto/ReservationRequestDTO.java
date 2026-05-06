package com.hotellunara.reservation.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequestDTO {

    @NotNull(message = "La habitacion es obligatoria")
    private Long roomId;

    @NotNull(message = "La fecha de check-in es obligatoria")
    @FutureOrPresent(message = "La fecha de check-in debe ser hoy o futura")
    private LocalDate checkIn;

    @NotNull(message = "La fecha de check-out es obligatoria")
    @FutureOrPresent(message = "La fecha de check-out debe ser hoy o futura")
    private LocalDate checkOut;

    @Min(value = 1, message = "Debe haber al menos un adulto")
    private int cantidadAdultos;

    @Min(value = 0, message = "La cantidad de ninos no puede ser negativa")
    private int cantidadNinos;

    @Size(max = 255)
    private String peticionesEspeciales;
}

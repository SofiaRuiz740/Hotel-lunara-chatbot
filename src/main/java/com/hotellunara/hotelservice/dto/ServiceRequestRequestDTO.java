package com.hotellunara.hotelservice.dto;

import jakarta.validation.constraints.FutureOrPresent;
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
public class ServiceRequestRequestDTO {

    @NotNull(message = "El servicio es obligatorio")
    private Long serviceId;

    private Long reservationId;

    @NotNull(message = "La fecha solicitada es obligatoria")
    @FutureOrPresent(message = "La fecha debe ser hoy o futura")
    private LocalDate fechaSolicitada;

    @NotNull(message = "La hora solicitada es obligatoria")
    private LocalTime horaSolicitada;

    @Size(max = 255)
    private String notas;
}

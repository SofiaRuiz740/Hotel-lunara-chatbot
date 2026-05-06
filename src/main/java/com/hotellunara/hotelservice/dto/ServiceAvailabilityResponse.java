package com.hotellunara.hotelservice.dto;

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
public class ServiceAvailabilityResponse {

    private Long serviceId;
    private String serviceNombre;
    private LocalDate fecha;
    private LocalTime hora;
    private int capacidadMaxima;
    private long reservasConfirmadas;
    private long cuposDisponibles;
    private boolean disponible;
}

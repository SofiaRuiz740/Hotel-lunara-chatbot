package com.hotellunara.admin.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private long totalHabitaciones;
    private long habitacionesOcupadas;
    private long habitacionesDisponibles;
    private long habitacionesMantenimiento;
    private long reservasHoy;
    private long checkoutsHoy;
    private long reservasActivasTotales;
    private BigDecimal ingresosMesActual;
    private long totalHuespedesRegistrados;
    private long solicitudesPendientes;
}

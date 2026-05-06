package com.hotellunara.reservation.dto;

import com.hotellunara.common.enums.ReservationCreatedBy;
import com.hotellunara.common.enums.ReservationStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponseDTO {

    private Long id;
    private String codigoReserva;
    private UUID guestId;
    private String guestNombreCompleto;
    private Long roomId;
    private String roomNumero;
    private String roomTipo;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int cantidadNoches;
    private int cantidadAdultos;
    private int cantidadNinos;
    private BigDecimal precioNoche;
    private BigDecimal precioTotal;
    private ReservationStatus estado;
    private String motivoCancelacion;
    private LocalDateTime fechaCancelacion;
    private String peticionesEspeciales;
    private ReservationCreatedBy creadaPor;
    private LocalDateTime fechaCreacion;
}

package com.hotellunara.hotelservice.dto;

import com.hotellunara.common.enums.ServiceRequestStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequestResponseDTO {

    private Long id;
    private UUID guestId;
    private String guestNombreCompleto;
    private Long reservationId;
    private Long serviceId;
    private String serviceNombre;
    private String serviceCategoria;
    private LocalDate fechaSolicitada;
    private LocalTime horaSolicitada;
    private ServiceRequestStatus estado;
    private String notas;
    private BigDecimal precioAplicado;
    private UUID atendidoPorId;
    private String atendidoPorNombre;
    private LocalDateTime creadaEn;
}

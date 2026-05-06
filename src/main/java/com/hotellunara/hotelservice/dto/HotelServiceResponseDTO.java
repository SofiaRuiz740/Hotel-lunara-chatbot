package com.hotellunara.hotelservice.dto;

import java.math.BigDecimal;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelServiceResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private String categoria;
    private BigDecimal precio;
    private int duracion;
    private LocalTime horarioApertura;
    private LocalTime horarioCierre;
    private boolean requiereReserva;
    private boolean disponibleParaExternos;
    private int capacidadMaximaPorSlot;
    private boolean activo;
}

package com.hotellunara.hotelservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class HotelServiceRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120)
    private String nombre;

    private String descripcion;

    @NotBlank(message = "La categoria es obligatoria")
    @Size(max = 60)
    private String categoria;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
    private BigDecimal precio;

    @Min(value = 0, message = "La duracion no puede ser negativa")
    private int duracion;

    @NotNull(message = "El horario de apertura es obligatorio")
    private LocalTime horarioApertura;

    @NotNull(message = "El horario de cierre es obligatorio")
    private LocalTime horarioCierre;

    private Boolean requiereReserva;
    private Boolean disponibleParaExternos;

    @Min(value = 0, message = "La capacidad no puede ser negativa")
    private int capacidadMaximaPorSlot;

    private Boolean activo;
}

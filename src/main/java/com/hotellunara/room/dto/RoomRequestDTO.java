package com.hotellunara.room.dto;

import com.hotellunara.common.enums.RoomStatus;
import com.hotellunara.common.enums.RoomType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomRequestDTO {

    @NotBlank(message = "El numero es obligatorio")
    @Size(max = 10)
    private String numero;

    @Min(value = 1, message = "El piso debe ser positivo")
    private int piso;

    @NotNull(message = "El tipo es obligatorio")
    private RoomType tipo;

    @Min(value = 1, message = "La capacidad de adultos debe ser al menos 1")
    private int capacidadAdultos;

    @Min(value = 0, message = "La capacidad de ninos no puede ser negativa")
    private int capacidadNinos;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a cero")
    private BigDecimal precioPorNoche;

    private String descripcion;

    @Size(max = 500)
    private String amenities;

    private RoomStatus estado;

    @Size(max = 1000)
    private String imagenes;

    private Boolean activa;
}

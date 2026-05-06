package com.hotellunara.room.dto;

import com.hotellunara.common.enums.RoomStatus;
import com.hotellunara.common.enums.RoomType;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponseDTO {

    private Long id;
    private String numero;
    private int piso;
    private RoomType tipo;
    private int capacidadAdultos;
    private int capacidadNinos;
    private BigDecimal precioPorNoche;
    private String descripcion;
    private String amenities;
    private RoomStatus estado;
    private String imagenes;
    private boolean activa;
}

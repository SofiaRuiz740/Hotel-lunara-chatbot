package com.hotellunara.room;

import com.hotellunara.common.enums.RoomStatus;
import com.hotellunara.common.enums.RoomType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rooms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String numero;

    @Column(nullable = false)
    private int piso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomType tipo;

    @Column(nullable = false)
    private int capacidadAdultos;

    @Column(nullable = false)
    private int capacidadNinos;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioPorNoche;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 500)
    private String amenities;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RoomStatus estado = RoomStatus.DISPONIBLE;

    @Column(length = 1000)
    private String imagenes;

    @Column(nullable = false)
    @Builder.Default
    private boolean activa = true;
}

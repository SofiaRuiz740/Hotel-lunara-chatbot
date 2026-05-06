package com.hotellunara.hotelservice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hotel_services")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, length = 60)
    private String categoria;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private int duracion;

    @Column(nullable = false)
    private LocalTime horarioApertura;

    @Column(nullable = false)
    private LocalTime horarioCierre;

    @Column(nullable = false)
    @Builder.Default
    private boolean requiereReserva = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean disponibleParaExternos = false;

    @Column(nullable = false)
    private int capacidadMaximaPorSlot;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;
}

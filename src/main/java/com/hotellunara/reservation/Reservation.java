package com.hotellunara.reservation;

import com.hotellunara.common.enums.ReservationCreatedBy;
import com.hotellunara.common.enums.ReservationStatus;
import com.hotellunara.room.Room;
import com.hotellunara.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reservations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigoReserva;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guest_id", nullable = false)
    private User guest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private LocalDate checkIn;

    @Column(nullable = false)
    private LocalDate checkOut;

    @Column(nullable = false)
    private int cantidadNoches;

    @Column(nullable = false)
    private int cantidadAdultos;

    @Column(nullable = false)
    private int cantidadNinos;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioNoche;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal precioTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus estado;

    @Column(length = 255)
    private String motivoCancelacion;

    private LocalDateTime fechaCancelacion;

    @Column(length = 255)
    private String peticionesEspeciales;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationCreatedBy creadaPor;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}

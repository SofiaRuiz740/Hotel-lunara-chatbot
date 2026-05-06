package com.hotellunara.restaurant;

import com.hotellunara.common.enums.RestaurantOccasion;
import com.hotellunara.common.enums.RestaurantReservationStatus;
import com.hotellunara.reservation.Reservation;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "restaurant_reservations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guest_id", nullable = false)
    private User guest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "table_id", nullable = false)
    private RestaurantTable table;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(nullable = false)
    private int cantidadPersonas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RestaurantOccasion ocasionEspecial = RestaurantOccasion.NINGUNA;

    @Column(length = 255)
    private String peticiones;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RestaurantReservationStatus estado = RestaurantReservationStatus.CONFIRMADA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_hotel_id")
    private Reservation reservationHotel;

    @Column(nullable = false)
    private LocalDateTime creadaEn;

    @PrePersist
    public void prePersist() {
        if (creadaEn == null) {
            creadaEn = LocalDateTime.now();
        }
    }
}

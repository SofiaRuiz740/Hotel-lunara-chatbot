package com.hotellunara.hotelservice;

import com.hotellunara.common.enums.ServiceRequestStatus;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "service_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guest_id", nullable = false)
    private User guest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private HotelService service;

    @Column(nullable = false)
    private LocalDate fechaSolicitada;

    @Column(nullable = false)
    private LocalTime horaSolicitada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ServiceRequestStatus estado = ServiceRequestStatus.PENDIENTE;

    @Column(length = 255)
    private String notas;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioAplicado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendido_por_id")
    private User atendidoPor;

    @Column(nullable = false)
    private LocalDateTime creadaEn;

    @PrePersist
    public void prePersist() {
        if (creadaEn == null) {
            creadaEn = LocalDateTime.now();
        }
    }
}

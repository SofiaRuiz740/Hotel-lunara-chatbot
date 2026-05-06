package com.hotellunara.reservation;

import com.hotellunara.common.enums.ReservationStatus;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
            select count(r) > 0
            from Reservation r
            where r.room.id = :roomId
              and r.estado in :statuses
              and r.checkIn < :checkOut
              and r.checkOut > :checkIn
            """)
    boolean existsOverlappingReservation(@Param("roomId") Long roomId,
                                         @Param("checkIn") LocalDate checkIn,
                                         @Param("checkOut") LocalDate checkOut,
                                         @Param("statuses") Collection<ReservationStatus> statuses);

    List<Reservation> findByGuestIdOrderByFechaCreacionDesc(UUID guestId);

    Optional<Reservation> findByIdAndGuestId(Long id, UUID guestId);

    List<Reservation> findByEstadoInOrderByCheckInAsc(List<ReservationStatus> statuses);

    @Query("""
            select r
            from Reservation r
            where r.checkIn = :checkIn
            order by r.fechaCreacion desc
            """)
    List<Reservation> findByCheckInOrderByFechaCreacionDesc(@Param("checkIn") LocalDate checkIn);

    @Query("""
            select r
            from Reservation r
            where r.checkOut = :checkOut
            order by r.fechaCreacion desc
            """)
    List<Reservation> findByCheckOutOrderByFechaCreacionDesc(@Param("checkOut") LocalDate checkOut);

    List<Reservation> findByEstadoOrderByCheckInAsc(ReservationStatus status);

    Optional<Reservation> findTopByOrderByIdDesc();

    @Query("""
            select count(r)
            from Reservation r
            where r.checkIn = :checkIn
            """)
    long countByCheckIn(@Param("checkIn") LocalDate checkIn);

    @Query("""
            select count(r)
            from Reservation r
            where r.checkOut = :checkOut
            """)
    long countByCheckOut(@Param("checkOut") LocalDate checkOut);

    long countByEstado(ReservationStatus status);

    @Query("""
            select coalesce(sum(r.precioTotal), 0)
            from Reservation r
            where r.estado = :status
              and year(r.checkOut) = :year
              and month(r.checkOut) = :month
            """)
    java.math.BigDecimal sumCompletedRevenueByMonth(@Param("status") ReservationStatus status,
                                                    @Param("year") int year,
                                                    @Param("month") int month);

    @Query("""
            select r
            from Reservation r
            where r.guest.id = :guestId
              and r.estado in :statuses
              and r.checkOut >= :today
            order by r.checkIn asc
            """)
    List<Reservation> findUpcomingOrActiveReservations(@Param("guestId") UUID guestId,
                                                       @Param("statuses") Collection<ReservationStatus> statuses,
                                                       @Param("today") LocalDate today);

    @Query("""
            select r
            from Reservation r
            where r.guest.id = :guestId
              and r.estado = 'ACTIVA'
              and :date between r.checkIn and r.checkOut
            order by r.checkIn asc
            """)
    List<Reservation> findActiveReservationsOnDate(@Param("guestId") UUID guestId, @Param("date") LocalDate date);

    Page<Reservation> findAllByOrderByFechaCreacionDesc(Pageable pageable);
}

package com.hotellunara.restaurant;

import com.hotellunara.common.enums.RestaurantReservationStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RestaurantRepository extends JpaRepository<RestaurantReservation, Long> {

    List<RestaurantReservation> findByFechaAndEstado(LocalDate fecha, RestaurantReservationStatus estado);

    List<RestaurantReservation> findByGuestIdOrderByFechaDescHoraDesc(UUID guestId);

    List<RestaurantReservation> findByFechaBetweenAndGuestIdOrderByFechaAscHoraAsc(LocalDate start,
                                                                                    LocalDate end,
                                                                                    UUID guestId);

    List<RestaurantReservation> findByFechaGreaterThanEqualAndGuestIdOrderByFechaAscHoraAsc(LocalDate fecha, UUID guestId);

    List<RestaurantReservation> findAllByOrderByFechaDescHoraDesc();

    @Query("""
            select count(r) > 0
            from RestaurantReservation r
            where r.table.id = :tableId
              and r.fecha = :fecha
              and r.estado in :statuses
              and r.hora between :windowStart and :windowEnd
            """)
    boolean existsTableConflict(@Param("tableId") Long tableId,
                                @Param("fecha") LocalDate fecha,
                                @Param("windowStart") LocalTime windowStart,
                                @Param("windowEnd") LocalTime windowEnd,
                                @Param("statuses") Collection<RestaurantReservationStatus> statuses);
}

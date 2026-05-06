package com.hotellunara.hotelservice;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HotelServiceRepository extends JpaRepository<HotelService, Long> {

    List<HotelService> findByActivoTrueOrderByNombreAsc();

    Optional<HotelService> findByIdAndActivoTrue(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from HotelService s where s.id = :id and s.activo = true")
    Optional<HotelService> findByIdAndActivoTrueForUpdate(@Param("id") Long id);
}

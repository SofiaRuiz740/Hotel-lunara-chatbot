package com.hotellunara.restaurant;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {

    List<RestaurantTable> findByActivaTrueAndCapacidadGreaterThanEqualOrderByCapacidadAscNumeroAsc(int capacidad);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from RestaurantTable t where t.id = :id and t.activa = true")
    Optional<RestaurantTable> findByIdForUpdate(@Param("id") Long id);
}

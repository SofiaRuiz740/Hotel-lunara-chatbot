package com.hotellunara.restaurant.dto;

import com.hotellunara.common.enums.RestaurantOccasion;
import com.hotellunara.common.enums.RestaurantReservationStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantReservationResponseDTO {

    private Long id;
    private UUID guestId;
    private String guestNombreCompleto;
    private Long tableId;
    private Integer tableNumero;
    private LocalDate fecha;
    private LocalTime hora;
    private int cantidadPersonas;
    private RestaurantOccasion ocasionEspecial;
    private String peticiones;
    private RestaurantReservationStatus estado;
    private Long reservationHotelId;
    private LocalDateTime creadaEn;
}

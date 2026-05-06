package com.hotellunara.reservation.dto;

import com.hotellunara.hotelservice.dto.ServiceRequestResponseDTO;
import com.hotellunara.restaurant.dto.RestaurantReservationResponseDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutSummaryResponse {

    private ReservationResponseDTO reserva;
    private List<ServiceRequestResponseDTO> serviciosConsumidos;
    private List<RestaurantReservationResponseDTO> reservasRestaurante;
}

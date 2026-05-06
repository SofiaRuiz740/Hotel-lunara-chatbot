package com.hotellunara.restaurant.dto;

import com.hotellunara.common.enums.RestaurantReservationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantReservationStatusUpdateRequest {

    @NotNull(message = "El estado es obligatorio")
    private RestaurantReservationStatus estado;
}

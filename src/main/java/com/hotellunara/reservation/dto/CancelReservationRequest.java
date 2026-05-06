package com.hotellunara.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelReservationRequest {

    @NotBlank(message = "El motivo de cancelacion es obligatorio")
    @Size(max = 255)
    private String motivo;
}

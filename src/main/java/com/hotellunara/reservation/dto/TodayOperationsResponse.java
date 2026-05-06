package com.hotellunara.reservation.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodayOperationsResponse {

    private List<ReservationResponseDTO> checkInsHoy;
    private List<ReservationResponseDTO> checkOutsHoy;
    private List<ReservationResponseDTO> reservasActivas;
}

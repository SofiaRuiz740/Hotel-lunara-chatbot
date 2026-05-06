package com.hotellunara.hotelservice.dto;

import com.hotellunara.common.enums.ServiceRequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequestStatusUpdateRequest {

    @NotNull(message = "El estado es obligatorio")
    private ServiceRequestStatus estado;
}

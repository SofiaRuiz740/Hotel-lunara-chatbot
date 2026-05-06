package com.hotellunara.concierge.dto;

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
public class ConciergeChatRequest {

    @NotBlank(message = "El mensaje es obligatorio")
    @Size(max = 2000)
    private String mensaje;

    @Size(max = 80)
    private String sessionToken;
}

package com.hotellunara.concierge.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationSessionResponseDTO {

    private UUID id;
    private UUID guestId;
    private String sessionToken;
    private String idiomaDetectado;
    private boolean activa;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaUltimoMensaje;
    private int totalMensajes;
}

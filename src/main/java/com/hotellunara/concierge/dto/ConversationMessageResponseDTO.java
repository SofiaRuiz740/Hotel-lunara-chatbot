package com.hotellunara.concierge.dto;

import com.hotellunara.common.enums.ConversationRole;
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
public class ConversationMessageResponseDTO {

    private UUID id;
    private UUID sessionId;
    private ConversationRole role;
    private String contenido;
    private LocalDateTime timestamp;
    private int tokensUsados;
    private String contextoSnapshot;
}

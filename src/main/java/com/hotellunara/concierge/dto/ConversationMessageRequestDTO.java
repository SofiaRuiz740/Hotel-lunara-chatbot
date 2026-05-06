package com.hotellunara.concierge.dto;

import com.hotellunara.common.enums.ConversationRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationMessageRequestDTO {

    @NotNull(message = "El rol es obligatorio")
    private ConversationRole role;

    @NotBlank(message = "El contenido es obligatorio")
    private String contenido;

    private Integer tokensUsados;
    private String contextoSnapshot;
}

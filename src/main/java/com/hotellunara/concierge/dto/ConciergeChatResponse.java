package com.hotellunara.concierge.dto;

import com.hotellunara.common.enums.ConciergeActionSuggested;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConciergeChatResponse {

    private String respuesta;
    private ConciergeActionSuggested actionSuggested;
    private String sessionToken;
}

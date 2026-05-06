package com.hotellunara.concierge;

import com.hotellunara.concierge.dto.ConversationSessionRequestDTO;
import com.hotellunara.concierge.dto.ConversationSessionResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConversationSessionMapper {

    @Mapping(target = "guestId", source = "guest.id")
    ConversationSessionResponseDTO toResponse(ConversationSession session);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "guest", ignore = true)
    @Mapping(target = "fechaInicio", ignore = true)
    @Mapping(target = "fechaUltimoMensaje", ignore = true)
    @Mapping(target = "totalMensajes", ignore = true)
    ConversationSession toEntity(ConversationSessionRequestDTO requestDTO);
}

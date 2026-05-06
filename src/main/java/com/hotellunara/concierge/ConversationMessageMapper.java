package com.hotellunara.concierge;

import com.hotellunara.concierge.dto.ConversationMessageRequestDTO;
import com.hotellunara.concierge.dto.ConversationMessageResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConversationMessageMapper {

    @Mapping(target = "sessionId", source = "session.id")
    ConversationMessageResponseDTO toResponse(ConversationMessage message);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "session", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    ConversationMessage toEntity(ConversationMessageRequestDTO requestDTO);
}

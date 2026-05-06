package com.hotellunara.concierge;

import com.hotellunara.concierge.dto.ConversationMessageRequestDTO;
import com.hotellunara.concierge.dto.ConversationMessageResponseDTO;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-05T18:09:55-0500",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class ConversationMessageMapperImpl implements ConversationMessageMapper {

    @Override
    public ConversationMessageResponseDTO toResponse(ConversationMessage message) {
        if ( message == null ) {
            return null;
        }

        ConversationMessageResponseDTO.ConversationMessageResponseDTOBuilder conversationMessageResponseDTO = ConversationMessageResponseDTO.builder();

        conversationMessageResponseDTO.sessionId( messageSessionId( message ) );
        conversationMessageResponseDTO.contenido( message.getContenido() );
        conversationMessageResponseDTO.contextoSnapshot( message.getContextoSnapshot() );
        conversationMessageResponseDTO.id( message.getId() );
        conversationMessageResponseDTO.role( message.getRole() );
        conversationMessageResponseDTO.timestamp( message.getTimestamp() );
        conversationMessageResponseDTO.tokensUsados( message.getTokensUsados() );

        return conversationMessageResponseDTO.build();
    }

    @Override
    public ConversationMessage toEntity(ConversationMessageRequestDTO requestDTO) {
        if ( requestDTO == null ) {
            return null;
        }

        ConversationMessage.ConversationMessageBuilder conversationMessage = ConversationMessage.builder();

        conversationMessage.contenido( requestDTO.getContenido() );
        conversationMessage.contextoSnapshot( requestDTO.getContextoSnapshot() );
        conversationMessage.role( requestDTO.getRole() );
        if ( requestDTO.getTokensUsados() != null ) {
            conversationMessage.tokensUsados( requestDTO.getTokensUsados() );
        }

        return conversationMessage.build();
    }

    private UUID messageSessionId(ConversationMessage conversationMessage) {
        if ( conversationMessage == null ) {
            return null;
        }
        ConversationSession session = conversationMessage.getSession();
        if ( session == null ) {
            return null;
        }
        UUID id = session.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}

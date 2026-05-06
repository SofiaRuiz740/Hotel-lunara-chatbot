package com.hotellunara.concierge;

import com.hotellunara.concierge.dto.ConversationSessionRequestDTO;
import com.hotellunara.concierge.dto.ConversationSessionResponseDTO;
import com.hotellunara.user.User;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-05T18:09:48-0500",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class ConversationSessionMapperImpl implements ConversationSessionMapper {

    @Override
    public ConversationSessionResponseDTO toResponse(ConversationSession session) {
        if ( session == null ) {
            return null;
        }

        ConversationSessionResponseDTO.ConversationSessionResponseDTOBuilder conversationSessionResponseDTO = ConversationSessionResponseDTO.builder();

        conversationSessionResponseDTO.guestId( sessionGuestId( session ) );
        conversationSessionResponseDTO.activa( session.isActiva() );
        conversationSessionResponseDTO.fechaInicio( session.getFechaInicio() );
        conversationSessionResponseDTO.fechaUltimoMensaje( session.getFechaUltimoMensaje() );
        conversationSessionResponseDTO.id( session.getId() );
        conversationSessionResponseDTO.idiomaDetectado( session.getIdiomaDetectado() );
        conversationSessionResponseDTO.sessionToken( session.getSessionToken() );
        conversationSessionResponseDTO.totalMensajes( session.getTotalMensajes() );

        return conversationSessionResponseDTO.build();
    }

    @Override
    public ConversationSession toEntity(ConversationSessionRequestDTO requestDTO) {
        if ( requestDTO == null ) {
            return null;
        }

        ConversationSession.ConversationSessionBuilder conversationSession = ConversationSession.builder();

        if ( requestDTO.getActiva() != null ) {
            conversationSession.activa( requestDTO.getActiva() );
        }
        conversationSession.idiomaDetectado( requestDTO.getIdiomaDetectado() );
        conversationSession.sessionToken( requestDTO.getSessionToken() );

        return conversationSession.build();
    }

    private UUID sessionGuestId(ConversationSession conversationSession) {
        if ( conversationSession == null ) {
            return null;
        }
        User guest = conversationSession.getGuest();
        if ( guest == null ) {
            return null;
        }
        UUID id = guest.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}

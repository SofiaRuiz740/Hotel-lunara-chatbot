package com.hotellunara.concierge;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<ConversationSession, UUID> {

    Optional<ConversationSession> findFirstByGuestIdAndActivaTrueOrderByFechaUltimoMensajeDesc(UUID guestId);

    Optional<ConversationSession> findBySessionTokenAndActivaTrue(String sessionToken);

    List<ConversationSession> findByGuestIdOrderByFechaUltimoMensajeDesc(UUID guestId);
}

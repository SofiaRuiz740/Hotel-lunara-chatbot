package com.hotellunara.concierge;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<ConversationMessage, UUID> {

    List<ConversationMessage> findTop5BySessionIdOrderByTimestampDesc(UUID sessionId);

    List<ConversationMessage> findBySessionGuestIdOrderByTimestampAsc(UUID guestId);
}

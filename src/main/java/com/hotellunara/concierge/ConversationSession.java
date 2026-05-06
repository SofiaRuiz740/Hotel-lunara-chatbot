package com.hotellunara.concierge;

import com.hotellunara.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "conversation_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id")
    private User guest;

    @Column(nullable = false, unique = true, length = 80)
    private String sessionToken;

    @Column(length = 20)
    private String idiomaDetectado;

    @Column(nullable = false)
    @Builder.Default
    private boolean activa = true;

    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    @Column(nullable = false)
    private LocalDateTime fechaUltimoMensaje;

    @Column(nullable = false)
    @Builder.Default
    private int totalMensajes = 0;

    @PrePersist
    public void prePersist() {
        if (fechaInicio == null) {
            fechaInicio = LocalDateTime.now();
        }
        if (fechaUltimoMensaje == null) {
            fechaUltimoMensaje = LocalDateTime.now();
        }
    }
}

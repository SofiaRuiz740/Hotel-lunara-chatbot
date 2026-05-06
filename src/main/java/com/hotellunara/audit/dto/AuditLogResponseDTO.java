package com.hotellunara.audit.dto;

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
public class AuditLogResponseDTO {

    private Long id;
    private UUID usuarioId;
    private String usuarioEmail;
    private String accion;
    private String entidad;
    private String entidadId;
    private String detalles;
    private String ip;
    private LocalDateTime timestamp;
}

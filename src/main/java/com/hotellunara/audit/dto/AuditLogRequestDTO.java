package com.hotellunara.audit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogRequestDTO {

    private java.util.UUID usuarioId;

    @NotBlank(message = "La accion es obligatoria")
    @Size(max = 120)
    private String accion;

    @NotBlank(message = "La entidad es obligatoria")
    @Size(max = 120)
    private String entidad;

    @NotBlank(message = "El id de entidad es obligatorio")
    @Size(max = 120)
    private String entidadId;

    private String detalles;

    @Size(max = 80)
    private String ip;
}

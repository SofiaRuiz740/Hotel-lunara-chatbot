package com.hotellunara.audit;

import com.hotellunara.audit.dto.AuditLogRequestDTO;
import com.hotellunara.audit.dto.AuditLogResponseDTO;
import com.hotellunara.user.User;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-05T18:09:53-0500",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class AuditLogMapperImpl implements AuditLogMapper {

    @Override
    public AuditLogResponseDTO toResponse(AuditLog entity) {
        if ( entity == null ) {
            return null;
        }

        AuditLogResponseDTO.AuditLogResponseDTOBuilder auditLogResponseDTO = AuditLogResponseDTO.builder();

        auditLogResponseDTO.usuarioId( entityUsuarioId( entity ) );
        auditLogResponseDTO.usuarioEmail( entityUsuarioEmail( entity ) );
        auditLogResponseDTO.accion( entity.getAccion() );
        auditLogResponseDTO.detalles( entity.getDetalles() );
        auditLogResponseDTO.entidad( entity.getEntidad() );
        auditLogResponseDTO.entidadId( entity.getEntidadId() );
        auditLogResponseDTO.id( entity.getId() );
        auditLogResponseDTO.ip( entity.getIp() );
        auditLogResponseDTO.timestamp( entity.getTimestamp() );

        return auditLogResponseDTO.build();
    }

    @Override
    public AuditLog toEntity(AuditLogRequestDTO requestDTO) {
        if ( requestDTO == null ) {
            return null;
        }

        AuditLog.AuditLogBuilder auditLog = AuditLog.builder();

        auditLog.accion( requestDTO.getAccion() );
        auditLog.detalles( requestDTO.getDetalles() );
        auditLog.entidad( requestDTO.getEntidad() );
        auditLog.entidadId( requestDTO.getEntidadId() );
        auditLog.ip( requestDTO.getIp() );

        return auditLog.build();
    }

    private UUID entityUsuarioId(AuditLog auditLog) {
        if ( auditLog == null ) {
            return null;
        }
        User usuario = auditLog.getUsuario();
        if ( usuario == null ) {
            return null;
        }
        UUID id = usuario.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityUsuarioEmail(AuditLog auditLog) {
        if ( auditLog == null ) {
            return null;
        }
        User usuario = auditLog.getUsuario();
        if ( usuario == null ) {
            return null;
        }
        String email = usuario.getEmail();
        if ( email == null ) {
            return null;
        }
        return email;
    }
}

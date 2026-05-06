package com.hotellunara.audit;

import com.hotellunara.audit.dto.AuditLogRequestDTO;
import com.hotellunara.audit.dto.AuditLogResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "usuarioEmail", source = "usuario.email")
    AuditLogResponseDTO toResponse(AuditLog entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    AuditLog toEntity(AuditLogRequestDTO requestDTO);
}

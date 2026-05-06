package com.hotellunara.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotellunara.audit.dto.AuditLogResponseDTO;
import com.hotellunara.user.User;
import com.hotellunara.user.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository auditRepository;
    private final AuditLogMapper auditLogMapper;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void log(UUID userId, String action, String entity, String entityId, Object details, String ip) {
        User user = userId == null ? null : userRepository.findById(userId).orElse(null);
        log(user, action, entity, entityId, details, ip);
    }

    @Transactional
    public void log(User user, String action, String entity, String entityId, Object details, String ip) {
        AuditLog log = AuditLog.builder()
                .usuario(user)
                .accion(action)
                .entidad(entity)
                .entidadId(entityId)
                .detalles(toJson(details))
                .ip(resolveIp(ip))
                .build();
        auditRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponseDTO> getAllLogs() {
        return auditRepository.findAllByOrderByTimestampDesc()
                .stream()
                .map(auditLogMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponseDTO> getAllLogs(Pageable pageable) {
        return auditRepository.findAllByOrderByTimestampDesc(pageable)
                .map(auditLogMapper::toResponse);
    }

    private String toJson(Object details) {
        if (details == null) {
            return null;
        }
        if (details instanceof String value) {
            return value;
        }
        try {
            return objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException ex) {
            return "{\"serializationError\":\"No fue posible serializar detalles\"}";
        }
    }

    private String resolveIp(String explicitIp) {
        if (explicitIp != null && !explicitIp.isBlank()) {
            return explicitIp;
        }

        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            return null;
        }

        HttpServletRequest request = servletAttributes.getRequest();
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }

        return request.getRemoteAddr();
    }
}

package com.hotellunara.admin;

import com.hotellunara.admin.dto.DashboardResponse;
import com.hotellunara.audit.dto.AuditLogResponseDTO;
import com.hotellunara.auth.dto.RegisterRequest;
import com.hotellunara.common.dto.PageResponse;
import com.hotellunara.common.response.ApiResponse;
import com.hotellunara.user.User;
import com.hotellunara.user.dto.UserResponseDTO;
import com.hotellunara.user.dto.UserRoleUpdateRequest;
import com.hotellunara.user.dto.UserStatusUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administracion", description = "Operaciones administrativas y dashboard")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    @Operation(summary = "Obtener metricas del dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success("Dashboard obtenido correctamente", adminService.getDashboard()));
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Obtener audit log completo")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponseDTO>>> getAuditLogs(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(ApiResponse.success("Audit log obtenido correctamente", adminService.getAuditLogs(page, size)));
    }

    @GetMapping("/users")
    @Operation(summary = "Listar usuarios registrados")
    public ResponseEntity<ApiResponse<PageResponse<UserResponseDTO>>> getUsers(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(ApiResponse.success("Usuarios obtenidos correctamente", adminService.getUsers(page, size)));
    }

    @PostMapping("/receptionists")
    @Operation(summary = "Crear una cuenta de recepcionista")
    public ResponseEntity<ApiResponse<UserResponseDTO>> createReceptionist(@Valid @RequestBody RegisterRequest request,
                                                                           @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Cuenta de recepcionista creada correctamente",
                adminService.createReceptionist(request, currentUser.getId())));
    }

    @PatchMapping("/users/{userId}/role")
    @Operation(summary = "Cambiar el rol de un usuario")
    public ResponseEntity<ApiResponse<UserResponseDTO>> changeRole(@PathVariable UUID userId,
                                                                   @Valid @RequestBody UserRoleUpdateRequest request,
                                                                   @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Rol actualizado correctamente",
                adminService.changeUserRole(userId, request.getRole(), currentUser.getId())));
    }

    @PatchMapping("/users/{userId}/status")
    @Operation(summary = "Activar o desactivar un usuario")
    public ResponseEntity<ApiResponse<UserResponseDTO>> changeStatus(@PathVariable UUID userId,
                                                                     @Valid @RequestBody UserStatusUpdateRequest request,
                                                                     @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Estado del usuario actualizado correctamente",
                adminService.changeUserStatus(userId, request.getActivo(), currentUser.getId())));
    }
}

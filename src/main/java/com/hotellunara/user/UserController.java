package com.hotellunara.user;

import com.hotellunara.common.response.ApiResponse;
import com.hotellunara.user.dto.UserRequestDTO;
import com.hotellunara.user.dto.UserResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestion del perfil del usuario autenticado")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Obtener perfil del usuario autenticado")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getProfile(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Perfil obtenido correctamente",
                userService.getProfile(currentUser.getId())));
    }

    @PatchMapping("/me")
    @Operation(summary = "Actualizar perfil del usuario autenticado")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateProfile(@AuthenticationPrincipal User currentUser,
                                                                      @Valid @RequestBody UserRequestDTO requestDTO) {
        return ResponseEntity.ok(ApiResponse.success("Perfil actualizado correctamente",
                userService.updateProfile(currentUser.getId(), requestDTO)));
    }
}

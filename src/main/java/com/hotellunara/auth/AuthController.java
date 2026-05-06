package com.hotellunara.auth;

import com.hotellunara.auth.dto.AuthResponse;
import com.hotellunara.auth.dto.LoginRequest;
import com.hotellunara.auth.dto.RefreshTokenRequest;
import com.hotellunara.auth.dto.RegisterRequest;
import com.hotellunara.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticacion", description = "Endpoints de registro, login y renovacion de tokens")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Registrar un huesped")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Usuario registrado correctamente", authService.register(request)));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login exitoso", authService.login(request)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refrescar access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Token renovado correctamente", authService.refreshToken(request)));
    }
}

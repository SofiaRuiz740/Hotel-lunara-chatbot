package com.hotellunara.auth;

import com.hotellunara.audit.AuditService;
import com.hotellunara.auth.dto.AuthResponse;
import com.hotellunara.auth.dto.LoginRequest;
import com.hotellunara.auth.dto.RefreshTokenRequest;
import com.hotellunara.auth.dto.RegisterRequest;
import com.hotellunara.common.enums.UserLanguage;
import com.hotellunara.common.enums.UserRole;
import com.hotellunara.common.exception.BusinessRuleException;
import com.hotellunara.common.exception.UnauthorizedException;
import com.hotellunara.user.User;
import com.hotellunara.user.UserMapper;
import com.hotellunara.user.UserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AuditService auditService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        return registerWithRole(request, UserRole.GUEST);
    }

    @Transactional
    public AuthResponse registerWithRole(RegisterRequest request, UserRole role) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessRuleException("Ya existe un usuario con ese email");
        }

        User user = User.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .telefono(request.getTelefono())
                .nacionalidad(request.getNacionalidad())
                .documentoIdentidad(request.getDocumentoIdentidad())
                .idioma(request.getIdioma() == null ? UserLanguage.ES : request.getIdioma())
                .alergias(request.getAlergias())
                .preferenciasCama(request.getPreferenciasCama())
                .peticionesEspeciales(request.getPeticionesEspeciales())
                .role(role)
                .activo(true)
                .emailVerificado(false)
                .build();

        User savedUser = userRepository.save(user);
        auditService.log(savedUser, "REGISTER_USER", "User", savedUser.getId().toString(),
                java.util.Map.of("role", savedUser.getRole().name(), "email", savedUser.getEmail()), null);
        return buildAuthResponse(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword()));
        } catch (BadCredentialsException ex) {
            auditService.log(
                    (java.util.UUID) null,
                    "LOGIN_FAILED",
                    "Auth",
                    normalizedEmail,
                    java.util.Map.of("email", normalizedEmail, "reason", "BAD_CREDENTIALS"),
                    null);
            throw new UnauthorizedException("Credenciales invalidas");
        }

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado"));
        if (!user.isActivo()) {
            throw new UnauthorizedException("El usuario esta inactivo");
        }

        user.setUltimoLogin(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        auditService.log(savedUser, "LOGIN_SUCCESS", "User", savedUser.getId().toString(), null, null);
        return buildAuthResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new UnauthorizedException("El token enviado no es un refresh token");
        }

        String email = jwtUtil.extractUsername(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado"));
        if (!jwtUtil.validateToken(refreshToken, user.getEmail())) {
            throw new UnauthorizedException("Refresh token invalido o expirado");
        }

        return AuthResponse.builder()
                .accessToken(jwtUtil.generateAccessToken(user))
                .refreshToken(jwtUtil.generateRefreshToken(user))
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpiration())
                .user(userMapper.toResponse(user))
                .build();
    }

    private AuthResponse buildAuthResponse(User user) {
        return AuthResponse.builder()
                .accessToken(jwtUtil.generateAccessToken(user))
                .refreshToken(jwtUtil.generateRefreshToken(user))
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpiration())
                .user(userMapper.toResponse(user))
                .build();
    }
}

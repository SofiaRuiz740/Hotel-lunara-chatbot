package com.hotellunara.user.dto;

import com.hotellunara.common.enums.UserLanguage;
import com.hotellunara.common.enums.UserRole;
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
public class UserResponseDTO {

    private UUID id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String nacionalidad;
    private String documentoIdentidad;
    private UserRole role;
    private UserLanguage idioma;
    private String alergias;
    private String preferenciasCama;
    private String peticionesEspeciales;
    private boolean activo;
    private boolean emailVerificado;
    private LocalDateTime fechaRegistro;
    private LocalDateTime ultimoLogin;
}

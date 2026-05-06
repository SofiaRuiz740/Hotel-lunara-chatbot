package com.hotellunara.user.dto;

import com.hotellunara.common.enums.UserLanguage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100)
    private String apellido;

    @Pattern(regexp = "^[0-9+()\\-\\s]{0,40}$", message = "Telefono invalido")
    private String telefono;

    @Size(max = 80)
    private String nacionalidad;

    @Size(max = 80)
    private String documentoIdentidad;

    private UserLanguage idioma;

    @Size(max = 255)
    private String alergias;

    @Size(max = 120)
    private String preferenciasCama;

    @Size(max = 255)
    private String peticionesEspeciales;
}

package com.hotellunara.user;

import com.hotellunara.user.dto.UserRequestDTO;
import com.hotellunara.user.dto.UserResponseDTO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-05T18:09:55-0500",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponseDTO toResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponseDTO.UserResponseDTOBuilder userResponseDTO = UserResponseDTO.builder();

        userResponseDTO.activo( user.isActivo() );
        userResponseDTO.alergias( user.getAlergias() );
        userResponseDTO.apellido( user.getApellido() );
        userResponseDTO.documentoIdentidad( user.getDocumentoIdentidad() );
        userResponseDTO.email( user.getEmail() );
        userResponseDTO.emailVerificado( user.isEmailVerificado() );
        userResponseDTO.fechaRegistro( user.getFechaRegistro() );
        userResponseDTO.id( user.getId() );
        userResponseDTO.idioma( user.getIdioma() );
        userResponseDTO.nacionalidad( user.getNacionalidad() );
        userResponseDTO.nombre( user.getNombre() );
        userResponseDTO.peticionesEspeciales( user.getPeticionesEspeciales() );
        userResponseDTO.preferenciasCama( user.getPreferenciasCama() );
        userResponseDTO.role( user.getRole() );
        userResponseDTO.telefono( user.getTelefono() );
        userResponseDTO.ultimoLogin( user.getUltimoLogin() );

        return userResponseDTO.build();
    }

    @Override
    public User toEntity(UserRequestDTO requestDTO) {
        if ( requestDTO == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.alergias( requestDTO.getAlergias() );
        user.apellido( requestDTO.getApellido() );
        user.documentoIdentidad( requestDTO.getDocumentoIdentidad() );
        user.idioma( requestDTO.getIdioma() );
        user.nacionalidad( requestDTO.getNacionalidad() );
        user.nombre( requestDTO.getNombre() );
        user.peticionesEspeciales( requestDTO.getPeticionesEspeciales() );
        user.preferenciasCama( requestDTO.getPreferenciasCama() );
        user.telefono( requestDTO.getTelefono() );

        return user.build();
    }

    @Override
    public void updateEntity(UserRequestDTO requestDTO, User user) {
        if ( requestDTO == null ) {
            return;
        }

        if ( requestDTO.getAlergias() != null ) {
            user.setAlergias( requestDTO.getAlergias() );
        }
        if ( requestDTO.getApellido() != null ) {
            user.setApellido( requestDTO.getApellido() );
        }
        if ( requestDTO.getDocumentoIdentidad() != null ) {
            user.setDocumentoIdentidad( requestDTO.getDocumentoIdentidad() );
        }
        if ( requestDTO.getIdioma() != null ) {
            user.setIdioma( requestDTO.getIdioma() );
        }
        if ( requestDTO.getNacionalidad() != null ) {
            user.setNacionalidad( requestDTO.getNacionalidad() );
        }
        if ( requestDTO.getNombre() != null ) {
            user.setNombre( requestDTO.getNombre() );
        }
        if ( requestDTO.getPeticionesEspeciales() != null ) {
            user.setPeticionesEspeciales( requestDTO.getPeticionesEspeciales() );
        }
        if ( requestDTO.getPreferenciasCama() != null ) {
            user.setPreferenciasCama( requestDTO.getPreferenciasCama() );
        }
        if ( requestDTO.getTelefono() != null ) {
            user.setTelefono( requestDTO.getTelefono() );
        }
    }
}

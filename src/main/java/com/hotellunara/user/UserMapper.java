package com.hotellunara.user;

import com.hotellunara.user.dto.UserRequestDTO;
import com.hotellunara.user.dto.UserResponseDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDTO toResponse(User user);

    User toEntity(UserRequestDTO requestDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UserRequestDTO requestDTO, @MappingTarget User user);
}

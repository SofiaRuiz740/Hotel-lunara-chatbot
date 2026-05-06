package com.hotellunara.room;

import com.hotellunara.room.dto.RoomRequestDTO;
import com.hotellunara.room.dto.RoomResponseDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    RoomResponseDTO toResponse(Room room);

    Room toEntity(RoomRequestDTO requestDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(RoomRequestDTO requestDTO, @MappingTarget Room room);
}

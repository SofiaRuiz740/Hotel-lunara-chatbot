package com.hotellunara.restaurant;

import com.hotellunara.restaurant.dto.RestaurantTableRequestDTO;
import com.hotellunara.restaurant.dto.RestaurantTableResponseDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface RestaurantTableMapper {

    RestaurantTableResponseDTO toResponse(RestaurantTable table);

    RestaurantTable toEntity(RestaurantTableRequestDTO requestDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(RestaurantTableRequestDTO requestDTO, @MappingTarget RestaurantTable table);
}

package com.hotellunara.hotelservice;

import com.hotellunara.hotelservice.dto.HotelServiceRequestDTO;
import com.hotellunara.hotelservice.dto.HotelServiceResponseDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface HotelServiceMapper {

    HotelServiceResponseDTO toResponse(HotelService service);

    HotelService toEntity(HotelServiceRequestDTO requestDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(HotelServiceRequestDTO requestDTO, @MappingTarget HotelService service);
}

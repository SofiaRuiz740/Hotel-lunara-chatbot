package com.hotellunara.restaurant;

import com.hotellunara.restaurant.dto.RestaurantReservationRequestDTO;
import com.hotellunara.restaurant.dto.RestaurantReservationResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RestaurantReservationMapper {

    @Mapping(target = "guestId", source = "guest.id")
    @Mapping(target = "guestNombreCompleto", expression = "java(entity.getGuest().getNombre() + \" \" + entity.getGuest().getApellido())")
    @Mapping(target = "tableId", source = "table.id")
    @Mapping(target = "tableNumero", source = "table.numero")
    @Mapping(target = "reservationHotelId", source = "reservationHotel.id")
    RestaurantReservationResponseDTO toResponse(RestaurantReservation entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "guest", ignore = true)
    @Mapping(target = "table", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "reservationHotel", ignore = true)
    @Mapping(target = "creadaEn", ignore = true)
    RestaurantReservation toEntity(RestaurantReservationRequestDTO requestDTO);
}

package com.hotellunara.reservation;

import com.hotellunara.reservation.dto.ReservationRequestDTO;
import com.hotellunara.reservation.dto.ReservationResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(target = "guestId", source = "guest.id")
    @Mapping(target = "guestNombreCompleto", expression = "java(reservation.getGuest().getNombre() + \" \" + reservation.getGuest().getApellido())")
    @Mapping(target = "roomId", source = "room.id")
    @Mapping(target = "roomNumero", source = "room.numero")
    @Mapping(target = "roomTipo", expression = "java(reservation.getRoom().getTipo().name())")
    ReservationResponseDTO toResponse(Reservation reservation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigoReserva", ignore = true)
    @Mapping(target = "guest", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "cantidadNoches", ignore = true)
    @Mapping(target = "precioNoche", ignore = true)
    @Mapping(target = "precioTotal", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "motivoCancelacion", ignore = true)
    @Mapping(target = "fechaCancelacion", ignore = true)
    @Mapping(target = "creadaPor", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    Reservation toEntity(ReservationRequestDTO requestDTO);
}

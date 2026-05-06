package com.hotellunara.hotelservice;

import com.hotellunara.hotelservice.dto.ServiceRequestRequestDTO;
import com.hotellunara.hotelservice.dto.ServiceRequestResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ServiceRequestMapper {

    @Mapping(target = "guestId", source = "guest.id")
    @Mapping(target = "guestNombreCompleto", expression = "java(entity.getGuest().getNombre() + \" \" + entity.getGuest().getApellido())")
    @Mapping(target = "reservationId", source = "reservation.id")
    @Mapping(target = "serviceId", source = "service.id")
    @Mapping(target = "serviceNombre", source = "service.nombre")
    @Mapping(target = "serviceCategoria", source = "service.categoria")
    @Mapping(target = "atendidoPorId", source = "atendidoPor.id")
    @Mapping(target = "atendidoPorNombre", expression = "java(entity.getAtendidoPor() == null ? null : entity.getAtendidoPor().getNombre() + \" \" + entity.getAtendidoPor().getApellido())")
    ServiceRequestResponseDTO toResponse(ServiceRequest entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "guest", ignore = true)
    @Mapping(target = "reservation", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "precioAplicado", ignore = true)
    @Mapping(target = "atendidoPor", ignore = true)
    @Mapping(target = "creadaEn", ignore = true)
    ServiceRequest toEntity(ServiceRequestRequestDTO requestDTO);
}

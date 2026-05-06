package com.hotellunara.hotelservice;

import com.hotellunara.hotelservice.dto.ServiceRequestRequestDTO;
import com.hotellunara.hotelservice.dto.ServiceRequestResponseDTO;
import com.hotellunara.reservation.Reservation;
import com.hotellunara.user.User;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-05T18:09:52-0500",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class ServiceRequestMapperImpl implements ServiceRequestMapper {

    @Override
    public ServiceRequestResponseDTO toResponse(ServiceRequest entity) {
        if ( entity == null ) {
            return null;
        }

        ServiceRequestResponseDTO.ServiceRequestResponseDTOBuilder serviceRequestResponseDTO = ServiceRequestResponseDTO.builder();

        serviceRequestResponseDTO.guestId( entityGuestId( entity ) );
        serviceRequestResponseDTO.reservationId( entityReservationId( entity ) );
        serviceRequestResponseDTO.serviceId( entityServiceId( entity ) );
        serviceRequestResponseDTO.serviceNombre( entityServiceNombre( entity ) );
        serviceRequestResponseDTO.serviceCategoria( entityServiceCategoria( entity ) );
        serviceRequestResponseDTO.atendidoPorId( entityAtendidoPorId( entity ) );
        serviceRequestResponseDTO.creadaEn( entity.getCreadaEn() );
        serviceRequestResponseDTO.estado( entity.getEstado() );
        serviceRequestResponseDTO.fechaSolicitada( entity.getFechaSolicitada() );
        serviceRequestResponseDTO.horaSolicitada( entity.getHoraSolicitada() );
        serviceRequestResponseDTO.id( entity.getId() );
        serviceRequestResponseDTO.notas( entity.getNotas() );
        serviceRequestResponseDTO.precioAplicado( entity.getPrecioAplicado() );

        serviceRequestResponseDTO.guestNombreCompleto( entity.getGuest().getNombre() + " " + entity.getGuest().getApellido() );
        serviceRequestResponseDTO.atendidoPorNombre( entity.getAtendidoPor() == null ? null : entity.getAtendidoPor().getNombre() + " " + entity.getAtendidoPor().getApellido() );

        return serviceRequestResponseDTO.build();
    }

    @Override
    public ServiceRequest toEntity(ServiceRequestRequestDTO requestDTO) {
        if ( requestDTO == null ) {
            return null;
        }

        ServiceRequest.ServiceRequestBuilder serviceRequest = ServiceRequest.builder();

        serviceRequest.fechaSolicitada( requestDTO.getFechaSolicitada() );
        serviceRequest.horaSolicitada( requestDTO.getHoraSolicitada() );
        serviceRequest.notas( requestDTO.getNotas() );

        return serviceRequest.build();
    }

    private UUID entityGuestId(ServiceRequest serviceRequest) {
        if ( serviceRequest == null ) {
            return null;
        }
        User guest = serviceRequest.getGuest();
        if ( guest == null ) {
            return null;
        }
        UUID id = guest.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long entityReservationId(ServiceRequest serviceRequest) {
        if ( serviceRequest == null ) {
            return null;
        }
        Reservation reservation = serviceRequest.getReservation();
        if ( reservation == null ) {
            return null;
        }
        Long id = reservation.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long entityServiceId(ServiceRequest serviceRequest) {
        if ( serviceRequest == null ) {
            return null;
        }
        HotelService service = serviceRequest.getService();
        if ( service == null ) {
            return null;
        }
        Long id = service.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityServiceNombre(ServiceRequest serviceRequest) {
        if ( serviceRequest == null ) {
            return null;
        }
        HotelService service = serviceRequest.getService();
        if ( service == null ) {
            return null;
        }
        String nombre = service.getNombre();
        if ( nombre == null ) {
            return null;
        }
        return nombre;
    }

    private String entityServiceCategoria(ServiceRequest serviceRequest) {
        if ( serviceRequest == null ) {
            return null;
        }
        HotelService service = serviceRequest.getService();
        if ( service == null ) {
            return null;
        }
        String categoria = service.getCategoria();
        if ( categoria == null ) {
            return null;
        }
        return categoria;
    }

    private UUID entityAtendidoPorId(ServiceRequest serviceRequest) {
        if ( serviceRequest == null ) {
            return null;
        }
        User atendidoPor = serviceRequest.getAtendidoPor();
        if ( atendidoPor == null ) {
            return null;
        }
        UUID id = atendidoPor.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}

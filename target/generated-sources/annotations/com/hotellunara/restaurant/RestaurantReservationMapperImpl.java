package com.hotellunara.restaurant;

import com.hotellunara.reservation.Reservation;
import com.hotellunara.restaurant.dto.RestaurantReservationRequestDTO;
import com.hotellunara.restaurant.dto.RestaurantReservationResponseDTO;
import com.hotellunara.user.User;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-05T18:09:53-0500",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class RestaurantReservationMapperImpl implements RestaurantReservationMapper {

    @Override
    public RestaurantReservationResponseDTO toResponse(RestaurantReservation entity) {
        if ( entity == null ) {
            return null;
        }

        RestaurantReservationResponseDTO.RestaurantReservationResponseDTOBuilder restaurantReservationResponseDTO = RestaurantReservationResponseDTO.builder();

        restaurantReservationResponseDTO.guestId( entityGuestId( entity ) );
        restaurantReservationResponseDTO.tableId( entityTableId( entity ) );
        restaurantReservationResponseDTO.tableNumero( entityTableNumero( entity ) );
        restaurantReservationResponseDTO.reservationHotelId( entityReservationHotelId( entity ) );
        restaurantReservationResponseDTO.cantidadPersonas( entity.getCantidadPersonas() );
        restaurantReservationResponseDTO.creadaEn( entity.getCreadaEn() );
        restaurantReservationResponseDTO.estado( entity.getEstado() );
        restaurantReservationResponseDTO.fecha( entity.getFecha() );
        restaurantReservationResponseDTO.hora( entity.getHora() );
        restaurantReservationResponseDTO.id( entity.getId() );
        restaurantReservationResponseDTO.ocasionEspecial( entity.getOcasionEspecial() );
        restaurantReservationResponseDTO.peticiones( entity.getPeticiones() );

        restaurantReservationResponseDTO.guestNombreCompleto( entity.getGuest().getNombre() + " " + entity.getGuest().getApellido() );

        return restaurantReservationResponseDTO.build();
    }

    @Override
    public RestaurantReservation toEntity(RestaurantReservationRequestDTO requestDTO) {
        if ( requestDTO == null ) {
            return null;
        }

        RestaurantReservation.RestaurantReservationBuilder restaurantReservation = RestaurantReservation.builder();

        restaurantReservation.cantidadPersonas( requestDTO.getCantidadPersonas() );
        restaurantReservation.fecha( requestDTO.getFecha() );
        restaurantReservation.hora( requestDTO.getHora() );
        restaurantReservation.ocasionEspecial( requestDTO.getOcasionEspecial() );
        restaurantReservation.peticiones( requestDTO.getPeticiones() );

        return restaurantReservation.build();
    }

    private UUID entityGuestId(RestaurantReservation restaurantReservation) {
        if ( restaurantReservation == null ) {
            return null;
        }
        User guest = restaurantReservation.getGuest();
        if ( guest == null ) {
            return null;
        }
        UUID id = guest.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long entityTableId(RestaurantReservation restaurantReservation) {
        if ( restaurantReservation == null ) {
            return null;
        }
        RestaurantTable table = restaurantReservation.getTable();
        if ( table == null ) {
            return null;
        }
        Long id = table.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Integer entityTableNumero(RestaurantReservation restaurantReservation) {
        if ( restaurantReservation == null ) {
            return null;
        }
        RestaurantTable table = restaurantReservation.getTable();
        if ( table == null ) {
            return null;
        }
        int numero = table.getNumero();
        return numero;
    }

    private Long entityReservationHotelId(RestaurantReservation restaurantReservation) {
        if ( restaurantReservation == null ) {
            return null;
        }
        Reservation reservationHotel = restaurantReservation.getReservationHotel();
        if ( reservationHotel == null ) {
            return null;
        }
        Long id = reservationHotel.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}

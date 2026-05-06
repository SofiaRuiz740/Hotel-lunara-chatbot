package com.hotellunara.reservation;

import com.hotellunara.reservation.dto.ReservationRequestDTO;
import com.hotellunara.reservation.dto.ReservationResponseDTO;
import com.hotellunara.room.Room;
import com.hotellunara.user.User;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-05T18:09:54-0500",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class ReservationMapperImpl implements ReservationMapper {

    @Override
    public ReservationResponseDTO toResponse(Reservation reservation) {
        if ( reservation == null ) {
            return null;
        }

        ReservationResponseDTO.ReservationResponseDTOBuilder reservationResponseDTO = ReservationResponseDTO.builder();

        reservationResponseDTO.guestId( reservationGuestId( reservation ) );
        reservationResponseDTO.roomId( reservationRoomId( reservation ) );
        reservationResponseDTO.roomNumero( reservationRoomNumero( reservation ) );
        reservationResponseDTO.cantidadAdultos( reservation.getCantidadAdultos() );
        reservationResponseDTO.cantidadNinos( reservation.getCantidadNinos() );
        reservationResponseDTO.cantidadNoches( reservation.getCantidadNoches() );
        reservationResponseDTO.checkIn( reservation.getCheckIn() );
        reservationResponseDTO.checkOut( reservation.getCheckOut() );
        reservationResponseDTO.codigoReserva( reservation.getCodigoReserva() );
        reservationResponseDTO.creadaPor( reservation.getCreadaPor() );
        reservationResponseDTO.estado( reservation.getEstado() );
        reservationResponseDTO.fechaCancelacion( reservation.getFechaCancelacion() );
        reservationResponseDTO.fechaCreacion( reservation.getFechaCreacion() );
        reservationResponseDTO.id( reservation.getId() );
        reservationResponseDTO.motivoCancelacion( reservation.getMotivoCancelacion() );
        reservationResponseDTO.peticionesEspeciales( reservation.getPeticionesEspeciales() );
        reservationResponseDTO.precioNoche( reservation.getPrecioNoche() );
        reservationResponseDTO.precioTotal( reservation.getPrecioTotal() );

        reservationResponseDTO.guestNombreCompleto( reservation.getGuest().getNombre() + " " + reservation.getGuest().getApellido() );
        reservationResponseDTO.roomTipo( reservation.getRoom().getTipo().name() );

        return reservationResponseDTO.build();
    }

    @Override
    public Reservation toEntity(ReservationRequestDTO requestDTO) {
        if ( requestDTO == null ) {
            return null;
        }

        Reservation.ReservationBuilder reservation = Reservation.builder();

        reservation.cantidadAdultos( requestDTO.getCantidadAdultos() );
        reservation.cantidadNinos( requestDTO.getCantidadNinos() );
        reservation.checkIn( requestDTO.getCheckIn() );
        reservation.checkOut( requestDTO.getCheckOut() );
        reservation.peticionesEspeciales( requestDTO.getPeticionesEspeciales() );

        return reservation.build();
    }

    private UUID reservationGuestId(Reservation reservation) {
        if ( reservation == null ) {
            return null;
        }
        User guest = reservation.getGuest();
        if ( guest == null ) {
            return null;
        }
        UUID id = guest.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long reservationRoomId(Reservation reservation) {
        if ( reservation == null ) {
            return null;
        }
        Room room = reservation.getRoom();
        if ( room == null ) {
            return null;
        }
        Long id = room.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String reservationRoomNumero(Reservation reservation) {
        if ( reservation == null ) {
            return null;
        }
        Room room = reservation.getRoom();
        if ( room == null ) {
            return null;
        }
        String numero = room.getNumero();
        if ( numero == null ) {
            return null;
        }
        return numero;
    }
}

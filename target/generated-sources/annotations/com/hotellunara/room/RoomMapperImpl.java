package com.hotellunara.room;

import com.hotellunara.room.dto.RoomRequestDTO;
import com.hotellunara.room.dto.RoomResponseDTO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-05T18:09:55-0500",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class RoomMapperImpl implements RoomMapper {

    @Override
    public RoomResponseDTO toResponse(Room room) {
        if ( room == null ) {
            return null;
        }

        RoomResponseDTO.RoomResponseDTOBuilder roomResponseDTO = RoomResponseDTO.builder();

        roomResponseDTO.activa( room.isActiva() );
        roomResponseDTO.amenities( room.getAmenities() );
        roomResponseDTO.capacidadAdultos( room.getCapacidadAdultos() );
        roomResponseDTO.capacidadNinos( room.getCapacidadNinos() );
        roomResponseDTO.descripcion( room.getDescripcion() );
        roomResponseDTO.estado( room.getEstado() );
        roomResponseDTO.id( room.getId() );
        roomResponseDTO.imagenes( room.getImagenes() );
        roomResponseDTO.numero( room.getNumero() );
        roomResponseDTO.piso( room.getPiso() );
        roomResponseDTO.precioPorNoche( room.getPrecioPorNoche() );
        roomResponseDTO.tipo( room.getTipo() );

        return roomResponseDTO.build();
    }

    @Override
    public Room toEntity(RoomRequestDTO requestDTO) {
        if ( requestDTO == null ) {
            return null;
        }

        Room.RoomBuilder room = Room.builder();

        if ( requestDTO.getActiva() != null ) {
            room.activa( requestDTO.getActiva() );
        }
        room.amenities( requestDTO.getAmenities() );
        room.capacidadAdultos( requestDTO.getCapacidadAdultos() );
        room.capacidadNinos( requestDTO.getCapacidadNinos() );
        room.descripcion( requestDTO.getDescripcion() );
        room.estado( requestDTO.getEstado() );
        room.imagenes( requestDTO.getImagenes() );
        room.numero( requestDTO.getNumero() );
        room.piso( requestDTO.getPiso() );
        room.precioPorNoche( requestDTO.getPrecioPorNoche() );
        room.tipo( requestDTO.getTipo() );

        return room.build();
    }

    @Override
    public void updateEntity(RoomRequestDTO requestDTO, Room room) {
        if ( requestDTO == null ) {
            return;
        }

        if ( requestDTO.getActiva() != null ) {
            room.setActiva( requestDTO.getActiva() );
        }
        if ( requestDTO.getAmenities() != null ) {
            room.setAmenities( requestDTO.getAmenities() );
        }
        room.setCapacidadAdultos( requestDTO.getCapacidadAdultos() );
        room.setCapacidadNinos( requestDTO.getCapacidadNinos() );
        if ( requestDTO.getDescripcion() != null ) {
            room.setDescripcion( requestDTO.getDescripcion() );
        }
        if ( requestDTO.getEstado() != null ) {
            room.setEstado( requestDTO.getEstado() );
        }
        if ( requestDTO.getImagenes() != null ) {
            room.setImagenes( requestDTO.getImagenes() );
        }
        if ( requestDTO.getNumero() != null ) {
            room.setNumero( requestDTO.getNumero() );
        }
        room.setPiso( requestDTO.getPiso() );
        if ( requestDTO.getPrecioPorNoche() != null ) {
            room.setPrecioPorNoche( requestDTO.getPrecioPorNoche() );
        }
        if ( requestDTO.getTipo() != null ) {
            room.setTipo( requestDTO.getTipo() );
        }
    }
}

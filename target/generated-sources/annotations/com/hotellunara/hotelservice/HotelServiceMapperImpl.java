package com.hotellunara.hotelservice;

import com.hotellunara.hotelservice.dto.HotelServiceRequestDTO;
import com.hotellunara.hotelservice.dto.HotelServiceResponseDTO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-05T18:09:51-0500",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class HotelServiceMapperImpl implements HotelServiceMapper {

    @Override
    public HotelServiceResponseDTO toResponse(HotelService service) {
        if ( service == null ) {
            return null;
        }

        HotelServiceResponseDTO.HotelServiceResponseDTOBuilder hotelServiceResponseDTO = HotelServiceResponseDTO.builder();

        hotelServiceResponseDTO.activo( service.isActivo() );
        hotelServiceResponseDTO.capacidadMaximaPorSlot( service.getCapacidadMaximaPorSlot() );
        hotelServiceResponseDTO.categoria( service.getCategoria() );
        hotelServiceResponseDTO.descripcion( service.getDescripcion() );
        hotelServiceResponseDTO.disponibleParaExternos( service.isDisponibleParaExternos() );
        hotelServiceResponseDTO.duracion( service.getDuracion() );
        hotelServiceResponseDTO.horarioApertura( service.getHorarioApertura() );
        hotelServiceResponseDTO.horarioCierre( service.getHorarioCierre() );
        hotelServiceResponseDTO.id( service.getId() );
        hotelServiceResponseDTO.nombre( service.getNombre() );
        hotelServiceResponseDTO.precio( service.getPrecio() );
        hotelServiceResponseDTO.requiereReserva( service.isRequiereReserva() );

        return hotelServiceResponseDTO.build();
    }

    @Override
    public HotelService toEntity(HotelServiceRequestDTO requestDTO) {
        if ( requestDTO == null ) {
            return null;
        }

        HotelService.HotelServiceBuilder hotelService = HotelService.builder();

        if ( requestDTO.getActivo() != null ) {
            hotelService.activo( requestDTO.getActivo() );
        }
        hotelService.capacidadMaximaPorSlot( requestDTO.getCapacidadMaximaPorSlot() );
        hotelService.categoria( requestDTO.getCategoria() );
        hotelService.descripcion( requestDTO.getDescripcion() );
        if ( requestDTO.getDisponibleParaExternos() != null ) {
            hotelService.disponibleParaExternos( requestDTO.getDisponibleParaExternos() );
        }
        hotelService.duracion( requestDTO.getDuracion() );
        hotelService.horarioApertura( requestDTO.getHorarioApertura() );
        hotelService.horarioCierre( requestDTO.getHorarioCierre() );
        hotelService.nombre( requestDTO.getNombre() );
        hotelService.precio( requestDTO.getPrecio() );
        if ( requestDTO.getRequiereReserva() != null ) {
            hotelService.requiereReserva( requestDTO.getRequiereReserva() );
        }

        return hotelService.build();
    }

    @Override
    public void updateEntity(HotelServiceRequestDTO requestDTO, HotelService service) {
        if ( requestDTO == null ) {
            return;
        }

        if ( requestDTO.getActivo() != null ) {
            service.setActivo( requestDTO.getActivo() );
        }
        service.setCapacidadMaximaPorSlot( requestDTO.getCapacidadMaximaPorSlot() );
        if ( requestDTO.getCategoria() != null ) {
            service.setCategoria( requestDTO.getCategoria() );
        }
        if ( requestDTO.getDescripcion() != null ) {
            service.setDescripcion( requestDTO.getDescripcion() );
        }
        if ( requestDTO.getDisponibleParaExternos() != null ) {
            service.setDisponibleParaExternos( requestDTO.getDisponibleParaExternos() );
        }
        service.setDuracion( requestDTO.getDuracion() );
        if ( requestDTO.getHorarioApertura() != null ) {
            service.setHorarioApertura( requestDTO.getHorarioApertura() );
        }
        if ( requestDTO.getHorarioCierre() != null ) {
            service.setHorarioCierre( requestDTO.getHorarioCierre() );
        }
        if ( requestDTO.getNombre() != null ) {
            service.setNombre( requestDTO.getNombre() );
        }
        if ( requestDTO.getPrecio() != null ) {
            service.setPrecio( requestDTO.getPrecio() );
        }
        if ( requestDTO.getRequiereReserva() != null ) {
            service.setRequiereReserva( requestDTO.getRequiereReserva() );
        }
    }
}

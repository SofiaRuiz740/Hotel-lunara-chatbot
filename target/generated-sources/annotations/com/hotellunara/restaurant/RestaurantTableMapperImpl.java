package com.hotellunara.restaurant;

import com.hotellunara.restaurant.dto.RestaurantTableRequestDTO;
import com.hotellunara.restaurant.dto.RestaurantTableResponseDTO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-05T18:09:54-0500",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class RestaurantTableMapperImpl implements RestaurantTableMapper {

    @Override
    public RestaurantTableResponseDTO toResponse(RestaurantTable table) {
        if ( table == null ) {
            return null;
        }

        RestaurantTableResponseDTO.RestaurantTableResponseDTOBuilder restaurantTableResponseDTO = RestaurantTableResponseDTO.builder();

        restaurantTableResponseDTO.activa( table.isActiva() );
        restaurantTableResponseDTO.capacidad( table.getCapacidad() );
        restaurantTableResponseDTO.estado( table.getEstado() );
        restaurantTableResponseDTO.id( table.getId() );
        restaurantTableResponseDTO.numero( table.getNumero() );
        restaurantTableResponseDTO.ubicacion( table.getUbicacion() );

        return restaurantTableResponseDTO.build();
    }

    @Override
    public RestaurantTable toEntity(RestaurantTableRequestDTO requestDTO) {
        if ( requestDTO == null ) {
            return null;
        }

        RestaurantTable.RestaurantTableBuilder restaurantTable = RestaurantTable.builder();

        if ( requestDTO.getActiva() != null ) {
            restaurantTable.activa( requestDTO.getActiva() );
        }
        restaurantTable.capacidad( requestDTO.getCapacidad() );
        restaurantTable.estado( requestDTO.getEstado() );
        restaurantTable.numero( requestDTO.getNumero() );
        restaurantTable.ubicacion( requestDTO.getUbicacion() );

        return restaurantTable.build();
    }

    @Override
    public void updateEntity(RestaurantTableRequestDTO requestDTO, RestaurantTable table) {
        if ( requestDTO == null ) {
            return;
        }

        if ( requestDTO.getActiva() != null ) {
            table.setActiva( requestDTO.getActiva() );
        }
        table.setCapacidad( requestDTO.getCapacidad() );
        if ( requestDTO.getEstado() != null ) {
            table.setEstado( requestDTO.getEstado() );
        }
        table.setNumero( requestDTO.getNumero() );
        if ( requestDTO.getUbicacion() != null ) {
            table.setUbicacion( requestDTO.getUbicacion() );
        }
    }
}

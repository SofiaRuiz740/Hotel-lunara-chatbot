package com.hotellunara.common.hotel;

import com.hotellunara.hotelservice.HotelServiceRepository;
import com.hotellunara.room.RoomRepository;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HotelInfoService {

    private final RoomRepository roomRepository;
    private final HotelServiceRepository hotelServiceRepository;

    @Value("${app.hotel.name}")
    private String hotelName;

    @Value("${app.hotel.stars}")
    private int stars;

    @Value("${app.hotel.floors}")
    private int floors;

    @Value("${app.hotel.total-rooms}")
    private int totalRooms;

    @Value("${app.hotel.description}")
    private String description;

    @Value("${app.hotel.address}")
    private String address;

    @Value("${app.hotel.phone}")
    private String phone;

    @Value("${app.hotel.email}")
    private String email;

    @Value("${app.hotel.check-in-time}")
    private String checkInTime;

    @Value("${app.hotel.check-out-time}")
    private String checkOutTime;

    @Value("${app.hotel.cancellation-policy}")
    private String cancellationPolicy;

    @Value("${app.hotel.images}")
    private String imagesProperty;

    @Transactional(readOnly = true)
    public HotelInfoResponse getHotelInfo() {
        Map<String, Long> roomSummary = new LinkedHashMap<>();
        roomRepository.findByActivaTrueOrderByPisoAscNumeroAsc().forEach(room ->
                roomSummary.merge(room.getTipo().name(), 1L, Long::sum));

        return HotelInfoResponse.builder()
                .nombre(hotelName)
                .estrellas(stars)
                .pisos(floors)
                .totalHabitaciones(totalRooms)
                .descripcion(description)
                .direccion(address)
                .telefono(phone)
                .email(email)
                .horarioCheckIn(checkInTime)
                .horarioCheckOut(checkOutTime)
                .politicaCancelacion(cancellationPolicy)
                .imagenes(Arrays.stream(imagesProperty.split(",")).map(String::trim).filter(value -> !value.isBlank()).toList())
                .horariosRestaurante(Map.of(
                        "desayuno", "07:00-10:30",
                        "almuerzo", "12:30-15:00",
                        "cena", "19:00-23:00"))
                .serviciosDestacados(hotelServiceRepository.findByActivoTrueOrderByNombreAsc()
                        .stream()
                        .map(service -> service.getNombre() + " - $" + service.getPrecio())
                        .toList())
                .habitacionesPorTipo(roomSummary)
                .build();
    }
}

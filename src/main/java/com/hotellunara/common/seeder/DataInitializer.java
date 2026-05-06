package com.hotellunara.common.seeder;

import com.hotellunara.common.enums.RestaurantTableLocation;
import com.hotellunara.common.enums.RestaurantTableStatus;
import com.hotellunara.common.enums.RoomStatus;
import com.hotellunara.common.enums.RoomType;
import com.hotellunara.common.enums.UserLanguage;
import com.hotellunara.common.enums.UserRole;
import com.hotellunara.hotelservice.HotelService;
import com.hotellunara.hotelservice.HotelServiceRepository;
import com.hotellunara.restaurant.RestaurantTable;
import com.hotellunara.restaurant.RestaurantTableRepository;
import com.hotellunara.room.Room;
import com.hotellunara.room.RoomRepository;
import com.hotellunara.user.User;
import com.hotellunara.user.UserRepository;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final HotelServiceRepository hotelServiceRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0 || roomRepository.count() > 0 || restaurantTableRepository.count() > 0
                || hotelServiceRepository.count() > 0) {
            return;
        }

        seedUsers();
        seedRooms();
        seedRestaurantTables();
        seedHotelServices();
    }

    private void seedUsers() {
        List<User> users = List.of(
                buildUser("Admin", "Lunara", "admin@hotellunara.com", "Admin1234!", UserRole.ADMIN),
                buildUser("Recepcion", "Lunara", "recepcion@hotellunara.com", "Recep1234!", UserRole.RECEPTIONIST),
                buildUser("Huesped", "Demo", "huesped@hotellunara.com", "Guest1234!", UserRole.GUEST));
        userRepository.saveAll(users);
    }

    private User buildUser(String nombre, String apellido, String email, String password, UserRole role) {
        return User.builder()
                .nombre(nombre)
                .apellido(apellido)
                .email(email)
                .password(passwordEncoder.encode(password))
                .telefono("+1 555-000-0000")
                .nacionalidad("CO")
                .documentoIdentidad("DOC-" + role.name())
                .role(role)
                .idioma(UserLanguage.ES)
                .activo(true)
                .emailVerificado(true)
                .build();
    }

    private void seedRooms() {
        List<Room> rooms = List.of(
                room("101", 1, RoomType.SIMPLE, 1, 1, "80.00"),
                room("102", 1, RoomType.SIMPLE, 1, 1, "95.00"),
                room("203", 2, RoomType.DOBLE, 2, 1, "125.00"),
                room("204", 2, RoomType.DOBLE, 2, 2, "140.00"),
                room("305", 3, RoomType.SUITE, 2, 2, "220.00"),
                room("306", 3, RoomType.SUITE, 3, 2, "255.00"),
                room("407", 4, RoomType.DOBLE, 2, 2, "150.00"),
                room("508", 5, RoomType.SUITE, 2, 2, "280.00"),
                room("709", 7, RoomType.PENTHOUSE, 4, 2, "420.00"),
                room("801", 8, RoomType.PENTHOUSE, 4, 3, "495.00"));
        roomRepository.saveAll(rooms);
    }

    private Room room(String numero, int piso, RoomType tipo, int adultos, int ninos, String precio) {
        return Room.builder()
                .numero(numero)
                .piso(piso)
                .tipo(tipo)
                .capacidadAdultos(adultos)
                .capacidadNinos(ninos)
                .precioPorNoche(new BigDecimal(precio))
                .descripcion("Habitacion " + tipo.name() + " con vista preferencial y acabados premium.")
                .amenities("WiFi,Smart TV,Aire acondicionado,Minibar")
                .estado(RoomStatus.DISPONIBLE)
                .imagenes("https://example.com/rooms/" + numero + "-1.jpg,https://example.com/rooms/" + numero + "-2.jpg")
                .activa(true)
                .build();
    }

    private void seedRestaurantTables() {
        List<RestaurantTable> tables = List.of(
                table(1, 2, RestaurantTableLocation.INTERIOR), table(2, 2, RestaurantTableLocation.INTERIOR),
                table(3, 2, RestaurantTableLocation.TERRAZA), table(4, 2, RestaurantTableLocation.BAR),
                table(5, 4, RestaurantTableLocation.INTERIOR), table(6, 4, RestaurantTableLocation.INTERIOR),
                table(7, 4, RestaurantTableLocation.INTERIOR), table(8, 4, RestaurantTableLocation.TERRAZA),
                table(9, 4, RestaurantTableLocation.TERRAZA), table(10, 4, RestaurantTableLocation.BAR),
                table(11, 6, RestaurantTableLocation.INTERIOR), table(12, 6, RestaurantTableLocation.INTERIOR),
                table(13, 6, RestaurantTableLocation.TERRAZA), table(14, 6, RestaurantTableLocation.BAR),
                table(15, 2, RestaurantTableLocation.INTERIOR), table(16, 2, RestaurantTableLocation.TERRAZA),
                table(17, 4, RestaurantTableLocation.BAR), table(18, 6, RestaurantTableLocation.INTERIOR),
                table(19, 4, RestaurantTableLocation.TERRAZA), table(20, 2, RestaurantTableLocation.BAR));
        restaurantTableRepository.saveAll(tables);
    }

    private RestaurantTable table(int numero, int capacidad, RestaurantTableLocation ubicacion) {
        return RestaurantTable.builder()
                .numero(numero)
                .capacidad(capacidad)
                .ubicacion(ubicacion)
                .estado(RestaurantTableStatus.LIBRE)
                .activa(true)
                .build();
    }

    private void seedHotelServices() {
        List<HotelService> services = List.of(
                service("Spa", "Circuito de relajacion y bienestar", "SPA", "45.00", 60, "09:00", "20:00", false, false, 4),
                service("Masaje", "Masaje terapeutico individual", "MASAJE", "60.00", 60, "09:00", "19:00", false, false, 1),
                service("Tour Ciudad", "Recorrido guiado por los puntos principales de la ciudad", "TOUR_CIUDAD", "25.00", 300, "09:00", "09:00", false, true, 15),
                service("Tour Playa", "Excursion de dia completo a playa privada", "TOUR_PLAYA", "35.00", 540, "08:00", "08:00", false, true, 12),
                service("Traslado Aeropuerto", "Traslado entre el hotel y el aeropuerto", "TRASLADO_AEROPUERTO", "20.00", 60, "00:00", "23:59", true, true, 10),
                service("Room Service", "Entrega de alimentos y bebidas en la habitacion", "ROOM_SERVICE", "0.00", 0, "06:00", "23:59", false, false, 50),
                service("Lavanderia", "Servicio de lavado y planchado por prenda", "LAVANDERIA", "5.00", 1440, "00:00", "23:59", false, false, 100));
        hotelServiceRepository.saveAll(services);
    }

    private HotelService service(String nombre, String descripcion, String categoria, String precio, int duracion,
                                 String apertura, String cierre, boolean requiereReserva,
                                 boolean disponibleParaExternos, int capacidad) {
        return HotelService.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .categoria(categoria)
                .precio(new BigDecimal(precio))
                .duracion(duracion)
                .horarioApertura(LocalTime.parse(apertura))
                .horarioCierre(LocalTime.parse(cierre))
                .requiereReserva(requiereReserva)
                .disponibleParaExternos(disponibleParaExternos)
                .capacidadMaximaPorSlot(capacidad)
                .activo(true)
                .build();
    }
}

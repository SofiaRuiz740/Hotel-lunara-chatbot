package com.hotellunara.reservation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hotellunara.audit.AuditService;
import com.hotellunara.common.enums.ReservationCreatedBy;
import com.hotellunara.common.enums.ReservationStatus;
import com.hotellunara.common.enums.RoomStatus;
import com.hotellunara.common.enums.RoomType;
import com.hotellunara.common.enums.UserRole;
import com.hotellunara.common.exception.BusinessRuleException;
import com.hotellunara.hotelservice.ServiceRequestMapper;
import com.hotellunara.hotelservice.ServiceRequestRepository;
import com.hotellunara.reservation.dto.ReservationRequestDTO;
import com.hotellunara.reservation.dto.ReservationResponseDTO;
import com.hotellunara.restaurant.RestaurantRepository;
import com.hotellunara.restaurant.RestaurantReservationMapper;
import com.hotellunara.room.Room;
import com.hotellunara.room.RoomRepository;
import com.hotellunara.user.User;
import com.hotellunara.user.UserRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private ReservationMapper reservationMapper;
    @Mock
    private AuditService auditService;
    @Mock
    private ServiceRequestRepository serviceRequestRepository;
    @Mock
    private ServiceRequestMapper serviceRequestMapper;
    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private RestaurantReservationMapper restaurantReservationMapper;
    @Mock
    private Clock clock;

    @InjectMocks
    private ReservationService reservationService;

    private UUID userId;
    private User guest;
    private Room room;

    @BeforeEach
    void setUp() {
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(Instant.parse("2026-01-10T10:00:00Z"));
        ReflectionTestUtils.setField(reservationService, "checkInTimeConfig", "15:00");
        ReflectionTestUtils.setField(reservationService, "roomCancellationHours", 48L);
        userId = UUID.randomUUID();
        guest = User.builder()
                .id(userId)
                .nombre("Ana")
                .apellido("Lopez")
                .email("ana@hotellunara.com")
                .role(UserRole.GUEST)
                .activo(true)
                .password("encoded")
                .build();

        room = Room.builder()
                .id(1L)
                .numero("304")
                .piso(3)
                .tipo(RoomType.DOBLE)
                .capacidadAdultos(2)
                .capacidadNinos(1)
                .precioPorNoche(BigDecimal.valueOf(150))
                .estado(RoomStatus.DISPONIBLE)
                .activa(true)
                .build();
    }

    @Test
    void testCrearReservaExitosa() {
        ReservationRequestDTO request = ReservationRequestDTO.builder()
                .roomId(room.getId())
                .checkIn(LocalDate.now().plusDays(2))
                .checkOut(LocalDate.now().plusDays(5))
                .cantidadAdultos(2)
                .cantidadNinos(1)
                .peticionesEspeciales("Late arrival")
                .build();

        Reservation mapped = Reservation.builder().build();
        Reservation saved = Reservation.builder()
                .id(10L)
                .codigoReserva("LUN-" + request.getCheckIn().getYear() + "-000010")
                .guest(guest)
                .room(room)
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .cantidadNoches(3)
                .cantidadAdultos(2)
                .cantidadNinos(1)
                .precioNoche(BigDecimal.valueOf(150))
                .precioTotal(BigDecimal.valueOf(450))
                .estado(ReservationStatus.CONFIRMADA)
                .creadaPor(ReservationCreatedBy.GUEST)
                .fechaCreacion(LocalDateTime.now())
                .build();

        ReservationResponseDTO response = ReservationResponseDTO.builder()
                .id(saved.getId())
                .codigoReserva(saved.getCodigoReserva())
                .estado(saved.getEstado())
                .precioTotal(saved.getPrecioTotal())
                .cantidadNoches(saved.getCantidadNoches())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(guest));
        when(roomRepository.findByIdForUpdate(room.getId())).thenReturn(Optional.of(room));
        when(reservationRepository.existsOverlappingReservation(any(), any(), any(), any())).thenReturn(false);
        when(reservationMapper.toEntity(request)).thenReturn(mapped);
        when(reservationRepository.saveAndFlush(any(Reservation.class))).thenReturn(saved);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(saved);
        when(reservationMapper.toResponse(saved)).thenReturn(response);

        ReservationResponseDTO result = reservationService.createReservation(request, userId);

        assertEquals("LUN-" + request.getCheckIn().getYear() + "-000010", result.getCodigoReserva());
        assertEquals(BigDecimal.valueOf(450), result.getPrecioTotal());
        assertEquals(3, result.getCantidadNoches());
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void testCrearReservaHabitacionNoDisponible() {
        ReservationRequestDTO request = ReservationRequestDTO.builder()
                .roomId(room.getId())
                .checkIn(LocalDate.now().plusDays(3))
                .checkOut(LocalDate.now().plusDays(6))
                .cantidadAdultos(2)
                .cantidadNinos(0)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(guest));
        when(roomRepository.findByIdForUpdate(room.getId())).thenReturn(Optional.of(room));
        when(reservationRepository.existsOverlappingReservation(any(), any(), any(), any())).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> reservationService.createReservation(request, userId));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void testCancelarReservaConMasDe48h() {
        Reservation reservation = Reservation.builder()
                .id(20L)
                .codigoReserva("LUN-2026-0002")
                .guest(guest)
                .room(room)
                .checkIn(LocalDate.of(2026, 1, 14))
                .checkOut(LocalDate.of(2026, 1, 16))
                .estado(ReservationStatus.CONFIRMADA)
                .build();

        ReservationResponseDTO response = ReservationResponseDTO.builder()
                .id(20L)
                .estado(ReservationStatus.CANCELADA)
                .motivoCancelacion("Cambio de plan")
                .build();

        when(reservationRepository.findById(20L)).thenReturn(Optional.of(reservation));
        when(userRepository.findById(userId)).thenReturn(Optional.of(guest));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reservationMapper.toResponse(any(Reservation.class))).thenReturn(response);

        ReservationResponseDTO result = reservationService.cancelReservation(20L, userId, "Cambio de plan");

        assertEquals(ReservationStatus.CANCELADA, result.getEstado());
        assertEquals(RoomStatus.DISPONIBLE, reservation.getRoom().getEstado());
        verify(auditService).log(any(User.class), any(), any(), any(), any(), any());
    }

    @Test
    void testCancelarReservaConMenos48hLanzaException() {
        Reservation reservation = Reservation.builder()
                .id(21L)
                .guest(guest)
                .room(room)
                .checkIn(LocalDate.of(2026, 1, 11))
                .checkOut(LocalDate.of(2026, 1, 13))
                .estado(ReservationStatus.CONFIRMADA)
                .build();

        when(reservationRepository.findById(21L)).thenReturn(Optional.of(reservation));
        when(userRepository.findById(userId)).thenReturn(Optional.of(guest));

        assertThrows(BusinessRuleException.class,
                () -> reservationService.cancelReservation(21L, userId, "Urgencia"));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void testCheckinFueraDeRangoFechaLanzaException() {
        Reservation reservation = Reservation.builder()
                .id(22L)
                .guest(guest)
                .room(room)
                .checkIn(LocalDate.now().plusDays(5))
                .checkOut(LocalDate.now().plusDays(7))
                .estado(ReservationStatus.CONFIRMADA)
                .build();

        when(reservationRepository.findById(22L)).thenReturn(Optional.of(reservation));

        assertThrows(BusinessRuleException.class, () -> reservationService.makeCheckin(22L, userId));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }
}

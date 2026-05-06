package com.hotellunara.reservation;

import com.hotellunara.audit.AuditService;
import com.hotellunara.common.enums.ReservationCreatedBy;
import com.hotellunara.common.enums.ReservationStatus;
import com.hotellunara.common.enums.UserRole;
import com.hotellunara.common.debug.DebugProbe;
import com.hotellunara.common.exception.BusinessRuleException;
import com.hotellunara.common.exception.ResourceNotFoundException;
import com.hotellunara.hotelservice.ServiceRequestMapper;
import com.hotellunara.hotelservice.ServiceRequestRepository;
import com.hotellunara.reservation.dto.CheckoutSummaryResponse;
import com.hotellunara.reservation.dto.ReservationRequestDTO;
import com.hotellunara.reservation.dto.ReservationResponseDTO;
import com.hotellunara.reservation.dto.TodayOperationsResponse;
import com.hotellunara.restaurant.RestaurantRepository;
import com.hotellunara.restaurant.RestaurantReservationMapper;
import com.hotellunara.common.enums.RoomStatus;
import com.hotellunara.room.Room;
import com.hotellunara.room.RoomRepository;
import com.hotellunara.user.User;
import com.hotellunara.user.UserRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final ReservationMapper reservationMapper;
    private final AuditService auditService;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceRequestMapper serviceRequestMapper;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantReservationMapper restaurantReservationMapper;
    private final Clock clock;

    @Value("${app.hotel.check-in-time:15:00}")
    private String checkInTimeConfig;

    @Value("${app.policy.room-cancellation-hours:48}")
    private long roomCancellationHours;

    @Transactional
    public ReservationResponseDTO createReservation(ReservationRequestDTO requestDTO, UUID userId) {
        LocalDate tomorrow = LocalDate.now(clock).plusDays(1);
        if (requestDTO.getCheckIn().isBefore(tomorrow)) {
            throw new BusinessRuleException("El check-in debe ser al menos a partir de manana");
        }
        if (!requestDTO.getCheckOut().isAfter(requestDTO.getCheckIn())) {
            throw new BusinessRuleException("La fecha de check-out debe ser posterior al check-in");
        }

        int cantidadNoches = (int) ChronoUnit.DAYS.between(requestDTO.getCheckIn(), requestDTO.getCheckOut());
        if (cantidadNoches > 30) {
            throw new BusinessRuleException("La reserva no puede exceder 30 noches");
        }

        User guest = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Room room = roomRepository.findByIdForUpdate(requestDTO.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Habitacion no encontrada"));

        if (!room.isActiva() || room.getEstado() == RoomStatus.MANTENIMIENTO) {
            throw new BusinessRuleException("La habitacion no esta disponible para reservar");
        }
        if (requestDTO.getCantidadAdultos() > room.getCapacidadAdultos()
                || requestDTO.getCantidadNinos() > room.getCapacidadNinos()) {
            throw new BusinessRuleException("La capacidad de la habitacion es insuficiente");
        }
        if (reservationRepository.existsOverlappingReservation(room.getId(), requestDTO.getCheckIn(),
                requestDTO.getCheckOut(), Set.of(ReservationStatus.CONFIRMADA, ReservationStatus.ACTIVA))) {
            throw new BusinessRuleException("La habitacion no esta disponible para las fechas solicitadas");
        }

        BigDecimal precioNoche = room.getPrecioPorNoche();
        BigDecimal precioTotal = precioNoche.multiply(BigDecimal.valueOf(cantidadNoches));
        String codigoReservaTemporal = "TMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        // #region agent log
        DebugProbe.log(
                "baseline",
                "H1",
                "ReservationService.createReservation",
                "Reservation code generated from repository count",
                Map.of(
                        "userId", userId.toString(),
                        "roomId", room.getId(),
                        "generatedCode", codigoReservaTemporal,
                        "checkIn", requestDTO.getCheckIn().toString(),
                        "checkOut", requestDTO.getCheckOut().toString()
                )
        );
        // #endregion

        Reservation reservation = reservationMapper.toEntity(requestDTO);
        reservation.setCodigoReserva(codigoReservaTemporal);
        reservation.setGuest(guest);
        reservation.setRoom(room);
        reservation.setCantidadNoches(cantidadNoches);
        reservation.setPrecioNoche(precioNoche);
        reservation.setPrecioTotal(precioTotal);
        reservation.setEstado(ReservationStatus.CONFIRMADA);
        reservation.setCreadaPor(guest.getRole() == UserRole.GUEST ? ReservationCreatedBy.GUEST : ReservationCreatedBy.RECEPTIONIST);

        Reservation savedReservation = reservationRepository.saveAndFlush(reservation);
        savedReservation.setCodigoReserva(buildReservationCode(savedReservation));
        savedReservation = reservationRepository.save(savedReservation);
        auditService.log(guest, "CREATE_RESERVATION", "Reservation", savedReservation.getId().toString(),
                java.util.Map.of("codigoReserva", savedReservation.getCodigoReserva(), "roomId", room.getId()), null);
        return reservationMapper.toResponse(savedReservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getMyReservations(UUID userId) {
        return reservationRepository.findByGuestIdOrderByFechaCreacionDesc(userId)
                .stream()
                .map(reservationMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getAllReservations() {
        return reservationRepository.findAllByOrderByFechaCreacionDesc(PageRequest.of(0, 100))
                .stream()
                .map(reservationMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getAllReservations(ReservationStatus status,
                                                            LocalDate checkInFrom,
                                                            LocalDate checkInTo,
                                                            String guestQuery) {
        String normalizedGuestQuery = guestQuery == null ? null : guestQuery.trim().toLowerCase();
        return reservationRepository.findAll()
                .stream()
                .filter(reservation -> status == null || reservation.getEstado() == status)
                .filter(reservation -> checkInFrom == null || !reservation.getCheckIn().isBefore(checkInFrom))
                .filter(reservation -> checkInTo == null || !reservation.getCheckIn().isAfter(checkInTo))
                .filter(reservation -> normalizedGuestQuery == null || normalizedGuestQuery.isBlank()
                        || reservation.getGuest().getEmail().toLowerCase().contains(normalizedGuestQuery)
                        || (reservation.getGuest().getNombre() + " " + reservation.getGuest().getApellido())
                        .toLowerCase().contains(normalizedGuestQuery)
                        || reservation.getCodigoReserva().toLowerCase().contains(normalizedGuestQuery))
                .sorted(Comparator.comparing(Reservation::getFechaCreacion).reversed())
                .map(reservationMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReservationResponseDTO getReservationById(Long reservationId, UUID userId) {
        Reservation reservation = getReservation(reservationId);
        User actor = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (actor.getRole() == UserRole.GUEST && !reservation.getGuest().getId().equals(userId)) {
            throw new AccessDeniedException("No puedes ver esta reserva");
        }
        return reservationMapper.toResponse(reservation);
    }

    @Transactional
    public ReservationResponseDTO cancelReservation(Long reservationId, UUID userId, String motivo) {
        Reservation reservation = getReservation(reservationId);
        User actor = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        boolean isStaff = actor.getRole() == UserRole.RECEPTIONIST || actor.getRole() == UserRole.ADMIN;
        if (!reservation.getGuest().getId().equals(userId) && !isStaff) {
            throw new AccessDeniedException("No puedes cancelar esta reserva");
        }
        if (reservation.getEstado() != ReservationStatus.CONFIRMADA) {
            throw new BusinessRuleException("Solo se pueden cancelar reservas en estado CONFIRMADA");
        }
        if (!isStaff) {
            LocalDateTime checkInDateTime = LocalDateTime.of(reservation.getCheckIn(), resolveOperationalCheckInTime());
            LocalDateTime cancellationCutoff = LocalDateTime.now(clock).plusHours(roomCancellationHours);
            if (checkInDateTime.isBefore(cancellationCutoff)) {
            // #region agent log
            DebugProbe.log(
                    "baseline",
                    "H2",
                    "ReservationService.cancelReservation",
                    "Guest cancellation blocked by 48h policy",
                    Map.of(
                            "reservationId", reservationId,
                            "checkInAtOperationalHour", checkInDateTime.toString(),
                            "cutoff", cancellationCutoff.toString(),
                            "isStaff", isStaff
                    )
            );
            // #endregion
            throw new BusinessRuleException("Cancelacion tardia: menos de " + roomCancellationHours + "h antes del check-in");
            }
        }

        reservation.setEstado(ReservationStatus.CANCELADA);
        reservation.setMotivoCancelacion(motivo);
        reservation.setFechaCancelacion(LocalDateTime.now(clock));
        reservation.getRoom().setEstado(RoomStatus.DISPONIBLE);

        Reservation savedReservation = reservationRepository.save(reservation);
        roomRepository.save(savedReservation.getRoom());
        auditService.log(actor, "CANCEL_RESERVATION", "Reservation", savedReservation.getId().toString(),
                java.util.Map.of("motivo", motivo, "codigoReserva", savedReservation.getCodigoReserva()), null);
        return reservationMapper.toResponse(savedReservation);
    }

    @Transactional
    public ReservationResponseDTO makeCheckin(Long reservationId, UUID actorId) {
        Reservation reservation = getReservation(reservationId);
        if (reservation.getEstado() != ReservationStatus.CONFIRMADA) {
            throw new BusinessRuleException("Solo se puede hacer check-in a una reserva confirmada");
        }

        LocalDate today = LocalDate.now(clock);
        LocalDate earliest = reservation.getCheckIn().minusDays(1);
        LocalDate latest = reservation.getCheckIn().plusDays(1);
        if (today.isBefore(earliest) || today.isAfter(latest)) {
            throw new BusinessRuleException("El check-in solo puede realizarse entre un dia antes y un dia despues de la fecha prevista");
        }

        reservation.setEstado(ReservationStatus.ACTIVA);
        reservation.getRoom().setEstado(RoomStatus.OCUPADA);
        Reservation savedReservation = reservationRepository.save(reservation);
        roomRepository.save(savedReservation.getRoom());
        auditService.log(actorId, "CHECKIN_RESERVATION", "Reservation", savedReservation.getId().toString(),
                savedReservation.getCodigoReserva(), null);
        return reservationMapper.toResponse(savedReservation);
    }

    @Transactional
    public CheckoutSummaryResponse makeCheckout(Long reservationId, UUID actorId) {
        Reservation reservation = getReservation(reservationId);
        if (reservation.getEstado() != ReservationStatus.ACTIVA) {
            throw new BusinessRuleException("Solo se puede hacer check-out a una reserva activa");
        }

        reservation.setEstado(ReservationStatus.COMPLETADA);
        reservation.getRoom().setEstado(RoomStatus.LIMPIEZA);
        Reservation savedReservation = reservationRepository.save(reservation);
        roomRepository.save(savedReservation.getRoom());

        List<com.hotellunara.hotelservice.dto.ServiceRequestResponseDTO> servicios = serviceRequestRepository
                .findByReservationIdOrderByFechaSolicitadaAscHoraSolicitadaAsc(savedReservation.getId())
                .stream()
                .map(serviceRequestMapper::toResponse)
                .toList();

        List<com.hotellunara.restaurant.dto.RestaurantReservationResponseDTO> restaurante = restaurantRepository
                .findByFechaBetweenAndGuestIdOrderByFechaAscHoraAsc(
                        savedReservation.getCheckIn(),
                        savedReservation.getCheckOut().minusDays(1),
                        savedReservation.getGuest().getId())
                .stream()
                .map(restaurantReservationMapper::toResponse)
                .toList();

        auditService.log(actorId, "CHECKOUT_RESERVATION", "Reservation", savedReservation.getId().toString(),
                savedReservation.getCodigoReserva(), null);

        return CheckoutSummaryResponse.builder()
                .reserva(reservationMapper.toResponse(savedReservation))
                .serviciosConsumidos(servicios)
                .reservasRestaurante(restaurante)
                .build();
    }

    @Transactional(readOnly = true)
    public TodayOperationsResponse getTodayOperations() {
        LocalDate today = LocalDate.now(clock);
        return TodayOperationsResponse.builder()
                .checkInsHoy(reservationRepository.findByCheckInOrderByFechaCreacionDesc(today)
                        .stream()
                        .map(reservationMapper::toResponse)
                        .toList())
                .checkOutsHoy(reservationRepository.findByCheckOutOrderByFechaCreacionDesc(today)
                        .stream()
                        .map(reservationMapper::toResponse)
                        .toList())
                .reservasActivas(reservationRepository.findByEstadoOrderByCheckInAsc(ReservationStatus.ACTIVA)
                        .stream()
                        .map(reservationMapper::toResponse)
                        .toList())
                .build();
    }

    private String buildReservationCode(Reservation reservation) {
        int year = reservation.getCheckIn() != null ? reservation.getCheckIn().getYear() : LocalDate.now(clock).getYear();
        return "LUN-" + year + "-" + String.format("%06d", reservation.getId());
    }

    private LocalTime resolveOperationalCheckInTime() {
        return LocalTime.parse(checkInTimeConfig);
    }

    @Transactional(readOnly = true)
    public Reservation getReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
    }
}

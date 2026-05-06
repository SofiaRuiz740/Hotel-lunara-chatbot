package com.hotellunara.restaurant;

import com.hotellunara.audit.AuditService;
import com.hotellunara.common.debug.DebugProbe;
import com.hotellunara.common.enums.RestaurantReservationStatus;
import com.hotellunara.common.enums.UserRole;
import com.hotellunara.common.exception.BusinessRuleException;
import com.hotellunara.common.exception.ResourceNotFoundException;
import com.hotellunara.reservation.Reservation;
import com.hotellunara.reservation.ReservationRepository;
import com.hotellunara.restaurant.dto.RestaurantAvailabilityResponse;
import com.hotellunara.restaurant.dto.RestaurantReservationRequestDTO;
import com.hotellunara.restaurant.dto.RestaurantReservationResponseDTO;
import com.hotellunara.user.User;
import com.hotellunara.user.UserRepository;
import java.time.Duration;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final RestaurantReservationMapper restaurantReservationMapper;
    private final RestaurantTableMapper restaurantTableMapper;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final AuditService auditService;
    private final Clock clock;

    @Value("${app.policy.restaurant-cancellation-hours:2}")
    private long restaurantCancellationHours;
    @Value("${app.policy.restaurant.breakfast-start:07:00}")
    private String breakfastStart;
    @Value("${app.policy.restaurant.breakfast-end:10:30}")
    private String breakfastEnd;
    @Value("${app.policy.restaurant.lunch-start:12:30}")
    private String lunchStart;
    @Value("${app.policy.restaurant.lunch-end:15:00}")
    private String lunchEnd;
    @Value("${app.policy.restaurant.dinner-start:19:00}")
    private String dinnerStart;
    @Value("${app.policy.restaurant.dinner-end:23:00}")
    private String dinnerEnd;

    @Transactional(readOnly = true)
    public RestaurantAvailabilityResponse checkAvailability(LocalDate fecha, LocalTime hora, int personas) {
        validateReservationDateTime(fecha, hora);
        validateRestaurantSchedule(hora);
        List<RestaurantTable> availableTables = findAvailableTables(fecha, hora, personas);
        return RestaurantAvailabilityResponse.builder()
                .fecha(fecha)
                .hora(hora)
                .cantidadPersonas(personas)
                .mesasDisponibles(availableTables.stream().map(restaurantTableMapper::toResponse).toList())
                .build();
    }

    @Transactional
    public RestaurantReservationResponseDTO createReservation(RestaurantReservationRequestDTO requestDTO, UUID userId) {
        validateReservationDateTime(requestDTO.getFecha(), requestDTO.getHora());
        validateRestaurantSchedule(requestDTO.getHora());
        User guest = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<RestaurantTable> availableTables = findAvailableTables(
                requestDTO.getFecha(), requestDTO.getHora(), requestDTO.getCantidadPersonas());
        RestaurantTable assignedTable = assignTableWithLock(
                availableTables, requestDTO.getFecha(), requestDTO.getHora(), requestDTO.getCantidadPersonas());
        // #region agent log
        DebugProbe.log(
                "baseline",
                "H3",
                "RestaurantService.createReservation",
                "Restaurant table assigned for reservation",
                Map.of(
                        "userId", userId.toString(),
                        "fecha", requestDTO.getFecha().toString(),
                        "hora", requestDTO.getHora().toString(),
                        "personas", requestDTO.getCantidadPersonas(),
                        "availableCount", availableTables.size(),
                        "assignedTableId", assignedTable.getId(),
                        "assignedTableNumber", assignedTable.getNumero()
                )
        );
        // #endregion

        Reservation hotelReservation = null;
        if (requestDTO.getReservationHotelId() != null) {
            hotelReservation = reservationRepository.findById(requestDTO.getReservationHotelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reserva de hotel no encontrada"));
            if (!hotelReservation.getGuest().getId().equals(userId)) {
                throw new AccessDeniedException("La reserva de hotel no pertenece al usuario");
            }
            if (requestDTO.getFecha().isBefore(hotelReservation.getCheckIn())
                    || !requestDTO.getFecha().isBefore(hotelReservation.getCheckOut())) {
                throw new BusinessRuleException("La fecha de la reserva de restaurante debe estar dentro de la estadia");
            }
        }

        RestaurantReservation reservation = restaurantReservationMapper.toEntity(requestDTO);
        reservation.setGuest(guest);
        reservation.setTable(assignedTable);
        reservation.setReservationHotel(hotelReservation);
        reservation.setEstado(RestaurantReservationStatus.CONFIRMADA);

        RestaurantReservation saved = restaurantRepository.save(reservation);
        auditService.log(guest, "CREATE_RESTAURANT_RESERVATION", "RestaurantReservation", saved.getId().toString(),
                java.util.Map.of("tableId", assignedTable.getId(), "fecha", saved.getFecha().toString(), "hora", saved.getHora().toString()), null);
        return restaurantReservationMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<RestaurantReservationResponseDTO> getMyReservations(UUID userId) {
        return restaurantRepository.findByGuestIdOrderByFechaDescHoraDesc(userId)
                .stream()
                .map(restaurantReservationMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RestaurantReservationResponseDTO> getAllReservations(LocalDate fecha,
                                                                     RestaurantReservationStatus status,
                                                                     String guestQuery) {
        String normalizedGuestQuery = guestQuery == null ? null : guestQuery.trim().toLowerCase();
        return restaurantRepository.findAllByOrderByFechaDescHoraDesc()
                .stream()
                .filter(reservation -> fecha == null || reservation.getFecha().equals(fecha))
                .filter(reservation -> status == null || reservation.getEstado() == status)
                .filter(reservation -> normalizedGuestQuery == null || normalizedGuestQuery.isBlank()
                        || reservation.getGuest().getEmail().toLowerCase().contains(normalizedGuestQuery)
                        || (reservation.getGuest().getNombre() + " " + reservation.getGuest().getApellido())
                        .toLowerCase().contains(normalizedGuestQuery)
                        || String.valueOf(reservation.getTable().getNumero()).contains(normalizedGuestQuery))
                .map(restaurantReservationMapper::toResponse)
                .toList();
    }

    @Transactional
    public RestaurantReservationResponseDTO cancelReservation(Long reservationId, UUID userId) {
        RestaurantReservation reservation = getReservationEntity(reservationId);
        User actor = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        boolean isStaff = actor.getRole() == UserRole.RECEPTIONIST || actor.getRole() == UserRole.ADMIN;
        if (!reservation.getGuest().getId().equals(userId) && !isStaff) {
            throw new AccessDeniedException("No puedes cancelar esta reserva de restaurante");
        }
        if (reservation.getEstado() == RestaurantReservationStatus.CANCELADA) {
            throw new BusinessRuleException("La reserva ya esta cancelada");
        }
        if (reservation.getEstado() == RestaurantReservationStatus.COMPLETADA
                || reservation.getEstado() == RestaurantReservationStatus.NO_SHOW) {
            throw new BusinessRuleException("No se puede cancelar una reserva ya finalizada");
        }
        if (!isStaff) {
            LocalDateTime reservationDateTime = LocalDateTime.of(reservation.getFecha(), reservation.getHora());
            if (reservationDateTime.isBefore(LocalDateTime.now(clock).plusHours(restaurantCancellationHours))) {
                throw new BusinessRuleException(
                        "Cancelacion tardia: menos de " + restaurantCancellationHours + " horas antes de la reserva");
            }
        }

        reservation.setEstado(RestaurantReservationStatus.CANCELADA);
        RestaurantReservation saved = restaurantRepository.save(reservation);
        auditService.log(actor, "CANCEL_RESTAURANT_RESERVATION", "RestaurantReservation", saved.getId().toString(), null, null);
        return restaurantReservationMapper.toResponse(saved);
    }

    @Transactional
    public RestaurantReservationResponseDTO updateReservationStatus(Long reservationId,
                                                                   RestaurantReservationStatus status,
                                                                   UUID actorId) {
        RestaurantReservation reservation = getReservationEntity(reservationId);
        validateStatusTransition(reservation.getEstado(), status);
        reservation.setEstado(status);
        RestaurantReservation saved = restaurantRepository.save(reservation);
        auditService.log(actorId, "UPDATE_RESTAURANT_RESERVATION_STATUS", "RestaurantReservation",
                saved.getId().toString(), status.name(), null);
        return restaurantReservationMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public RestaurantReservation getReservationEntity(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva de restaurante no encontrada"));
    }

    @Transactional(readOnly = true)
    public List<RestaurantTable> findAvailableTables(LocalDate fecha, LocalTime hora, int personas) {
        List<RestaurantTable> candidateTables =
                restaurantTableRepository.findByActivaTrueAndCapacidadGreaterThanEqualOrderByCapacidadAscNumeroAsc(personas);
        List<RestaurantReservation> confirmedReservations =
                restaurantRepository.findByFechaAndEstado(fecha, RestaurantReservationStatus.CONFIRMADA);
        // #region agent log
        DebugProbe.log(
                "baseline",
                "H4",
                "RestaurantService.findAvailableTables",
                "Computed restaurant availability inputs",
                Map.of(
                        "fecha", fecha.toString(),
                        "hora", hora.toString(),
                        "personas", personas,
                        "candidateTables", candidateTables.size(),
                        "confirmedReservations", confirmedReservations.size()
                )
        );
        // #endregion

        return candidateTables.stream()
                .filter(table -> confirmedReservations.stream().noneMatch(existing ->
                        existing.getTable().getId().equals(table.getId())
                                && Math.abs(Duration.between(existing.getHora(), hora).toMinutes()) <= 90))
                .toList();
    }

    private void validateRestaurantSchedule(LocalTime hora) {
        boolean breakfast = !hora.isBefore(LocalTime.parse(breakfastStart)) && !hora.isAfter(LocalTime.parse(breakfastEnd));
        boolean lunch = !hora.isBefore(LocalTime.parse(lunchStart)) && !hora.isAfter(LocalTime.parse(lunchEnd));
        boolean dinner = !hora.isBefore(LocalTime.parse(dinnerStart)) && !hora.isAfter(LocalTime.parse(dinnerEnd));
        if (!(breakfast || lunch || dinner)) {
            throw new BusinessRuleException("La hora solicitada esta fuera del horario valido del restaurante");
        }
    }

    private void validateReservationDateTime(LocalDate fecha, LocalTime hora) {
        LocalDateTime requestedDateTime = LocalDateTime.of(fecha, hora);
        if (requestedDateTime.isBefore(LocalDateTime.now(clock))) {
            throw new BusinessRuleException("No se pueden crear o consultar reservas de restaurante en el pasado");
        }
    }

    private RestaurantTable assignTableWithLock(List<RestaurantTable> availableTables,
                                                LocalDate fecha,
                                                LocalTime hora,
                                                int personas) {
        if (availableTables.isEmpty()) {
            throw new BusinessRuleException("No hay mesas disponibles para la fecha y hora solicitadas");
        }
        LocalTime windowStart = hora.minusMinutes(90);
        LocalTime windowEnd = hora.plusMinutes(90);
        List<RestaurantTable> ordered = availableTables.stream()
                .sorted(Comparator.comparingInt((RestaurantTable table) -> table.getCapacidad() - personas)
                        .thenComparingInt(RestaurantTable::getNumero))
                .toList();
        for (RestaurantTable candidate : ordered) {
            RestaurantTable lockedTable = restaurantTableRepository.findByIdForUpdate(candidate.getId())
                    .orElse(null);
            if (lockedTable == null) {
                continue;
            }
            boolean hasConflict = restaurantRepository.existsTableConflict(
                    lockedTable.getId(),
                    fecha,
                    windowStart,
                    windowEnd,
                    java.util.Set.of(RestaurantReservationStatus.CONFIRMADA));
            if (!hasConflict) {
                return lockedTable;
            }
        }
        throw new BusinessRuleException("No hay mesas disponibles para la fecha y hora solicitadas");
    }

    private void validateStatusTransition(RestaurantReservationStatus currentStatus,
                                          RestaurantReservationStatus newStatus) {
        boolean currentIsFinal = currentStatus == RestaurantReservationStatus.CANCELADA
                || currentStatus == RestaurantReservationStatus.COMPLETADA
                || currentStatus == RestaurantReservationStatus.NO_SHOW;
        if (currentIsFinal && currentStatus != newStatus) {
            throw new BusinessRuleException("No se puede modificar una reserva de restaurante en estado final");
        }
    }
}

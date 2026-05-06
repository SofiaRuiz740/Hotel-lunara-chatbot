package com.hotellunara.hotelservice;

import com.hotellunara.audit.AuditService;
import com.hotellunara.common.enums.ReservationStatus;
import com.hotellunara.common.enums.ServiceRequestStatus;
import com.hotellunara.common.debug.DebugProbe;
import com.hotellunara.common.exception.BusinessRuleException;
import com.hotellunara.common.exception.ResourceNotFoundException;
import com.hotellunara.hotelservice.dto.ServiceRequestRequestDTO;
import com.hotellunara.hotelservice.dto.ServiceRequestResponseDTO;
import com.hotellunara.reservation.Reservation;
import com.hotellunara.reservation.ReservationRepository;
import com.hotellunara.user.User;
import com.hotellunara.user.UserRepository;
import java.time.LocalDate;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final HotelServiceRepository hotelServiceRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ServiceRequestMapper serviceRequestMapper;
    private final AuditService auditService;
    private final Clock clock;

    @Transactional
    public ServiceRequestResponseDTO requestService(ServiceRequestRequestDTO requestDTO, UUID userId) {
        HotelService service = hotelServiceRepository.findByIdAndActivoTrueForUpdate(requestDTO.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));
        User guest = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (requestDTO.getHoraSolicitada().isBefore(service.getHorarioApertura())
                || requestDTO.getHoraSolicitada().isAfter(service.getHorarioCierre())) {
            throw new BusinessRuleException("La hora solicitada esta fuera del horario del servicio");
        }
        if (LocalDateTime.of(requestDTO.getFechaSolicitada(), requestDTO.getHoraSolicitada()).isBefore(LocalDateTime.now(clock))) {
            throw new BusinessRuleException("No se pueden solicitar servicios en un horario pasado");
        }

        Reservation reservation = null;
        List<Reservation> activeReservations = reservationRepository.findActiveReservationsOnDate(
                userId, requestDTO.getFechaSolicitada());

        boolean mandatoryActiveReservation = service.isRequiereReserva() || requiresStay(service.getCategoria());
        // #region agent log
        DebugProbe.log(
                "baseline",
                "H5",
                "ServiceRequestService.requestService",
                "Service request reservation requirements evaluated",
                Map.of(
                        "userId", userId.toString(),
                        "serviceId", service.getId(),
                        "serviceCategory", String.valueOf(service.getCategoria()),
                        "requiresReservationFlag", service.isRequiereReserva(),
                        "mandatoryActiveReservation", mandatoryActiveReservation,
                        "activeReservationsFound", activeReservations.size(),
                        "reservationIdProvided", requestDTO.getReservationId() != null
                )
        );
        // #endregion
        if (mandatoryActiveReservation) {
            if (activeReservations.isEmpty()) {
                throw new BusinessRuleException("El servicio requiere una reserva activa del hotel en esa fecha");
            }
            reservation = activeReservations.get(0);
        }

        if (requestDTO.getReservationId() != null) {
            Reservation requestedReservation = reservationRepository.findById(requestDTO.getReservationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
            if (!requestedReservation.getGuest().getId().equals(userId)) {
                throw new AccessDeniedException("La reserva no pertenece al usuario");
            }
            if (requestedReservation.getEstado() != ReservationStatus.ACTIVA
                    && mandatoryActiveReservation) {
                throw new BusinessRuleException("La reserva asociada debe estar activa");
            }
            reservation = requestedReservation;
        }

        long occupiedCapacity = serviceRequestRepository.countByServiceIdAndFechaSolicitadaAndHoraSolicitadaAndEstadoIn(
                service.getId(),
                requestDTO.getFechaSolicitada(),
                requestDTO.getHoraSolicitada(),
                Set.of(ServiceRequestStatus.CONFIRMADO, ServiceRequestStatus.EN_PROCESO));
        if (occupiedCapacity >= service.getCapacidadMaximaPorSlot()) {
            throw new BusinessRuleException("No hay capacidad disponible para el horario solicitado");
        }

        ServiceRequest serviceRequest = serviceRequestMapper.toEntity(requestDTO);
        serviceRequest.setGuest(guest);
        serviceRequest.setReservation(reservation);
        serviceRequest.setService(service);
        serviceRequest.setPrecioAplicado(service.getPrecio());
        serviceRequest.setEstado(ServiceRequestStatus.PENDIENTE);

        ServiceRequest saved = serviceRequestRepository.save(serviceRequest);
        auditService.log(guest, "CREATE_SERVICE_REQUEST", "ServiceRequest", saved.getId().toString(),
                java.util.Map.of("serviceId", service.getId(), "fecha", saved.getFechaSolicitada().toString(), "hora", saved.getHoraSolicitada().toString()), null);
        return serviceRequestMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ServiceRequestResponseDTO> getMyRequests(UUID userId) {
        return serviceRequestRepository.findByGuestIdOrderByCreadaEnDesc(userId)
                .stream()
                .map(serviceRequestMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceRequestResponseDTO> getAllRequests(ServiceRequestStatus status,
                                                          LocalDate fecha,
                                                          String guestQuery) {
        String normalizedGuestQuery = guestQuery == null ? null : guestQuery.trim().toLowerCase();
        return serviceRequestRepository.findAll()
                .stream()
                .filter(request -> status == null || request.getEstado() == status)
                .filter(request -> fecha == null || request.getFechaSolicitada().equals(fecha))
                .filter(request -> normalizedGuestQuery == null || normalizedGuestQuery.isBlank()
                        || request.getGuest().getEmail().toLowerCase().contains(normalizedGuestQuery)
                        || (request.getGuest().getNombre() + " " + request.getGuest().getApellido())
                        .toLowerCase().contains(normalizedGuestQuery)
                        || request.getService().getNombre().toLowerCase().contains(normalizedGuestQuery))
                .sorted(Comparator.comparing(ServiceRequest::getCreadaEn).reversed())
                .map(serviceRequestMapper::toResponse)
                .toList();
    }

    @Transactional
    public ServiceRequestResponseDTO updateStatus(Long requestId, ServiceRequestStatus status, UUID actorId) {
        ServiceRequest request = getRequestEntity(requestId);
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        validateStatusTransition(request.getEstado(), status);

        if (status == ServiceRequestStatus.CONFIRMADO || status == ServiceRequestStatus.EN_PROCESO) {
            HotelService lockedService = hotelServiceRepository.findByIdAndActivoTrueForUpdate(request.getService().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));
            long occupiedCapacity = serviceRequestRepository.countByServiceIdAndFechaSolicitadaAndHoraSolicitadaAndEstadoInAndIdNot(
                    lockedService.getId(),
                    request.getFechaSolicitada(),
                    request.getHoraSolicitada(),
                    Set.of(ServiceRequestStatus.CONFIRMADO, ServiceRequestStatus.EN_PROCESO),
                    request.getId());
            if (occupiedCapacity >= lockedService.getCapacidadMaximaPorSlot()) {
                throw new BusinessRuleException("No hay capacidad disponible para confirmar la solicitud");
            }
        }

        request.setEstado(status);
        request.setAtendidoPor(actor);
        ServiceRequest saved = serviceRequestRepository.save(request);
        auditService.log(actor, "UPDATE_SERVICE_REQUEST_STATUS", "ServiceRequest", saved.getId().toString(), status.name(), null);
        return serviceRequestMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ServiceRequest getRequestEntity(Long requestId) {
        return serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud de servicio no encontrada"));
    }

    private boolean requiresStay(String category) {
        if (category == null) {
            return false;
        }
        String normalized = category.toUpperCase(Locale.ROOT).replace(" ", "_");
        return normalized.contains("ROOM_SERVICE") || normalized.contains("LAVANDERIA");
    }

    private void validateStatusTransition(ServiceRequestStatus currentStatus, ServiceRequestStatus newStatus) {
        boolean currentIsFinal = currentStatus == ServiceRequestStatus.COMPLETADO || currentStatus == ServiceRequestStatus.CANCELADO;
        if (currentIsFinal && currentStatus != newStatus) {
            throw new BusinessRuleException("No se puede modificar una solicitud en estado final");
        }
    }
}

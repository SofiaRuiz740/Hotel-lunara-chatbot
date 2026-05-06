package com.hotellunara.hotelservice;

import com.hotellunara.audit.AuditService;
import com.hotellunara.common.enums.ServiceRequestStatus;
import com.hotellunara.common.exception.BusinessRuleException;
import com.hotellunara.common.exception.ResourceNotFoundException;
import com.hotellunara.hotelservice.dto.HotelServiceRequestDTO;
import com.hotellunara.hotelservice.dto.HotelServiceResponseDTO;
import com.hotellunara.hotelservice.dto.ServiceAvailabilityResponse;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HotelServiceService {

    private final HotelServiceRepository hotelServiceRepository;
    private final HotelServiceMapper hotelServiceMapper;
    private final ServiceRequestRepository serviceRequestRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<HotelServiceResponseDTO> getActiveServices() {
        return hotelServiceRepository.findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(hotelServiceMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public HotelServiceResponseDTO getServiceById(Long id) {
        return hotelServiceMapper.toResponse(getActiveServiceEntity(id));
    }

    @Transactional(readOnly = true)
    public ServiceAvailabilityResponse getAvailability(Long serviceId, LocalDate fecha, LocalTime hora) {
        HotelService service = getActiveServiceEntity(serviceId);
        if (LocalDateTime.of(fecha, hora).isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("No se puede consultar disponibilidad de servicios en un horario pasado");
        }
        validateServiceTime(service, hora);
        long occupied = serviceRequestRepository.countByServiceIdAndFechaSolicitadaAndHoraSolicitadaAndEstadoIn(
                serviceId, fecha, hora, Set.of(ServiceRequestStatus.CONFIRMADO, ServiceRequestStatus.EN_PROCESO));
        long remaining = service.getCapacidadMaximaPorSlot() - occupied;
        return ServiceAvailabilityResponse.builder()
                .serviceId(service.getId())
                .serviceNombre(service.getNombre())
                .fecha(fecha)
                .hora(hora)
                .capacidadMaxima(service.getCapacidadMaximaPorSlot())
                .reservasConfirmadas(occupied)
                .cuposDisponibles(Math.max(remaining, 0))
                .disponible(remaining > 0)
                .build();
    }

    @Transactional
    public HotelServiceResponseDTO createService(HotelServiceRequestDTO requestDTO, UUID actorId) {
        HotelService service = hotelServiceMapper.toEntity(requestDTO);
        validateServiceWindow(service.getHorarioApertura(), service.getHorarioCierre());
        HotelService saved = hotelServiceRepository.save(service);
        auditService.log(actorId, "CREATE_HOTEL_SERVICE", "HotelService", saved.getId().toString(),
                buildServiceAuditDetails(null, saved), null);
        return hotelServiceMapper.toResponse(saved);
    }

    @Transactional
    public HotelServiceResponseDTO updateService(Long id, HotelServiceRequestDTO requestDTO, UUID actorId) {
        HotelService service = getServiceEntity(id);
        java.util.Map<String, Object> before = buildServiceSnapshot(service);
        hotelServiceMapper.updateEntity(requestDTO, service);
        validateServiceWindow(service.getHorarioApertura(), service.getHorarioCierre());
        HotelService saved = hotelServiceRepository.save(service);
        auditService.log(actorId, "UPDATE_HOTEL_SERVICE", "HotelService", saved.getId().toString(),
                buildServiceAuditDetails(before, saved), null);
        return hotelServiceMapper.toResponse(saved);
    }

    @Transactional
    public HotelServiceResponseDTO changeStatus(Long id, boolean active, UUID actorId) {
        HotelService service = getServiceEntity(id);
        service.setActivo(active);
        HotelService saved = hotelServiceRepository.save(service);
        auditService.log(actorId, "CHANGE_HOTEL_SERVICE_STATUS", "HotelService", saved.getId().toString(), active, null);
        return hotelServiceMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public HotelService getServiceEntity(Long id) {
        return hotelServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));
    }

    @Transactional(readOnly = true)
    public HotelService getActiveServiceEntity(Long id) {
        return hotelServiceRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));
    }

    private void validateServiceTime(HotelService service, LocalTime requestedTime) {
        if (requestedTime.isBefore(service.getHorarioApertura()) || requestedTime.isAfter(service.getHorarioCierre())) {
            throw new BusinessRuleException("La hora solicitada esta fuera del horario del servicio");
        }
    }

    private void validateServiceWindow(LocalTime apertura, LocalTime cierre) {
        if (apertura == null || cierre == null || apertura.isAfter(cierre)) {
            throw new BusinessRuleException("La ventana horaria del servicio es invalida");
        }
    }

    private java.util.Map<String, Object> buildServiceAuditDetails(java.util.Map<String, Object> before, HotelService after) {
        java.util.Map<String, Object> details = new java.util.LinkedHashMap<>();
        if (before != null) {
            details.put("before", before);
        }
        details.put("after", buildServiceSnapshot(after));
        return details;
    }

    private java.util.Map<String, Object> buildServiceSnapshot(HotelService service) {
        java.util.Map<String, Object> snapshot = new java.util.LinkedHashMap<>();
        snapshot.put("nombre", service.getNombre());
        snapshot.put("categoria", service.getCategoria());
        snapshot.put("precio", service.getPrecio());
        snapshot.put("duracion", service.getDuracion());
        snapshot.put("horarioApertura", service.getHorarioApertura());
        snapshot.put("horarioCierre", service.getHorarioCierre());
        snapshot.put("requiereReserva", service.isRequiereReserva());
        snapshot.put("disponibleParaExternos", service.isDisponibleParaExternos());
        snapshot.put("capacidadMaximaPorSlot", service.getCapacidadMaximaPorSlot());
        snapshot.put("activo", service.isActivo());
        return snapshot;
    }
}

package com.hotellunara.hotelservice;

import com.hotellunara.common.response.ApiResponse;
import com.hotellunara.hotelservice.dto.HotelServiceRequestDTO;
import com.hotellunara.hotelservice.dto.HotelServiceResponseDTO;
import com.hotellunara.hotelservice.dto.ServiceAvailabilityResponse;
import com.hotellunara.hotelservice.dto.ServiceRequestRequestDTO;
import com.hotellunara.hotelservice.dto.ServiceRequestResponseDTO;
import com.hotellunara.hotelservice.dto.ServiceRequestStatusUpdateRequest;
import com.hotellunara.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Servicios", description = "Catalogo de servicios del hotel y solicitudes")
public class ServiceController {

    private final HotelServiceService hotelServiceService;
    private final ServiceRequestService serviceRequestService;

    @GetMapping("/services")
    @Operation(summary = "Listar servicios activos")
    public ResponseEntity<ApiResponse<List<HotelServiceResponseDTO>>> getServices() {
        return ResponseEntity.ok(ApiResponse.success("Servicios obtenidos correctamente", hotelServiceService.getActiveServices()));
    }

    @GetMapping("/services/{id}")
    @Operation(summary = "Obtener un servicio por id")
    public ResponseEntity<ApiResponse<HotelServiceResponseDTO>> getService(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Servicio obtenido correctamente", hotelServiceService.getServiceById(id)));
    }

    @GetMapping("/services/{id}/availability")
    @Operation(summary = "Consultar disponibilidad de un servicio")
    public ResponseEntity<ApiResponse<ServiceAvailabilityResponse>> getServiceAvailability(@PathVariable Long id,
                                                                                           @RequestParam LocalDate fecha,
                                                                                           @RequestParam LocalTime hora) {
        return ResponseEntity.ok(ApiResponse.success("Disponibilidad del servicio consultada correctamente",
                hotelServiceService.getAvailability(id, fecha, hora)));
    }

    @PostMapping("/services")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear un servicio del hotel")
    public ResponseEntity<ApiResponse<HotelServiceResponseDTO>> createService(@Valid @RequestBody HotelServiceRequestDTO requestDTO,
                                                                              @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Servicio creado correctamente",
                hotelServiceService.createService(requestDTO, currentUser.getId())));
    }

    @PutMapping("/services/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar un servicio del hotel")
    public ResponseEntity<ApiResponse<HotelServiceResponseDTO>> updateService(@PathVariable Long id,
                                                                              @Valid @RequestBody HotelServiceRequestDTO requestDTO,
                                                                              @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Servicio actualizado correctamente",
                hotelServiceService.updateService(id, requestDTO, currentUser.getId())));
    }

    @PatchMapping("/services/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activar o desactivar un servicio del hotel")
    public ResponseEntity<ApiResponse<HotelServiceResponseDTO>> changeServiceStatus(@PathVariable Long id,
                                                                                    @RequestParam boolean active,
                                                                                    @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Estado del servicio actualizado correctamente",
                hotelServiceService.changeStatus(id, active, currentUser.getId())));
    }

    @PostMapping("/service-requests")
    @Operation(summary = "Solicitar un servicio del hotel")
    public ResponseEntity<ApiResponse<ServiceRequestResponseDTO>> requestService(@Valid @RequestBody ServiceRequestRequestDTO requestDTO,
                                                                                 @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Solicitud de servicio creada correctamente",
                serviceRequestService.requestService(requestDTO, currentUser.getId())));
    }

    @GetMapping("/service-requests/me")
    @Operation(summary = "Listar mis solicitudes de servicio")
    public ResponseEntity<ApiResponse<List<ServiceRequestResponseDTO>>> getMyRequests(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Solicitudes obtenidas correctamente",
                serviceRequestService.getMyRequests(currentUser.getId())));
    }

    @GetMapping("/service-requests")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN')")
    @Operation(summary = "Listar todas las solicitudes de servicio")
    public ResponseEntity<ApiResponse<List<ServiceRequestResponseDTO>>> getAllRequests(
            @RequestParam(required = false) com.hotellunara.common.enums.ServiceRequestStatus status,
            @RequestParam(required = false) LocalDate fecha,
            @RequestParam(required = false) String guestQuery) {
        return ResponseEntity.ok(ApiResponse.success("Solicitudes obtenidas correctamente",
                serviceRequestService.getAllRequests(status, fecha, guestQuery)));
    }

    @PatchMapping("/service-requests/{id}/status")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN')")
    @Operation(summary = "Actualizar estado de una solicitud de servicio")
    public ResponseEntity<ApiResponse<ServiceRequestResponseDTO>> updateRequestStatus(@PathVariable Long id,
                                                                                      @Valid @RequestBody ServiceRequestStatusUpdateRequest request,
                                                                                      @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Estado de la solicitud actualizado correctamente",
                serviceRequestService.updateStatus(id, request.getEstado(), currentUser.getId())));
    }
}

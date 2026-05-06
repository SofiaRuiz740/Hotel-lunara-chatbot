package com.hotellunara.restaurant;

import com.hotellunara.common.response.ApiResponse;
import com.hotellunara.common.enums.RestaurantReservationStatus;
import com.hotellunara.restaurant.dto.RestaurantAvailabilityResponse;
import com.hotellunara.restaurant.dto.RestaurantReservationRequestDTO;
import com.hotellunara.restaurant.dto.RestaurantReservationResponseDTO;
import com.hotellunara.restaurant.dto.RestaurantReservationStatusUpdateRequest;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restaurant")
@RequiredArgsConstructor
@Tag(name = "Restaurante", description = "Disponibilidad y reservas del restaurante")
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping("/availability")
    @Operation(summary = "Consultar disponibilidad de mesas")
    public ResponseEntity<ApiResponse<RestaurantAvailabilityResponse>> checkAvailability(@RequestParam LocalDate fecha,
                                                                                         @RequestParam LocalTime hora,
                                                                                         @RequestParam int personas) {
        return ResponseEntity.ok(ApiResponse.success("Disponibilidad consultada correctamente",
                restaurantService.checkAvailability(fecha, hora, personas)));
    }

    @PostMapping("/reservations")
    @Operation(summary = "Crear una reserva de restaurante")
    public ResponseEntity<ApiResponse<RestaurantReservationResponseDTO>> createReservation(
            @Valid @RequestBody RestaurantReservationRequestDTO requestDTO,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Reserva de restaurante creada correctamente",
                restaurantService.createReservation(requestDTO, currentUser.getId())));
    }

    @GetMapping("/reservations/me")
    @Operation(summary = "Listar mis reservas de restaurante")
    public ResponseEntity<ApiResponse<List<RestaurantReservationResponseDTO>>> getMyReservations(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Reservas de restaurante obtenidas correctamente",
                restaurantService.getMyReservations(currentUser.getId())));
    }

    @GetMapping("/reservations")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN')")
    @Operation(summary = "Listar todas las reservas de restaurante")
    public ResponseEntity<ApiResponse<List<RestaurantReservationResponseDTO>>> getAllReservations(
            @RequestParam(required = false) LocalDate fecha,
            @RequestParam(required = false) RestaurantReservationStatus status,
            @RequestParam(required = false) String guestQuery) {
        return ResponseEntity.ok(ApiResponse.success("Reservas de restaurante obtenidas correctamente",
                restaurantService.getAllReservations(fecha, status, guestQuery)));
    }

    @PatchMapping("/reservations/{id}/cancel")
    @Operation(summary = "Cancelar una reserva de restaurante")
    public ResponseEntity<ApiResponse<RestaurantReservationResponseDTO>> cancelReservation(@PathVariable Long id,
                                                                                           @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Reserva de restaurante cancelada correctamente",
                restaurantService.cancelReservation(id, currentUser.getId())));
    }

    @PatchMapping("/reservations/{id}/status")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN')")
    @Operation(summary = "Actualizar estado de una reserva de restaurante")
    public ResponseEntity<ApiResponse<RestaurantReservationResponseDTO>> updateStatus(@PathVariable Long id,
                                                                                      @Valid @RequestBody RestaurantReservationStatusUpdateRequest request,
                                                                                      @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Estado de la reserva actualizado correctamente",
                restaurantService.updateReservationStatus(id, request.getEstado(), currentUser.getId())));
    }
}

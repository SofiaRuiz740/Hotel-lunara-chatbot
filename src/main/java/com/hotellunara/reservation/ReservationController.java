package com.hotellunara.reservation;

import com.hotellunara.common.response.ApiResponse;
import com.hotellunara.reservation.dto.CancelReservationRequest;
import com.hotellunara.reservation.dto.CheckoutSummaryResponse;
import com.hotellunara.reservation.dto.ReservationRequestDTO;
import com.hotellunara.reservation.dto.ReservationResponseDTO;
import com.hotellunara.reservation.dto.TodayOperationsResponse;
import com.hotellunara.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.hotellunara.common.enums.ReservationStatus;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservas", description = "Gestion de reservas de habitaciones")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "Crear una reserva de habitacion")
    public ResponseEntity<ApiResponse<ReservationResponseDTO>> createReservation(@Valid @RequestBody ReservationRequestDTO requestDTO,
                                                                                 @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Reserva creada correctamente",
                reservationService.createReservation(requestDTO, currentUser.getId())));
    }

    @GetMapping("/me")
    @Operation(summary = "Listar mis reservas")
    public ResponseEntity<ApiResponse<List<ReservationResponseDTO>>> getMyReservations(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Reservas obtenidas correctamente",
                reservationService.getMyReservations(currentUser.getId())));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN')")
    @Operation(summary = "Listar todas las reservas")
    public ResponseEntity<ApiResponse<List<ReservationResponseDTO>>> getAllReservations(
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) LocalDate checkInFrom,
            @RequestParam(required = false) LocalDate checkInTo,
            @RequestParam(required = false) String guestQuery) {
        return ResponseEntity.ok(ApiResponse.success("Reservas obtenidas correctamente",
                reservationService.getAllReservations(status, checkInFrom, checkInTo, guestQuery)));
    }

    @GetMapping("/today-operations")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN')")
    @Operation(summary = "Obtener panel operativo del dia")
    public ResponseEntity<ApiResponse<TodayOperationsResponse>> getTodayOperations() {
        return ResponseEntity.ok(ApiResponse.success("Panel operativo obtenido correctamente",
                reservationService.getTodayOperations()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una reserva por id")
    public ResponseEntity<ApiResponse<ReservationResponseDTO>> getReservation(@PathVariable Long id,
                                                                              @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Reserva obtenida correctamente",
                reservationService.getReservationById(id, currentUser.getId())));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancelar una reserva")
    public ResponseEntity<ApiResponse<ReservationResponseDTO>> cancelReservation(@PathVariable Long id,
                                                                                 @Valid @RequestBody CancelReservationRequest request,
                                                                                 @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Reserva cancelada correctamente",
                reservationService.cancelReservation(id, currentUser.getId(), request.getMotivo())));
    }

    @PostMapping("/{id}/checkin")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN')")
    @Operation(summary = "Realizar check-in")
    public ResponseEntity<ApiResponse<ReservationResponseDTO>> checkin(@PathVariable Long id,
                                                                       @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Check-in realizado correctamente",
                reservationService.makeCheckin(id, currentUser.getId())));
    }

    @PostMapping("/{id}/checkout")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN')")
    @Operation(summary = "Realizar check-out")
    public ResponseEntity<ApiResponse<CheckoutSummaryResponse>> checkout(@PathVariable Long id,
                                                                         @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Check-out realizado correctamente",
                reservationService.makeCheckout(id, currentUser.getId())));
    }
}

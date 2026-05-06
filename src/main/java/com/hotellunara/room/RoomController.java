package com.hotellunara.room;

import com.hotellunara.common.enums.RoomStatus;
import com.hotellunara.common.enums.RoomType;
import com.hotellunara.common.response.ApiResponse;
import com.hotellunara.room.dto.RoomRequestDTO;
import com.hotellunara.room.dto.RoomResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Tag(name = "Habitaciones", description = "Consulta y administracion de habitaciones")
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    @Operation(summary = "Listar habitaciones activas")
    public ResponseEntity<ApiResponse<List<RoomResponseDTO>>> getRooms() {
        return ResponseEntity.ok(ApiResponse.success("Habitaciones obtenidas correctamente", roomService.getAllActiveRooms()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una habitacion por id")
    public ResponseEntity<ApiResponse<RoomResponseDTO>> getRoom(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Habitacion obtenida correctamente", roomService.getRoomById(id)));
    }

    @GetMapping("/availability")
    @Operation(summary = "Consultar disponibilidad de habitaciones")
    public ResponseEntity<ApiResponse<List<RoomResponseDTO>>> getAvailability(@RequestParam LocalDate checkIn,
                                                                              @RequestParam LocalDate checkOut,
                                                                              @RequestParam(required = false) Integer adults,
                                                                              @RequestParam(required = false) Integer children,
                                                                              @RequestParam(required = false) RoomType type) {
        return ResponseEntity.ok(ApiResponse.success("Disponibilidad consultada correctamente",
                roomService.getAvailableRooms(checkIn, checkOut, adults, children, type)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear una habitacion")
    public ResponseEntity<ApiResponse<RoomResponseDTO>> createRoom(@Valid @RequestBody RoomRequestDTO requestDTO,
                                                                   @AuthenticationPrincipal com.hotellunara.user.User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Habitacion creada correctamente",
                roomService.createRoom(requestDTO, currentUser.getId())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar una habitacion")
    public ResponseEntity<ApiResponse<RoomResponseDTO>> updateRoom(@PathVariable Long id,
                                                                   @Valid @RequestBody RoomRequestDTO requestDTO,
                                                                   @AuthenticationPrincipal com.hotellunara.user.User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Habitacion actualizada correctamente",
                roomService.updateRoom(id, requestDTO, currentUser.getId())));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN')")
    @Operation(summary = "Cambiar el estado operativo de una habitacion")
    public ResponseEntity<ApiResponse<RoomResponseDTO>> changeStatus(@PathVariable Long id,
                                                                     @RequestParam RoomStatus status,
                                                                     @AuthenticationPrincipal com.hotellunara.user.User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Estado de habitacion actualizado correctamente",
                roomService.changeRoomStatus(id, status, currentUser.getId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desactivar una habitacion")
    public ResponseEntity<ApiResponse<RoomResponseDTO>> deactivateRoom(@PathVariable Long id,
                                                                       @AuthenticationPrincipal com.hotellunara.user.User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Habitacion desactivada correctamente",
                roomService.deactivateRoom(id, currentUser.getId())));
    }
}

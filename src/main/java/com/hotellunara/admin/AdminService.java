package com.hotellunara.admin;

import com.hotellunara.audit.AuditService;
import com.hotellunara.audit.dto.AuditLogResponseDTO;
import com.hotellunara.auth.dto.RegisterRequest;
import com.hotellunara.admin.dto.DashboardResponse;
import com.hotellunara.common.dto.PageResponse;
import com.hotellunara.common.enums.ReservationStatus;
import com.hotellunara.common.enums.RoomStatus;
import com.hotellunara.common.enums.ServiceRequestStatus;
import com.hotellunara.common.enums.UserRole;
import com.hotellunara.user.UserService;
import com.hotellunara.user.dto.UserResponseDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hotellunara.reservation.ReservationRepository;
import com.hotellunara.room.RoomRepository;
import com.hotellunara.hotelservice.ServiceRequestRepository;
import com.hotellunara.user.UserRepository;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        LocalDate today = LocalDate.now();
        BigDecimal revenue = reservationRepository.sumCompletedRevenueByMonth(
                ReservationStatus.COMPLETADA, today.getYear(), today.getMonthValue());
        return DashboardResponse.builder()
                .totalHabitaciones(roomRepository.countByActivaTrue())
                .habitacionesOcupadas(roomRepository.countByActivaTrueAndEstado(RoomStatus.OCUPADA))
                .habitacionesDisponibles(roomRepository.countByActivaTrueAndEstado(RoomStatus.DISPONIBLE))
                .habitacionesMantenimiento(roomRepository.countByActivaTrueAndEstado(RoomStatus.MANTENIMIENTO))
                .reservasHoy(reservationRepository.countByCheckIn(today))
                .checkoutsHoy(reservationRepository.countByCheckOut(today))
                .reservasActivasTotales(reservationRepository.countByEstado(ReservationStatus.ACTIVA))
                .ingresosMesActual(revenue == null ? BigDecimal.ZERO : revenue)
                .totalHuespedesRegistrados(userRepository.countByRoleAndActivoTrue(UserRole.GUEST))
                .solicitudesPendientes(serviceRequestRepository.countByEstado(ServiceRequestStatus.PENDIENTE))
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponseDTO> getAuditLogs(int page, int size) {
        return PageResponse.from(auditService.getAllLogs(PageRequest.of(page, size)));
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponseDTO> getUsers(int page, int size) {
        return userService.getUsersPage(page, size);
    }

    @Transactional
    public UserResponseDTO createReceptionist(RegisterRequest request, UUID actorId) {
        UserResponseDTO created = userService.createReceptionist(request);
        auditService.log(actorId, "CREATE_RECEPTIONIST_ACCOUNT", "User", created.getId().toString(), created.getEmail(), null);
        return created;
    }

    @Transactional
    public UserResponseDTO changeUserRole(UUID targetUserId, com.hotellunara.common.enums.UserRole role, UUID actorId) {
        UserResponseDTO updated = userService.changeRole(targetUserId, role);
        auditService.log(actorId, "CHANGE_USER_ROLE", "User", updated.getId().toString(), role.name(), null);
        return updated;
    }

    @Transactional
    public UserResponseDTO changeUserStatus(UUID targetUserId, boolean active, UUID actorId) {
        UserResponseDTO updated = userService.changeStatus(targetUserId, active);
        auditService.log(actorId, "CHANGE_USER_STATUS", "User", updated.getId().toString(), active, null);
        return updated;
    }
}

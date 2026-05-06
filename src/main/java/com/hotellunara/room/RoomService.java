package com.hotellunara.room;

import com.hotellunara.audit.AuditService;
import com.hotellunara.common.enums.ReservationStatus;
import com.hotellunara.common.enums.RoomStatus;
import com.hotellunara.common.enums.RoomType;
import com.hotellunara.common.exception.BusinessRuleException;
import com.hotellunara.common.exception.ResourceNotFoundException;
import com.hotellunara.reservation.ReservationRepository;
import com.hotellunara.room.dto.RoomRequestDTO;
import com.hotellunara.room.dto.RoomResponseDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final RoomMapper roomMapper;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<RoomResponseDTO> getAllActiveRooms() {
        return roomRepository.findByActivaTrueOrderByPisoAscNumeroAsc()
                .stream()
                .map(roomMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoomResponseDTO getRoomById(Long id) {
        return roomMapper.toResponse(getActiveRoom(id));
    }

    @Transactional(readOnly = true)
    public List<RoomResponseDTO> getAvailableRooms(LocalDate checkIn,
                                                   LocalDate checkOut,
                                                   Integer adults,
                                                   Integer children,
                                                   RoomType type) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new BusinessRuleException("El rango de fechas es invalido");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new BusinessRuleException("La fecha de check-in no puede estar en el pasado");
        }

        return roomRepository.findByActivaTrueOrderByPisoAscNumeroAsc()
                .stream()
                .filter(room -> adults == null || room.getCapacidadAdultos() >= adults)
                .filter(room -> children == null || room.getCapacidadNinos() >= children)
                .filter(room -> type == null || room.getTipo() == type)
                .filter(room -> room.getEstado() != RoomStatus.MANTENIMIENTO)
                .filter(room -> !reservationRepository.existsOverlappingReservation(
                        room.getId(),
                        checkIn,
                        checkOut,
                        Set.of(ReservationStatus.CONFIRMADA, ReservationStatus.ACTIVA)))
                .map(roomMapper::toResponse)
                .toList();
    }

    @Transactional
    public RoomResponseDTO createRoom(RoomRequestDTO requestDTO, java.util.UUID actorId) {
        if (roomRepository.existsByNumero(requestDTO.getNumero())) {
            throw new BusinessRuleException("Ya existe una habitacion con ese numero");
        }
        Room room = roomMapper.toEntity(requestDTO);
        if (requestDTO.getEstado() == null) {
            room.setEstado(RoomStatus.DISPONIBLE);
        }
        if (requestDTO.getActiva() == null) {
            room.setActiva(true);
        }
        Room savedRoom = roomRepository.save(room);
        auditService.log(actorId, "CREATE_ROOM", "Room", savedRoom.getId().toString(), buildRoomAuditDetails(null, savedRoom), null);
        return roomMapper.toResponse(savedRoom);
    }

    @Transactional
    public RoomResponseDTO updateRoom(Long id, RoomRequestDTO requestDTO, java.util.UUID actorId) {
        Room room = getRoom(id);
        java.util.Map<String, Object> before = buildRoomSnapshot(room);
        if (!room.getNumero().equals(requestDTO.getNumero()) && roomRepository.existsByNumero(requestDTO.getNumero())) {
            throw new BusinessRuleException("Ya existe una habitacion con ese numero");
        }
        roomMapper.updateEntity(requestDTO, room);
        Room updatedRoom = roomRepository.save(room);
        auditService.log(actorId, "UPDATE_ROOM", "Room", updatedRoom.getId().toString(),
                buildRoomAuditDetails(before, updatedRoom), null);
        return roomMapper.toResponse(updatedRoom);
    }

    @Transactional
    public RoomResponseDTO changeRoomStatus(Long id, RoomStatus status, java.util.UUID actorId) {
        if (status != RoomStatus.LIMPIEZA && status != RoomStatus.MANTENIMIENTO) {
            throw new BusinessRuleException("Solo se puede cambiar manualmente una habitacion a LIMPIEZA o MANTENIMIENTO");
        }
        Room room = getRoom(id);
        room.setEstado(status);
        Room updatedRoom = roomRepository.save(room);
        auditService.log(actorId, "CHANGE_ROOM_STATUS", "Room", updatedRoom.getId().toString(), status.name(), null);
        return roomMapper.toResponse(updatedRoom);
    }

    @Transactional
    public RoomResponseDTO deactivateRoom(Long id, java.util.UUID actorId) {
        Room room = getRoom(id);
        room.setActiva(false);
        Room updatedRoom = roomRepository.save(room);
        auditService.log(actorId, "DEACTIVATE_ROOM", "Room", updatedRoom.getId().toString(), updatedRoom.getNumero(), null);
        return roomMapper.toResponse(updatedRoom);
    }

    @Transactional(readOnly = true)
    public Room getRoom(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habitacion no encontrada"));
    }

    @Transactional(readOnly = true)
    public Room getActiveRoom(Long id) {
        return roomRepository.findByIdAndActivaTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habitacion no encontrada"));
    }

    private java.util.Map<String, Object> buildRoomAuditDetails(java.util.Map<String, Object> before, Room after) {
        java.util.Map<String, Object> details = new java.util.LinkedHashMap<>();
        if (before != null) {
            details.put("before", before);
        }
        details.put("after", buildRoomSnapshot(after));
        return details;
    }

    private java.util.Map<String, Object> buildRoomSnapshot(Room room) {
        java.util.Map<String, Object> snapshot = new java.util.LinkedHashMap<>();
        snapshot.put("numero", room.getNumero());
        snapshot.put("piso", room.getPiso());
        snapshot.put("tipo", room.getTipo().name());
        snapshot.put("capacidadAdultos", room.getCapacidadAdultos());
        snapshot.put("capacidadNinos", room.getCapacidadNinos());
        snapshot.put("precioPorNoche", room.getPrecioPorNoche());
        snapshot.put("estado", room.getEstado().name());
        snapshot.put("activa", room.isActiva());
        return snapshot;
    }
}

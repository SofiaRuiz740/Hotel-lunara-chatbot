package com.hotellunara.hotelservice;

import com.hotellunara.common.enums.ServiceRequestStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    long countByServiceIdAndFechaSolicitadaAndHoraSolicitadaAndEstadoIn(Long serviceId,
                                                                        LocalDate fechaSolicitada,
                                                                        LocalTime horaSolicitada,
                                                                        Collection<ServiceRequestStatus> estados);

    long countByServiceIdAndFechaSolicitadaAndHoraSolicitadaAndEstadoInAndIdNot(Long serviceId,
                                                                                LocalDate fechaSolicitada,
                                                                                LocalTime horaSolicitada,
                                                                                Collection<ServiceRequestStatus> estados,
                                                                                Long id);

    List<ServiceRequest> findByGuestIdOrderByCreadaEnDesc(UUID guestId);

    List<ServiceRequest> findByReservationIdOrderByFechaSolicitadaAscHoraSolicitadaAsc(Long reservationId);

    List<ServiceRequest> findByGuestIdAndEstadoInOrderByFechaSolicitadaAscHoraSolicitadaAsc(UUID guestId,
                                                                                            Collection<ServiceRequestStatus> estados);

    long countByEstado(ServiceRequestStatus estado);
}

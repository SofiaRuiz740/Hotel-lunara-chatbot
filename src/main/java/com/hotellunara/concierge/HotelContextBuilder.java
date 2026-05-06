package com.hotellunara.concierge;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotellunara.common.enums.ReservationStatus;
import com.hotellunara.common.enums.ServiceRequestStatus;
import com.hotellunara.hotelservice.HotelServiceRepository;
import com.hotellunara.hotelservice.ServiceRequestRepository;
import com.hotellunara.reservation.Reservation;
import com.hotellunara.reservation.ReservationRepository;
import com.hotellunara.restaurant.RestaurantRepository;
import com.hotellunara.user.User;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HotelContextBuilder {

    private final HotelServiceRepository hotelServiceRepository;
    private final ReservationRepository reservationRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final RestaurantRepository restaurantRepository;
    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper;

    public String buildContext(User guest, ConversationSession session) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("hotel", buildHotelContext());
        if (guest != null) {
            context.put("guest", buildGuestContext(guest, session));
        }
        return toJson(context);
    }

    private Map<String, Object> buildHotelContext() {
        Map<String, Object> hotelContext = new LinkedHashMap<>();
        hotelContext.put("nombre", "Hotel Lunara");
        hotelContext.put("categoria", "4 estrellas");
        hotelContext.put("pisos", 8);
        hotelContext.put("habitaciones", 60);
        hotelContext.put("direccion", "Av. Principal 123, Ciudad");
        hotelContext.put("telefono", "+1 (555) 123-4567");
        hotelContext.put("email", "info@hotellunara.com");
        hotelContext.put("restaurante", Map.of(
                "desayuno", "07:00-10:30",
                "almuerzo", "12:30-15:00",
                "cena", "19:00-23:00"));
        hotelContext.put("serviciosActivos", hotelServiceRepository.findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(service -> Map.of(
                        "nombre", service.getNombre(),
                        "precio", service.getPrecio(),
                        "horario", service.getHorarioApertura() + "-" + service.getHorarioCierre()))
                .toList());
        hotelContext.put("politicaCheckIn", "15:00");
        hotelContext.put("politicaCheckOut", "12:00");
        hotelContext.put("politicaCancelacion", "Gratuita con mas de 48h de anticipacion");
        return hotelContext;
    }

    private Map<String, Object> buildGuestContext(User guest, ConversationSession session) {
        Map<String, Object> guestContext = new LinkedHashMap<>();
        guestContext.put("nombreCompleto", guest.getNombre() + " " + guest.getApellido());
        guestContext.put("alergias", guest.getAlergias());
        guestContext.put("preferenciasCama", guest.getPreferenciasCama());
        guestContext.put("peticionesEspeciales", guest.getPeticionesEspeciales());

        List<Reservation> reservations = reservationRepository.findUpcomingOrActiveReservations(
                guest.getId(), List.of(ReservationStatus.ACTIVA, ReservationStatus.CONFIRMADA), LocalDate.now());
        if (!reservations.isEmpty()) {
            Reservation reservation = reservations.get(0);
            Map<String, Object> reservationContext = new LinkedHashMap<>();
            reservationContext.put("codigo", reservation.getCodigoReserva());
            reservationContext.put("habitacion", reservation.getRoom().getNumero());
            reservationContext.put("tipo", reservation.getRoom().getTipo().name());
            reservationContext.put("piso", reservation.getRoom().getPiso());
            reservationContext.put("amenities", reservation.getRoom().getAmenities());
            reservationContext.put("checkIn", reservation.getCheckIn());
            reservationContext.put("checkOut", reservation.getCheckOut());
            reservationContext.put("nochesRestantes", Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), reservation.getCheckOut())));
            guestContext.put("reservaActivaOProxima", reservationContext);
        }

        guestContext.put("solicitudesActivas", serviceRequestRepository.findByGuestIdAndEstadoInOrderByFechaSolicitadaAscHoraSolicitadaAsc(
                        guest.getId(),
                        List.of(ServiceRequestStatus.PENDIENTE, ServiceRequestStatus.CONFIRMADO, ServiceRequestStatus.EN_PROCESO))
                .stream()
                .map(request -> Map.of(
                        "servicio", request.getService().getNombre(),
                        "estado", request.getEstado().name(),
                        "fecha", request.getFechaSolicitada(),
                        "hora", request.getHoraSolicitada()))
                .toList());

        guestContext.put("reservasRestauranteProximas", restaurantRepository.findByFechaGreaterThanEqualAndGuestIdOrderByFechaAscHoraAsc(
                        LocalDate.now(), guest.getId())
                .stream()
                .map(reservation -> Map.of(
                        "fecha", reservation.getFecha(),
                        "hora", reservation.getHora(),
                        "mesa", reservation.getTable().getNumero(),
                        "estado", reservation.getEstado().name()))
                .toList());

        guestContext.put("ultimos5MensajesSesion", getRecentMessages(session)
                .stream()
                .map(message -> Map.of(
                        "role", message.getRole().name(),
                        "contenido", message.getContenido(),
                        "timestamp", message.getTimestamp()))
                .toList());
        return guestContext;
    }

    public List<ConversationMessage> getRecentMessages(ConversationSession session) {
        if (session == null || session.getId() == null) {
            return List.of();
        }
        List<ConversationMessage> messages = messageRepository.findTop5BySessionIdOrderByTimestampDesc(session.getId());
        java.util.Collections.reverse(messages);
        return messages;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No fue posible serializar el contexto del hotel", ex);
        }
    }
}

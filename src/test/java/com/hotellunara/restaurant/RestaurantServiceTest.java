package com.hotellunara.restaurant;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.hotellunara.audit.AuditService;
import com.hotellunara.common.enums.RestaurantReservationStatus;
import com.hotellunara.common.enums.UserRole;
import com.hotellunara.common.exception.BusinessRuleException;
import com.hotellunara.reservation.ReservationRepository;
import com.hotellunara.user.User;
import com.hotellunara.user.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private RestaurantTableRepository restaurantTableRepository;
    @Mock
    private RestaurantReservationMapper restaurantReservationMapper;
    @Mock
    private RestaurantTableMapper restaurantTableMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private AuditService auditService;
    @Mock
    private Clock clock;

    @InjectMocks
    private RestaurantService restaurantService;

    @BeforeEach
    void setUp() {
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(Instant.parse("2026-01-10T10:00:00Z"));
        ReflectionTestUtils.setField(restaurantService, "restaurantCancellationHours", 2L);
        ReflectionTestUtils.setField(restaurantService, "breakfastStart", "07:00");
        ReflectionTestUtils.setField(restaurantService, "breakfastEnd", "10:30");
        ReflectionTestUtils.setField(restaurantService, "lunchStart", "12:30");
        ReflectionTestUtils.setField(restaurantService, "lunchEnd", "15:00");
        ReflectionTestUtils.setField(restaurantService, "dinnerStart", "19:00");
        ReflectionTestUtils.setField(restaurantService, "dinnerEnd", "23:00");
    }

    @Test
    void rejectGuestCancellationLessThanTwoHours() {
        UUID userId = UUID.randomUUID();
        User guest = User.builder().id(userId).role(UserRole.GUEST).email("guest@test.com").password("x").build();
        RestaurantReservation reservation = RestaurantReservation.builder()
                .id(1L)
                .guest(guest)
                .fecha(LocalDate.of(2026, 1, 10))
                .hora(LocalTime.of(11, 0))
                .estado(RestaurantReservationStatus.CONFIRMADA)
                .build();

        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(userRepository.findById(userId)).thenReturn(Optional.of(guest));

        assertThrows(BusinessRuleException.class, () -> restaurantService.cancelReservation(1L, userId));
    }
}

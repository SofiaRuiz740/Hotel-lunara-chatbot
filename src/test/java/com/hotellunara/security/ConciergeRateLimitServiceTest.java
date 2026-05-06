package com.hotellunara.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.hotellunara.common.exception.TooManyRequestsException;
import java.time.Clock;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ConciergeRateLimitServiceTest {

    @Mock
    private Clock clock;

    @InjectMocks
    private ConciergeRateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(rateLimitService, "windowSeconds", 60L);
        ReflectionTestUtils.setField(rateLimitService, "maxRequests", 2);
    }

    @Test
    void allowRequestsWithinConfiguredLimit() {
        when(clock.instant()).thenReturn(Instant.parse("2026-01-10T10:00:00Z"));
        assertDoesNotThrow(() -> rateLimitService.consume("ip:1.1.1.1"));
        assertDoesNotThrow(() -> rateLimitService.consume("ip:1.1.1.1"));
    }

    @Test
    void blockRequestsAboveConfiguredLimit() {
        when(clock.instant()).thenReturn(Instant.parse("2026-01-10T10:00:00Z"));
        rateLimitService.consume("ip:2.2.2.2");
        rateLimitService.consume("ip:2.2.2.2");
        assertThrows(TooManyRequestsException.class, () -> rateLimitService.consume("ip:2.2.2.2"));
    }
}

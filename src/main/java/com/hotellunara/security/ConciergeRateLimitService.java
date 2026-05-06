package com.hotellunara.security;

import com.hotellunara.common.exception.TooManyRequestsException;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConciergeRateLimitService {

    private final Clock clock;
    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    @Value("${app.security.concierge.rate-limit.window-seconds:60}")
    private long windowSeconds;
    @Value("${app.security.concierge.rate-limit.max-requests:20}")
    private int maxRequests;

    public void consume(String key) {
        Instant now = Instant.now(clock);
        WindowCounter updated = counters.compute(key, (ignored, existing) -> updateCounter(existing, now));
        if (updated.count() > maxRequests) {
            throw new TooManyRequestsException("Demasiadas solicitudes al concierge. Intenta nuevamente en unos segundos.");
        }
        cleanupExpired(now);
    }

    private WindowCounter updateCounter(WindowCounter existing, Instant now) {
        if (existing == null || now.isAfter(existing.windowStart().plusSeconds(windowSeconds))) {
            return new WindowCounter(now, 1);
        }
        return new WindowCounter(existing.windowStart(), existing.count() + 1);
    }

    private void cleanupExpired(Instant now) {
        counters.entrySet().removeIf(entry ->
                now.isAfter(entry.getValue().windowStart().plusSeconds(windowSeconds * 2)));
    }

    private record WindowCounter(Instant windowStart, int count) {
    }
}

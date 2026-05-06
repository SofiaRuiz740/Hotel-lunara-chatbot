package com.hotellunara.concierge;

import com.hotellunara.common.response.ApiResponse;
import com.hotellunara.concierge.dto.ConciergeChatRequest;
import com.hotellunara.concierge.dto.ConciergeChatResponse;
import com.hotellunara.concierge.dto.ConversationMessageResponseDTO;
import com.hotellunara.security.ConciergeRateLimitService;
import com.hotellunara.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/concierge")
@RequiredArgsConstructor
@Tag(name = "Concierge IA", description = "Chat contextual con el concierge virtual")
public class ConciergeController {

    private final ConciergeService conciergeService;
    private final ConciergeRateLimitService conciergeRateLimitService;

    @PostMapping("/chat")
    @Operation(summary = "Enviar mensaje al concierge virtual")
    public ResponseEntity<ApiResponse<ConciergeChatResponse>> chat(@Valid @RequestBody ConciergeChatRequest request,
                                                                  @AuthenticationPrincipal User currentUser,
                                                                  HttpServletRequest httpRequest) {
        String callerKey = currentUser != null
                ? "user:" + currentUser.getId()
                : "ip:" + resolveIp(httpRequest);
        conciergeRateLimitService.consume(callerKey);
        return ResponseEntity.ok(ApiResponse.success("Respuesta generada correctamente",
                conciergeService.chat(request.getMensaje(), request.getSessionToken(), currentUser == null ? null : currentUser.getId())));
    }

    @GetMapping("/history")
    @Operation(summary = "Obtener historial de conversaciones del usuario")
    public ResponseEntity<ApiResponse<List<ConversationMessageResponseDTO>>> getHistory(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Historial obtenido correctamente",
                conciergeService.getHistory(currentUser.getId())));
    }

    private String resolveIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        return request.getRemoteAddr();
    }
}

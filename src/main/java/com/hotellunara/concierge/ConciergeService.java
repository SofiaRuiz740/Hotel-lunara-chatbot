package com.hotellunara.concierge;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotellunara.common.enums.ConciergeActionSuggested;
import com.hotellunara.common.enums.ConversationRole;
import com.hotellunara.common.exception.ResourceNotFoundException;
import com.hotellunara.concierge.dto.ConciergeChatResponse;
import com.hotellunara.concierge.dto.ConversationMessageResponseDTO;
import com.hotellunara.user.User;
import com.hotellunara.user.UserRepository;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConciergeService {

    private final SessionRepository sessionRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ConversationMessageMapper conversationMessageMapper;
    private final HotelContextBuilder hotelContextBuilder;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.openai.api-key}")
    private String openAiApiKey;

    @Value("${app.openai.model}")
    private String openAiModel;

    @Value("${app.openai.base-url}")
    private String openAiBaseUrl;

    @Value("${app.ai.provider:auto}")
    private String aiProvider;

    @Value("${app.ollama.model:llama3.2}")
    private String ollamaModel;

    @Value("${app.ollama.base-url:http://host.docker.internal:11434/api/chat}")
    private String ollamaBaseUrl;

    @Value("${app.ollama.preflight-timeout-ms:800}")
    private int ollamaPreflightTimeoutMs;

    @Transactional
    public ConciergeChatResponse chat(String mensaje, String sessionToken, UUID userId) {
        User guest = userId == null ? null : userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        ConversationSession session = resolveSession(guest, sessionToken, mensaje);
        String contextSnapshot = hotelContextBuilder.buildContext(guest, session);
        List<ConversationMessage> recentMessages = hotelContextBuilder.getRecentMessages(session);

        OpenAiResult aiResult;
        try {
            aiResult = generateAiResponse(mensaje, contextSnapshot, recentMessages, guest);
        } catch (Exception ex) {
            log.warn("Concierge AI fallback activated: {}", ex.getMessage());
            aiResult = new OpenAiResult(buildFallbackResponse(mensaje, guest), 0);
        }
        ConciergeActionSuggested action = extractAction(aiResult.content());
        String cleanedResponse = sanitizeAssistantResponse(aiResult.content());

        messageRepository.save(ConversationMessage.builder()
                .session(session)
                .role(ConversationRole.USER)
                .contenido(mensaje)
                .tokensUsados(0)
                .build());

        messageRepository.save(ConversationMessage.builder()
                .session(session)
                .role(ConversationRole.ASSISTANT)
                .contenido(cleanedResponse)
                .tokensUsados(aiResult.totalTokens())
                .contextoSnapshot(contextSnapshot)
                .build());

        session.setFechaUltimoMensaje(LocalDateTime.now());
        session.setTotalMensajes(session.getTotalMensajes() + 2);
        sessionRepository.save(session);

        return ConciergeChatResponse.builder()
                .respuesta(cleanedResponse)
                .actionSuggested(action)
                .sessionToken(session.getSessionToken())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ConversationMessageResponseDTO> getHistory(UUID userId) {
        return messageRepository.findBySessionGuestIdOrderByTimestampAsc(userId)
                .stream()
                .map(conversationMessageMapper::toResponse)
                .toList();
    }

    private ConversationSession resolveSession(User guest, String sessionToken, String mensaje) {
        if (guest != null) {
            return sessionRepository.findFirstByGuestIdAndActivaTrueOrderByFechaUltimoMensajeDesc(guest.getId())
                    .orElseGet(() -> sessionRepository.save(ConversationSession.builder()
                            .guest(guest)
                            .sessionToken(UUID.randomUUID().toString())
                            .idiomaDetectado(resolveLanguageCode(guest, mensaje))
                            .activa(true)
                            .build()));
        }

        if (sessionToken != null && !sessionToken.isBlank()) {
            return sessionRepository.findBySessionTokenAndActivaTrue(sessionToken)
                    .orElseGet(() -> sessionRepository.save(ConversationSession.builder()
                            .sessionToken(sessionToken)
                            .idiomaDetectado(resolveLanguageCode(null, mensaje))
                            .activa(true)
                            .build()));
        }

        return sessionRepository.save(ConversationSession.builder()
                .sessionToken(UUID.randomUUID().toString())
                .idiomaDetectado(resolveLanguageCode(null, mensaje))
                .activa(true)
                .build());
    }

    private String resolveLanguageCode(User guest, String mensaje) {
        if (guest != null && guest.getIdioma() != null) {
            return guest.getIdioma().name();
        }
        String normalized = mensaje.toLowerCase(Locale.ROOT);
        if (normalized.contains("bonjour") || normalized.contains("merci")) {
            return "FR";
        }
        if (normalized.contains("hello") || normalized.contains("please")) {
            return "EN";
        }
        return "ES";
    }

    private OpenAiResult callOpenAi(String mensaje,
                                    String contextSnapshot,
                                    List<ConversationMessage> recentMessages,
                                    User guest) {
        if (openAiApiKey == null || openAiApiKey.isBlank()) {
            return new OpenAiResult(buildFallbackResponse(mensaje, guest), 0);
        }

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", buildSystemPrompt(contextSnapshot, guest)));
        for (ConversationMessage message : recentMessages) {
            messages.add(Map.of(
                    "role", message.getRole() == ConversationRole.USER ? "user" : "assistant",
                    "content", message.getContenido()));
        }
        messages.add(Map.of("role", "user", "content", mensaje));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", openAiModel);
        payload.put("max_tokens", 500);
        payload.put("temperature", 0.7);
        payload.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        ResponseEntity<String> response = restTemplate.exchange(
                openAiBaseUrl,
                HttpMethod.POST,
                new HttpEntity<>(payload, headers),
                String.class);

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").get(0).path("message").path("content").asText();
            int totalTokens = root.path("usage").path("total_tokens").asInt(0);
            return new OpenAiResult(content, totalTokens);
        } catch (Exception ex) {
            throw new IllegalStateException("No fue posible interpretar la respuesta de OpenAI", ex);
        }
    }

    private OpenAiResult generateAiResponse(String mensaje,
                                            String contextSnapshot,
                                            List<ConversationMessage> recentMessages,
                                            User guest) {
        String provider = aiProvider == null ? "auto" : aiProvider.trim().toLowerCase(Locale.ROOT);
        return switch (provider) {
            case "ollama" -> callOllama(mensaje, contextSnapshot, recentMessages, guest);
            case "openai" -> callOpenAi(mensaje, contextSnapshot, recentMessages, guest);
            case "auto" -> tryAutoProviders(mensaje, contextSnapshot, recentMessages, guest);
            default -> throw new IllegalStateException("Proveedor de IA no soportado: " + aiProvider);
        };
    }

    private OpenAiResult tryAutoProviders(String mensaje,
                                          String contextSnapshot,
                                          List<ConversationMessage> recentMessages,
                                          User guest) {
        try {
            return callOllama(mensaje, contextSnapshot, recentMessages, guest);
        } catch (Exception ollamaEx) {
            log.warn("Ollama unavailable, trying OpenAI: {}", ollamaEx.getMessage());
        }

        if (openAiApiKey != null && !openAiApiKey.isBlank()) {
            return callOpenAi(mensaje, contextSnapshot, recentMessages, guest);
        }

        throw new IllegalStateException("No hay proveedor de IA disponible (Ollama/OpenAI)");
    }

    private OpenAiResult callOllama(String mensaje,
                                    String contextSnapshot,
                                    List<ConversationMessage> recentMessages,
                                    User guest) {
        ensureOllamaAvailable();

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", buildSystemPrompt(contextSnapshot, guest)));
        for (ConversationMessage message : recentMessages) {
            messages.add(Map.of(
                    "role", message.getRole() == ConversationRole.USER ? "user" : "assistant",
                    "content", message.getContenido()));
        }
        messages.add(Map.of("role", "user", "content", mensaje));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", ollamaModel);
        payload.put("stream", false);
        payload.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(
                ollamaBaseUrl,
                HttpMethod.POST,
                new HttpEntity<>(payload, headers),
                String.class);

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("message").path("content").asText();
            int totalTokens = root.path("prompt_eval_count").asInt(0) + root.path("eval_count").asInt(0);
            if (content == null || content.isBlank()) {
                throw new IllegalStateException("Respuesta vacia desde Ollama");
            }
            return new OpenAiResult(content, totalTokens);
        } catch (Exception ex) {
            throw new IllegalStateException("No fue posible interpretar la respuesta de Ollama", ex);
        }
    }

    private void ensureOllamaAvailable() {
        if (ollamaBaseUrl == null || ollamaBaseUrl.isBlank()) {
            throw new IllegalStateException("OLLAMA_BASE_URL no configurada");
        }
        if (ollamaModel == null || ollamaModel.isBlank()) {
            throw new IllegalStateException("OLLAMA_MODEL no configurado");
        }

        URI uri;
        try {
            uri = URI.create(ollamaBaseUrl);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("OLLAMA_BASE_URL invalida", ex);
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalStateException("OLLAMA_BASE_URL invalida");
        }

        int port = uri.getPort();
        if (port < 0) {
            port = "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
        }

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), ollamaPreflightTimeoutMs);
        } catch (Exception ex) {
            throw new IllegalStateException("Ollama no disponible en " + host + ":" + port, ex);
        }
    }

    private String buildSystemPrompt(String contextSnapshot, User guest) {
        String guestHint = guest == null ? "" : " Huesped actual: " + guest.getNombre() + " " + guest.getApellido() + ".";
        return "Eres el concierge virtual del Hotel Lunara, un asistente profesional, calido y sofisticado. "
                + "Tienes acceso al contexto completo del hotel y del huesped. Personaliza cada respuesta usando el nombre del huesped cuando este disponible. "
                + "Responde siempre en el idioma en que el huesped escribe. "
                + "Si el huesped pregunta algo que puedes resolver con una accion concreta (reservar restaurante, solicitar servicio), "
                + "termina tu respuesta con una linea especial exactamente asi:\n"
                + "ACTION_SUGGESTED: BOOK_RESTAURANT | REQUEST_SERVICE | NONE\n"
                + "No inventes informacion que no este en el contexto."
                + guestHint
                + "\nContexto actual JSON:\n" + contextSnapshot;
    }

    private ConciergeActionSuggested extractAction(String content) {
        for (String line : content.split("\\R")) {
            if (line.startsWith("ACTION_SUGGESTED:")) {
                String value = line.substring("ACTION_SUGGESTED:".length()).trim();
                try {
                    return ConciergeActionSuggested.valueOf(value);
                } catch (IllegalArgumentException ex) {
                    return ConciergeActionSuggested.NONE;
                }
            }
        }
        return ConciergeActionSuggested.NONE;
    }

    private String sanitizeAssistantResponse(String content) {
        return content.replaceAll("(?m)^ACTION_SUGGESTED:.*$", "").trim();
    }

    private String buildFallbackResponse(String mensaje, User guest) {
        String guestName = guest != null ? guest.getNombre() : "huesped";
        String normalized = mensaje.toLowerCase(Locale.ROOT);

        if (normalized.contains("restaurante") || normalized.contains("mesa")) {
            return "Hola " + guestName + ", claro que si. Puedo ayudarte con el restaurante y reservar tu mesa "
                    + "de forma rapida desde la seccion de restaurante.\n"
                    + "ACTION_SUGGESTED: BOOK_RESTAURANT";
        }

        if (normalized.contains("servicio") || normalized.contains("spa") || normalized.contains("masaje")
                || normalized.contains("tour") || normalized.contains("traslado") || normalized.contains("room service")) {
            return "Hola " + guestName + ", con gusto. Puedes solicitar servicios del hotel desde tu panel de cuenta "
                    + "y te guio en el proceso.\n"
                    + "ACTION_SUGGESTED: REQUEST_SERVICE";
        }

        return "Hola " + guestName + ", bienvenido a Concierge Lunara. "
                + "Estoy aqui para ayudarte con habitaciones, reservas, restaurante, servicios y politicas del hotel. "
                + "Si deseas, te guio paso a paso segun lo que necesites ahora mismo.\n"
                + "ACTION_SUGGESTED: NONE";
    }

    private record OpenAiResult(String content, int totalTokens) {
    }
}

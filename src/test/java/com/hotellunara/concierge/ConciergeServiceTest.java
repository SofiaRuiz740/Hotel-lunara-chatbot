package com.hotellunara.concierge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotellunara.common.enums.ConciergeActionSuggested;
import com.hotellunara.common.enums.UserLanguage;
import com.hotellunara.common.enums.UserRole;
import com.hotellunara.concierge.dto.ConciergeChatResponse;
import com.hotellunara.user.User;
import com.hotellunara.user.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class ConciergeServiceTest {

    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ConversationMessageMapper conversationMessageMapper;
    @Mock
    private HotelContextBuilder hotelContextBuilder;
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ConciergeService conciergeService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(conciergeService, "openAiApiKey", "test-key");
        ReflectionTestUtils.setField(conciergeService, "openAiModel", "gpt-4o-mini");
        ReflectionTestUtils.setField(conciergeService, "openAiBaseUrl", "https://api.openai.com/v1/chat/completions");
        ReflectionTestUtils.setField(conciergeService, "aiProvider", "openai");
        ReflectionTestUtils.setField(conciergeService, "ollamaModel", "llama3.2");
        ReflectionTestUtils.setField(conciergeService, "ollamaBaseUrl", "http://localhost:11434/api/chat");
        ReflectionTestUtils.setField(conciergeService, "ollamaPreflightTimeoutMs", 100);
        ReflectionTestUtils.setField(conciergeService, "objectMapper", new ObjectMapper());
    }

    @Test
    void testChatAnonimoUsaContextoSoloDelHotel() {
        String sessionToken = "anon-session";
        ConversationSession session = ConversationSession.builder()
                .id(UUID.randomUUID())
                .sessionToken(sessionToken)
                .activa(true)
                .build();

        when(sessionRepository.findBySessionTokenAndActivaTrue(sessionToken)).thenReturn(Optional.of(session));
        when(hotelContextBuilder.buildContext(null, session)).thenReturn("{\"hotel\":true}");
        when(hotelContextBuilder.getRecentMessages(session)).thenReturn(List.of());
        when(restTemplate.exchange(eq("https://api.openai.com/v1/chat/completions"), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("""
                        {"choices":[{"message":{"content":"Bienvenido\\nACTION_SUGGESTED: NONE"}}],"usage":{"total_tokens":42}}
                        """));
        when(messageRepository.save(any(ConversationMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(sessionRepository.save(any(ConversationSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConciergeChatResponse response = conciergeService.chat("Hola", sessionToken, null);

        assertEquals("Bienvenido", response.getRespuesta());
        assertEquals(ConciergeActionSuggested.NONE, response.getActionSuggested());
        verify(hotelContextBuilder).buildContext(null, session);

        ArgumentCaptor<HttpEntity> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("https://api.openai.com/v1/chat/completions"), eq(HttpMethod.POST), requestCaptor.capture(), eq(String.class));
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> payload = (java.util.Map<String, Object>) requestCaptor.getValue().getBody();
        @SuppressWarnings("unchecked")
        List<java.util.Map<String, String>> messages = (List<java.util.Map<String, String>>) payload.get("messages");
        assertTrue(messages.get(0).get("content").contains("{\"hotel\":true}"));
    }

    @Test
    void testChatAutenticadoIncluyeContextoHuesped() {
        UUID userId = UUID.randomUUID();
        User guest = User.builder()
                .id(userId)
                .nombre("Laura")
                .apellido("Perez")
                .email("laura@hotellunara.com")
                .role(UserRole.GUEST)
                .idioma(UserLanguage.ES)
                .password("encoded")
                .activo(true)
                .build();
        ConversationSession session = ConversationSession.builder()
                .id(UUID.randomUUID())
                .guest(guest)
                .sessionToken("auth-session")
                .activa(true)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(guest));
        when(sessionRepository.findFirstByGuestIdAndActivaTrueOrderByFechaUltimoMensajeDesc(userId)).thenReturn(Optional.of(session));
        when(hotelContextBuilder.buildContext(guest, session)).thenReturn("{\"hotel\":true,\"guest\":true}");
        when(hotelContextBuilder.getRecentMessages(session)).thenReturn(List.of());
        when(restTemplate.exchange(eq("https://api.openai.com/v1/chat/completions"), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("""
                        {"choices":[{"message":{"content":"Laura, ya lo reviso\\nACTION_SUGGESTED: REQUEST_SERVICE"}}],"usage":{"total_tokens":55}}
                        """));
        when(messageRepository.save(any(ConversationMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(sessionRepository.save(any(ConversationSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConciergeChatResponse response = conciergeService.chat("Necesito toallas", null, userId);

        assertEquals("Laura, ya lo reviso", response.getRespuesta());
        assertEquals(ConciergeActionSuggested.REQUEST_SERVICE, response.getActionSuggested());
        verify(hotelContextBuilder).buildContext(guest, session);

        ArgumentCaptor<HttpEntity> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("https://api.openai.com/v1/chat/completions"), eq(HttpMethod.POST), requestCaptor.capture(), eq(String.class));
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> payload = (java.util.Map<String, Object>) requestCaptor.getValue().getBody();
        @SuppressWarnings("unchecked")
        List<java.util.Map<String, String>> messages = (List<java.util.Map<String, String>>) payload.get("messages");
        assertTrue(messages.get(0).get("content").contains("{\"hotel\":true,\"guest\":true}"));
    }

    @Test
    void testChatSinProveedorDisponibleUsaFallbackLocal() {
        ReflectionTestUtils.setField(conciergeService, "aiProvider", "ollama");
        ReflectionTestUtils.setField(conciergeService, "openAiApiKey", "");
        ReflectionTestUtils.setField(conciergeService, "ollamaBaseUrl", "");

        String sessionToken = "anon-session";
        ConversationSession session = ConversationSession.builder()
                .id(UUID.randomUUID())
                .sessionToken(sessionToken)
                .activa(true)
                .build();

        when(sessionRepository.findBySessionTokenAndActivaTrue(sessionToken)).thenReturn(Optional.of(session));
        when(hotelContextBuilder.buildContext(null, session)).thenReturn("{\"hotel\":true}");
        when(hotelContextBuilder.getRecentMessages(session)).thenReturn(List.of());
        when(messageRepository.save(any(ConversationMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(sessionRepository.save(any(ConversationSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConciergeChatResponse response = conciergeService.chat("Necesito una mesa", sessionToken, null);

        assertEquals(ConciergeActionSuggested.BOOK_RESTAURANT, response.getActionSuggested());
        assertTrue(response.getRespuesta().contains("restaurante"));
        verify(restTemplate, never()).exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }
}

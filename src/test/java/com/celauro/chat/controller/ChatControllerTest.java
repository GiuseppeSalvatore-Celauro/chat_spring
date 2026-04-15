package com.celauro.chat.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;

import com.celauro.chat.DTO.MessageResponseDTO;
import com.celauro.chat.exception.NotFoundException;
import com.celauro.chat.service.MessageService;
import com.celauro.chat.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

@WebMvcTest(ChatController.class)
public class ChatControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private MessageService messageService;
    @MockitoBean
    private UserService userService;

    // ========================
    // Create Message
    // ========================
    @Test
    void shouldCreateMessage()throws Exception{
        MessageResponseDTO response = createResponse("prova", "testo di prova");

        when(messageService.createMessage(any())).thenReturn(response);

        String json = """
                {
                    "username": "prova",
                    "text": "testo di prova"
                }
                """;

        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("prova"))
                .andExpect(jsonPath("$.text").value("testo di prova"));

        verify(messageService).createMessage(any());
    }

    // ========================
    // Create Message - edge case
    // ========================
    @Test
    void shouldThrowException_whenUsernameAndTextNull()throws Exception{
        String json = """
                {
                    "username": "",
                    "text": ""
                }
                """;

        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(messageService, never()).createMessage(any());
    }

    @Test
    void shouldThrowException_whenUsernameTooShort()throws Exception{
        String json = """
                {
                    "username": "sa",
                    "text": "ciao"
                }
                """;

        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(messageService, never()).createMessage(any());
    }

    @Test
    void shouldThrowException_whenUsernameAndTextTooLong()throws Exception{
        String json = """
                {
                    "username": "asdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdf",
                    "text": "ciaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociao"
                }
                """;

        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(messageService, never()).createMessage(any());
    }

    // ========================
    // Recent Message without limits
    // ========================
    @Test
    void shouldReturnListOfMessagesWithDescOrder() throws Exception{
        MessageResponseDTO response = createResponse("prova", "primo");
        MessageResponseDTO response1 = createResponse("prova", "secondo");
        MessageResponseDTO response2 = createResponse("prova", "terzo");

        when(messageService.getMessageDesc()).thenReturn(List.of(response2, response1, response));

        mockMvc.perform(get("/api/chat/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].text").value("terzo"));

        verify(messageService).getMessageDesc();
    }

    // ========================
    // Recent Message with limits
    // ========================
    @Test
    void shouldReturnNumberOfMessages() throws Exception{
        MessageResponseDTO response1 = createResponse("prova", "secondo");
        MessageResponseDTO response2 = createResponse("prova", "terzo");

        when(messageService.getRecentMessages(any(Integer.class))).thenReturn(List.of(response2, response1));

        mockMvc.perform(get("/api/chat/messages/recent?limit=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].text").value("terzo"));

        verify(messageService).getRecentMessages(2);
    }

    // ========================
    // User Messages
    // ========================
    @Test
    void shouldReturnUserMessages() throws Exception{
        MessageResponseDTO response = createResponse("prova", "primo");
        MessageResponseDTO response1 = createResponse("prova", "secondo");
        MessageResponseDTO response2 = createResponse("prova", "terzo");

        when(messageService.getUserMessages("prova")).thenReturn(List.of(response2, response1, response));

        mockMvc.perform(get("/api/chat/messages/user/prova"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].text").value("terzo"));

        verify(messageService).getUserMessages("prova");
    }

    // ========================
    // Delete message
    // ========================
    @Test
    void shouldDeleteMessages() throws Exception{
        MessageResponseDTO response = createResponse("prova", "primo");

        when(messageService.deleteMessage(1L)).thenReturn(response);

        mockMvc.perform(delete("/api/chat/messages/delete/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("prova"));

        verify(messageService).deleteMessage(1L);
    }

    // ========================
    // Delete message - edge case
    // ========================
    @Test
    void shouldThrowNotFoundException_whenMessageNotFound() throws Exception {
        when(messageService.deleteMessage(1L))
                .thenThrow(new NotFoundException("Not found"));

        mockMvc.perform(delete("/api/chat/messages/1"))
                .andExpect(status().isNotFound());
    }

    // ========================
    // Search message
    // ========================
    @Test
    void shouldReturnFilteredListOfMessage_whenBothUsernameAndTextContainsArePresent() throws Exception{
        MessageResponseDTO response = createResponse("prova", "primo");
        MessageResponseDTO response1 = createResponse("prova", "secondo");
        MessageResponseDTO response2 = createResponse("prova", "terzo");

        when(messageService.getFilteredList(any(), any(), any())).thenReturn(List.of(response1));

        mockMvc.perform(get("/api/chat/messages/search?username=prova&textContains=sec"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].text").value("secondo"));

        verify(messageService).getFilteredList(any(), any(), any());
    }

    // ========================
    // Search message - edge cases
    // ========================
    @Test
    void shouldReturnFilteredListOfMessage_whenOnlyUsernameIsPresent() throws Exception{
        MessageResponseDTO response = createResponse("prova", "primo");
        MessageResponseDTO response1 = createResponse("prova", "secondo");
        MessageResponseDTO response2 = createResponse("prova", "terzo");

        when(messageService.getFilteredList(any(), any(), any())).thenReturn(List.of(response2, response1, response));

        mockMvc.perform(get("/api/chat/messages/search?username=prova"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        verify(messageService).getFilteredList(any(), any(), any());
    }

    @Test
    void shouldReturnFilteredListOfMessage_whenOnlyTextContainsIsPresent() throws Exception{
        MessageResponseDTO response = createResponse("prova", "primo");
        MessageResponseDTO response1 = createResponse("prova", "secondo");
        MessageResponseDTO response2 = createResponse("prova", "terzo");

        when(messageService.getFilteredList(any(), any(), any())).thenReturn(List.of(response1));

        mockMvc.perform(get("/api/chat/messages/search?textContains=sec"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(messageService).getFilteredList(any(), any(), any());
    }

    private MessageResponseDTO createResponse(String username, String text){
        MessageResponseDTO r = new MessageResponseDTO();
        r.setUsername(username);
        r.setText(text);
        return r;
    }
}

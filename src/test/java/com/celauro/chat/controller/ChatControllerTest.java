package com.celauro.chat.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;

import com.celauro.chat.DTO.*;
import com.celauro.chat.exception.NotFoundException;
import com.celauro.chat.service.MessageService;
import com.celauro.chat.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Objects;

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
        MessageResponseDTO response = createResponse("prova", "provaReceiver" ,"testo di prova");

        when(messageService.createMessage(any())).thenReturn(response);

        String json = """
                {
                    "sender": "prova",
                    "receiver": "provaReceiver",
                    "text": "testo di prova"
                }
                """;

        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sender").value("prova"))
                .andExpect(jsonPath("$.receiver").value("provaReceiver"))
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
                    "sender": "",
                    "receiver": "",
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
                    "sender": "sa",
                    "receiver": "or",
                    "text": "testo di prova"
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
                    "sender": "asdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdf",
                    "receiver": "pippo",
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
        MessageResponseDTO response = createResponse("prova", "pippo", "primo");
        MessageResponseDTO response1 = createResponse("prova", "pippo", "secondo");
        MessageResponseDTO response2 = createResponse("prova", "pippo", "terzo");

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
        MessageResponseDTO response1 = createResponse("prova", "pippo","secondo");
        MessageResponseDTO response2 = createResponse("prova", "pippo","terzo");

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
        MessageResponseDTO response = createResponse("prova", "pippo","primo");
        MessageResponseDTO response1 = createResponse("prova", "pippo","secondo");
        MessageResponseDTO response2 = createResponse("prova", "pippo","terzo");

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
        MessageResponseDTO response = createResponse("prova", "pippo","primo");

        when(messageService.deleteMessage(1L)).thenReturn(response);

        mockMvc.perform(delete("/api/chat/messages/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sender").value("prova"));

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
        MessageResponseDTO response = createResponse("prova", "pippo","secondo");

        when(messageService.getFilteredList(any(), any(), any())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/chat/messages/search?username=prova&textContains=sec"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].text").value("secondo"));

        verify(messageService).getFilteredList(eq(20), eq("prova"), eq("sec"));
    }

    // ========================
    // Search message - edge cases
    // ========================
    @Test
    void shouldReturnFilteredListOfMessage_whenOnlyUsernameIsPresent() throws Exception{
        MessageResponseDTO response = createResponse("prova", "pippo","primo");
        MessageResponseDTO response1 = createResponse("prova", "pippo","secondo");
        MessageResponseDTO response2 = createResponse("prova", "pippo","terzo");

        when(messageService.getFilteredList(any(), any(), any())).thenReturn(List.of(response2, response1, response));

        mockMvc.perform(get("/api/chat/messages/search?username=prova"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        verify(messageService).getFilteredList(eq(20), eq("prova"), eq(null));
    }

    @Test
    void shouldReturnFilteredListOfMessage_whenOnlyTextContainsIsPresent() throws Exception{
        MessageResponseDTO response = createResponse("prova", "pippo","secondo");

        when(messageService.getFilteredList(any(), any(), any())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/chat/messages/search?textContains=sec"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(messageService).getFilteredList(eq(20), eq(null), eq("sec"));
    }

    @Test
    void shouldReturnFilteredListOfMessage_whenNoParams() throws Exception{
        MessageResponseDTO response = createResponse("prova", "pippo","primo");
        MessageResponseDTO response1 = createResponse("prova", "pippo","secondo");
        MessageResponseDTO response2 = createResponse("prova", "pippo","terzo");

        when(messageService.getFilteredList(any(), any(), any())).thenReturn(List.of(response2, response1, response));

        mockMvc.perform(get("/api/chat/messages/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        verify(messageService).getFilteredList(eq(20), eq(null), eq(null));
    }

    // ========================
    // Count messages
    // ========================
    @Test
    void shouldReturnNumerOfMessages()throws Exception{
        MessageCountResponseDTO response = new MessageCountResponseDTO("test", 4);

        when(messageService.getCountOfMessages("test")).thenReturn(response);

        mockMvc.perform(get("/api/chat/messages/count?username=test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("test"))
                .andExpect(jsonPath("$.count").value(4));

        verify(messageService).getCountOfMessages("test");
    }

    // ========================
    // Count messages - edge case
    // ========================
    @Test
    void shouldReturn404_whenUserDoesNotExist()throws Exception{
        when(messageService.getCountOfMessages("test")).thenThrow(new NotFoundException("Not found"));

        mockMvc.perform(get("/api/chat/messages/count?username=test"))
                .andExpect(status().isNotFound());
    }

    // ========================
    // Conversation between users
    // ========================
    @Test
    void shouldReturnConversationBetweenUsers()throws  Exception{
        MessageResponseDTO r = createResponse("prova", "pippo","primo");
        MessageResponseDTO r1 = createResponse("pippo", "prova","secondo");
        MessageResponseDTO r2 = createResponse("prova", "pippo","terzo");

        when(messageService.getConversationsBetweenUsers("prova", "pippo")).thenReturn(List.of(r2,r1,r));

        mockMvc.perform(get("/api/chat/messages/conversation?user1=prova&user2=pippo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].sender").value("prova"))
                .andExpect(jsonPath("$[0].receiver").value("pippo"));

        verify(messageService).getConversationsBetweenUsers("prova", "pippo");
    }
    // ========================
    // Conversation between users - edge case
    // ========================
    @Test
    void shouldThrowException_whenSenderDoesNotExist()throws Exception{
        when(messageService.getConversationsBetweenUsers(eq("salvatore"), any()))
                .thenThrow(new NotFoundException("not found"));

        mockMvc.perform(get("/api/chat/messages/conversation?user1=salvatore&user2=pippo"))
                .andExpect(status().isNotFound());

        verify(messageService).getConversationsBetweenUsers(eq("salvatore"), any());
    }

    @Test
    void shouldReturn400_whenMissingParams()throws Exception{
        mockMvc.perform(get("/api/chat/messages/conversation"))
                .andExpect(status().isBadRequest());
    }

    // ========================
    // All user conversations
    // ========================
    @Test
    void shouldReturnUserConversation()throws  Exception{
        ConversationResponseDTO r = createConversationResponse("pippo", "primo",1L);
        ConversationResponseDTO r1 = createConversationResponse("marco", "secondo",2L);

        when(messageService.getUserConversations("salvatore")).thenReturn(List.of(r1,r));

        mockMvc.perform(get("/api/chat/messages/conversations/salvatore"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].withUser").value("marco"))
                .andExpect(jsonPath("$[1].withUser").value("pippo"));

        verify(messageService).getUserConversations("salvatore");
    }

    // ========================
    // All user conversations - edge case
    // ========================
    @Test
    void shouldReturnEmptyList_whenUserDoesNotHaveConversation()throws Exception{

        mockMvc.perform(get("/api/chat/messages/conversations/salvatore"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(messageService).getUserConversations(eq("salvatore"));
    }

    // ========================
    // Create user
    // ========================
    @Test
    void shouldCreateUser()throws Exception{
        UserResponseDTO response = createUserResponse("test", false, 1L);

        when(userService.createUser(any())).thenReturn(response);

        UserRequestDTO request = new UserRequestDTO();
        request.setUsername("test");

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        mockMvc.perform(post("/api/chat/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Username").value("test"))
                .andExpect(jsonPath("$.online").value("false"));

        verify(userService).createUser(any());
    }

    // ========================
    // Login user
    // ========================
    @Test
    void shouldReturnUserInOnlineStatus() throws Exception {
        UserResponseDTO response = createUserResponse("test", true, 1L);

        when(userService.userLogin(any())).thenReturn(response);

        UserRequestDTO request = new UserRequestDTO();
        request.setUsername("test");

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        mockMvc.perform(put("/api/chat/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Username").value("test"))
                .andExpect(jsonPath("$.online").value("true"));

        verify(userService).userLogin(any());
    }

    // ========================
    // Logout user
    // ========================
    @Test
    void shouldReturnUserInOfflineStatus() throws Exception {
        UserResponseDTO response = createUserResponse("test", false, 1L);

        when(userService.userLogout(any())).thenReturn(response);

        UserRequestDTO request = new UserRequestDTO();
        request.setUsername("test");

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        mockMvc.perform(put("/api/chat/users/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Username").value("test"))
                .andExpect(jsonPath("$.online").value("false"));

        verify(userService).userLogout(any());
    }

    // ========================
    // Status user
    // ========================
    @Test
    void shouldReturnUserStatus() throws Exception {
        UserResponseDTO response = createUserResponse("test", false, 1L);

        when(userService.getUserStatus(any())).thenReturn(response);

        mockMvc.perform(get("/api/chat/users/test/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Username").value("test"))
                .andExpect(jsonPath("$.online").value("false"));

        verify(userService).getUserStatus(any());
    }
    // ========================
    // Helper methods
    // ========================
    private UserResponseDTO createUserResponse(String username, boolean isOnline, long lastSeen){
        UserResponseDTO u = new UserResponseDTO();
        u.setOnline(isOnline);
        u.setUsername(username);
        u.setLastSeen(lastSeen);
        return u;
    }

    private MessageResponseDTO createResponse(String senderUsername, String receiverUsername, String text){
        MessageResponseDTO r = new MessageResponseDTO();
        r.setSender(senderUsername);
        r.setReceiver(receiverUsername);
        r.setText(text);
        return r;
    }

    private ConversationResponseDTO createConversationResponse(String withUser, String lastMessage, Long timestamp){
        ConversationResponseDTO c = new ConversationResponseDTO();
        c.setTimestamp(timestamp);
        c.setWithUser(withUser);
        c.setLastMessage(lastMessage);
        return c;
    }
}

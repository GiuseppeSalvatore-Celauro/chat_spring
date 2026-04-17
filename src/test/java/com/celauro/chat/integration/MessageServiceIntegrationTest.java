package com.celauro.chat.integration;

import com.celauro.chat.DTO.*;
import com.celauro.chat.entity.Message;
import com.celauro.chat.exception.NotFoundException;
import com.celauro.chat.repository.MessageRepository;
import com.celauro.chat.service.MessageService;
import com.celauro.chat.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.transaction.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class MessageServiceIntegrationTest {
    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserService userService;

    // ========================
    // Create message
    // ========================
    @Test
    void shouldCreateAndPersistMessage(){
        userService.createUser(createUserRequest("prova"));
        userService.createUser(createUserRequest("provaReceiver"));

        MessageResponseDTO response = messageService.createMessage(createMessageRequest("prova", "provaReceiver","Testo di prova"));

        assertNotNull(response);

        List<Message> messages = messageRepository.findAll();

        assertEquals(1, messages.size());
        assertEquals("prova", messages.getFirst().getSender().getUsername());
        assertEquals("provaReceiver", messages.getFirst().getReceiver().getUsername());
        assertEquals("Testo di prova", messages.getFirst().getText());
    }

    // ========================
    // Recent messages
    // ========================
    @Test
    void shouldReturnRecentMessages(){
        userService.createUser(createUserRequest("prova"));
        userService.createUser(createUserRequest("prova1"));
        userService.createUser(createUserRequest("prova2"));

        messageService.createMessage(createMessageRequest("prova", "prova1", "Primo"));
        messageService.createMessage(createMessageRequest("prova1", "prova1", "Secondo"));
        messageService.createMessage(createMessageRequest("prova2", "prova1", "Terzo"));

        List<MessageResponseDTO> messages = messageService.getRecentMessages(2);

        assertEquals(2, messages.size());
        assertEquals("Terzo", messages.get(0).getText());
        assertEquals("Secondo", messages.get(1).getText());
    }

    // ========================
    // Delete message
    // ========================
    @Test
    void shouldDeleteMessage(){
        userService.createUser(createUserRequest("prova"));
        userService.createUser(createUserRequest("prova1"));
        userService.createUser(createUserRequest("prova2"));

        messageService.createMessage(createMessageRequest("prova", "prova1", "Primo"));
        MessageResponseDTO response = messageService.createMessage(createMessageRequest("prova1", "prova", "Secondo"));
        messageService.createMessage(createMessageRequest("prova2", "prova1", "Terzo"));


        MessageResponseDTO message = messageService.deleteMessage(response.getId());

        List<Message> messages = messageRepository.findAll();

        assertEquals(2, messages.size());
        assertEquals("prova1", message.getSender());
        assertEquals("prova", message.getReceiver());
        assertEquals("Secondo", message.getText());
    }

    // ========================
    // Search message from username
    // ========================
    @Test
    void shouldReturnUserMessagesFromSearchQuery(){
        userService.createUser(createUserRequest("salvatore"));
        userService.createUser(createUserRequest("prova"));

        messageService.createMessage(createMessageRequest("salvatore", "prova", "primo"));
        messageService.createMessage(createMessageRequest("salvatore", "prova", "secondo"));
        messageService.createMessage(createMessageRequest("salvatore", "prova", "terzo"));

        List<MessageResponseDTO> messages = messageService.getFilteredList(20, "salvatore", null);

        assertEquals(3, messages.size());
        assertTrue(messages.stream()
                .allMatch(m -> m.getSender().equals("salvatore")));
    }

    // ========================
    // Search message from username - edge case
    // ========================
    @Test
    void shouldThrowException_whenUserHasNoMessages_InIntegration(){
        userService.createUser(createUserRequest("salvatore"));

        assertThrows(NotFoundException.class, () -> messageService.getFilteredList(20, "salvatore", null));

    }

    // ========================
    // Search message from text
    // ========================
    @Test
    void shouldReturnMessagesThatContainText(){
        userService.createUser(createUserRequest("salvatore"));
        userService.createUser(createUserRequest("pippo"));
        userService.createUser(createUserRequest("mario"));


        messageService.createMessage(createMessageRequest("salvatore", "mario","primo"));
        messageService.createMessage(createMessageRequest("pippo", "mario","secondo"));
        messageService.createMessage(createMessageRequest("mario", "pippo","terzo"));

        List<MessageResponseDTO> messages = messageService.getFilteredList(20, null, "sec");

        assertEquals(1, messages.size());
        assertEquals("pippo", messages.getFirst().getSender());
        assertEquals("mario", messages.getFirst().getReceiver());
        assertEquals("secondo", messages.getFirst().getText());
    }

    // ========================
    // Search message from text - edge case
    // ========================
    @Test
    void shouldSearchTextIgnoringCase(){
        userService.createUser(createUserRequest("prova"));
        userService.createUser(createUserRequest("provaReceiver"));

        messageService.createMessage(createMessageRequest("prova", "provaReceiver","Secondo testo"));

        List<MessageResponseDTO> messages = messageService.getFilteredList(20, null, "secondo");

        assertEquals(1, messages.size());
    }
    // ========================
    // Search message from both username and text
    // ========================
    @Test
    void shouldReturnUserMessagesThatContainText(){
        userService.createUser(createUserRequest("salvatore"));
        userService.createUser(createUserRequest("pippo"));

        messageService.createMessage(createMessageRequest("salvatore", "pippo", "primo"));
        messageService.createMessage(createMessageRequest("pippo", "salvatore","secondo"));
        messageService.createMessage(createMessageRequest("salvatore", "pippo","terzo"));

        List<MessageResponseDTO> messages = messageService.getFilteredList(20, "salvatore", "te");

        assertEquals(1, messages.size());
        assertEquals("salvatore", messages.getFirst().getSender());
        assertEquals("pippo",messages.getFirst().getReceiver());
        assertEquals("terzo", messages.getFirst().getText());
    }

    // ========================
    // Search message with default value
    // ========================
    @Test
    void shouldReturn20Messages(){
        userService.createUser(createUserRequest("salvatore"));
        userService.createUser(createUserRequest("pippo"));

        for(int i = 0; i <= 30; i++){
            messageService.createMessage(createMessageRequest("salvatore", "pippo", "msg n:" + i));
        }

        List<MessageResponseDTO> messages = messageService.getFilteredList(20, null, null);

        assertEquals(20, messages.size());
        assertEquals("salvatore", messages.getFirst().getSender());
        assertEquals("pippo", messages.getFirst().getReceiver());
        assertEquals("msg n:30", messages.getFirst().getText());
        assertEquals("msg n:11", messages.get(19).getText());
    }

    // ========================
    // Count messages
    // ========================
    @Test
    void shouldReturnNumberOfMessages(){
        userService.createUser(createUserRequest("salvatore"));
        userService.createUser(createUserRequest("receiver"));

        for(int i = 0; i < 5; i++){
            messageService.createMessage(createMessageRequest("salvatore", "receiver", "msg n:" + i));
        }

        MessageCountResponseDTO response = messageService.getCountOfMessages("salvatore");

        assertEquals("salvatore", response.getUsername());
        assertEquals(5, response.getCount());
    }

    // ========================
    // Count messages - edge case
    // ========================
    @Test
    void shouldThrowException_whenUserDoesNotExist(){
        assertThrows(NotFoundException.class, ()-> messageService.getCountOfMessages("test"));
    }

    @Test
    void shouldThrowException_whenUserHasNoMessages(){
        userService.createUser(createUserRequest("test"));

        assertThrows(NotFoundException.class, ()-> messageService.getCountOfMessages("test"));

    }

    // ========================
    // Conversation between users
    // ========================
    @Test
    void shouldReturnConversationBetweenUsers(){
        userService.createUser(createUserRequest("salvatore"));
        userService.createUser(createUserRequest("pippo"));

        messageService.createMessage(createMessageRequest("salvatore", "pippo", "primo messaggio"));
        messageService.createMessage(createMessageRequest("pippo", "salvatore", "secondo messaggio"));
        messageService.createMessage(createMessageRequest("salvatore", "pippo", "terzo messaggio"));

        List<MessageResponseDTO> r = messageService.getConversationsBetweenUsers("salvatore", "pippo");

        assertEquals(3, r.size());
        assertEquals("salvatore", r.getFirst().getSender());
        assertEquals("pippo", r.getFirst().getReceiver());
        assertEquals("terzo messaggio", r.getFirst().getText());
        assertEquals("primo messaggio", r.getLast().getText());
    }

    // ========================
    // Conversation between users - edge cases
    // ========================
    @Test
    void shouldReturnEmptyList_whenConversationBetweenUsersDoesNotExist(){
        userService.createUser(createUserRequest("salvatore"));
        userService.createUser(createUserRequest("pippo"));

        List<MessageResponseDTO> r = messageService.getConversationsBetweenUsers("salvatore", "pippo");

        assertEquals(0, r.size());
    }

    // ========================
    // All user conversations
    // ========================
    @Test
    void shouldReturnListOfUserConversation(){
        userService.createUser(createUserRequest("salvatore"));
        userService.createUser(createUserRequest("pippo"));
        userService.createUser(createUserRequest("marco"));

        messageService.createMessage(createMessageRequest("salvatore", "marco", "primo messaggio"));
        messageService.createMessage(createMessageRequest("pippo", "salvatore", "secondo messaggio"));
        messageService.createMessage(createMessageRequest("marco", "salvatore", "terzo messaggio"));

        List<ConversationResponseDTO> r = messageService.getUserConversations("salvatore");

        assertEquals(2, r.size());
        assertEquals("marco", r.getFirst().getWithUser());
        assertEquals("pippo", r.getLast().getWithUser());
        assertEquals("terzo messaggio", r.getFirst().getLastMessage());
        assertEquals("secondo messaggio", r.getLast().getLastMessage());
    }

    // ========================
    // All user conversations - edge cases
    // ========================
    @Test
    void shouldReturnEmptyList_whenUserDoesNotHaveConversation(){
        userService.createUser(createUserRequest("salvatore"));
        userService.createUser(createUserRequest("pippo"));

        List<ConversationResponseDTO> r = messageService.getUserConversations("salvatore");

        assertEquals(0, r.size());
    }


    // ========================
    // Helper methods
    // ========================
    private MessageRequestDTO createMessageRequest(String sender, String receiver, String text){
        MessageRequestDTO r = new MessageRequestDTO();
        r.setSender(sender);
        r.setReceiver(receiver);
        r.setText(text);
        return r;
    }

    private UserRequestDTO createUserRequest(String username){
        UserRequestDTO r = new UserRequestDTO();
        r.setUsername(username);
        return r;
    }




}

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

        MessageResponseDTO response = messageService.createMessage(createMessageRequest("prova", "Testo di prova"));

        assertNotNull(response);

        List<Message> messages = messageRepository.findAll();

        assertEquals(1, messages.size());
        assertEquals("prova", messages.getFirst().getUser().getUsername());
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

        messageService.createMessage(createMessageRequest("prova", "Primo"));
        messageService.createMessage(createMessageRequest("prova1", "Secondo"));
        messageService.createMessage(createMessageRequest("prova2", "Terzo"));

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

        messageService.createMessage(createMessageRequest("prova", "Primo"));
        MessageResponseDTO response = messageService.createMessage(createMessageRequest("prova1", "Secondo"));
        messageService.createMessage(createMessageRequest("prova2", "Terzo"));


        MessageResponseDTO message = messageService.deleteMessage(response.getId());

        List<Message> messages = messageRepository.findAll();

        assertEquals(2, messages.size());
        assertEquals("prova1", message.getUsername());
        assertEquals("Secondo", message.getText());
    }

    // ========================
    // Search message from username
    // ========================
    @Test
    void shouldReturnUserMessagesFromSearchQuery(){
        userService.createUser(createUserRequest("salvatore"));

        messageService.createMessage(createMessageRequest("salvatore", "primo"));
        messageService.createMessage(createMessageRequest("salvatore", "secondo"));
        messageService.createMessage(createMessageRequest("salvatore", "terzo"));

        List<MessageResponseDTO> messages = messageService.getFilteredList(20, "salvatore", null);

        assertEquals(3, messages.size());
        assertTrue(messages.stream()
                .allMatch(m -> m.getUsername().equals("salvatore")));
    }

    // ========================
    // Search message from username - edge case
    // ========================
    @Test
    void shouldThrowException_whenUserHasNoMessages_InIntegration(){
        userService.createUser(createUserRequest("salvatore"));

        assertThrows(NotFoundException.class, () -> {
           messageService.getFilteredList(20, "salvatore", null);
        });

    }

    // ========================
    // Search message from text
    // ========================
    @Test
    void shouldReturnMessagesThatContainText(){
        userService.createUser(createUserRequest("salvatore"));
        userService.createUser(createUserRequest("pippo"));
        userService.createUser(createUserRequest("mario"));


        messageService.createMessage(createMessageRequest("salvatore", "primo"));
        messageService.createMessage(createMessageRequest("pippo", "secondo"));
        messageService.createMessage(createMessageRequest("mario", "terzo"));

        List<MessageResponseDTO> messages = messageService.getFilteredList(20, null, "sec");

        assertEquals(1, messages.size());
        assertEquals("pippo", messages.getFirst().getUsername());
        assertEquals("secondo", messages.getFirst().getText());
    }

    // ========================
    // Search message from text - edge case
    // ========================
    @Test
    void shouldSearchTextIgnoringCase(){
        userService.createUser(createUserRequest("prova"));

        messageService.createMessage(createMessageRequest("prova", "Secondo testo"));

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

        messageService.createMessage(createMessageRequest("salvatore", "primo"));
        messageService.createMessage(createMessageRequest("pippo", "secondo"));
        messageService.createMessage(createMessageRequest("salvatore", "terzo"));

        List<MessageResponseDTO> messages = messageService.getFilteredList(20, "salvatore", "te");

        assertEquals(1, messages.size());
        assertEquals("salvatore", messages.getFirst().getUsername());
        assertEquals("terzo", messages.getFirst().getText());
    }

    // ========================
    // Search message with default value
    // ========================
    @Test
    void shouldReturn20Messages(){
        userService.createUser(createUserRequest("salvatore"));

        for(int i = 0; i <= 30; i++){
            messageService.createMessage(createMessageRequest("salvatore", "msg n:" + i));
        }

        List<MessageResponseDTO> messages = messageService.getFilteredList(20, null, null);

        assertEquals(20, messages.size());
        assertEquals("salvatore", messages.getFirst().getUsername());
        assertEquals("msg n:30", messages.getFirst().getText());
        assertEquals("msg n:11", messages.get(19).getText());
    }

    // ========================
    // Count messages
    // ========================
    @Test
    void shouldReturnNumberOfMessages(){
        userService.createUser(createUserRequest("salvatore"));

        for(int i = 0; i < 5; i++){
            messageService.createMessage(createMessageRequest("salvatore", "msg n:" + i));
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
        assertThrows(NotFoundException.class, ()->{
            messageService.getCountOfMessages("test");
        });
    }

    @Test
    void shouldThrowException_whenUserHasNoMessages(){
        userService.createUser(createUserRequest("test"));

        assertThrows(NotFoundException.class, ()->{
           messageService.getCountOfMessages("test");
        });

    }

    private MessageRequestDTO createMessageRequest(String username, String text){
        MessageRequestDTO r = new MessageRequestDTO();
        r.setUsername(username);
        r.setText(text);
        return r;
    }

    private UserRequestDTO createUserRequest(String username){
        UserRequestDTO r = new UserRequestDTO();
        r.setUsername(username);
        return r;
    }
}

package com.celauro.chat.integration;

import com.celauro.chat.DTO.MessageRequestDTO;
import com.celauro.chat.DTO.MessageResponseDTO;
import com.celauro.chat.entity.Message;
import com.celauro.chat.repository.MessageRepository;
import com.celauro.chat.service.MessageService;
import com.celauro.chat.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.transaction.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class MessageServiceIntegrationTest {
    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageRepository messageRepository;

    // ========================
    // Create message
    // ========================
    @Test
    void shouldCreateAndPersistMessage(){
        MessageResponseDTO response = messageService.createMessage(createRequest("prova", "Testo di prova"));

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

        messageService.createMessage(createRequest("prova", "Primo"));
        messageService.createMessage(createRequest("prova1", "Secondo"));
        messageService.createMessage(createRequest("prova2", "Terzo"));

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
        messageService.createMessage(createRequest("prova", "Primo"));
        MessageResponseDTO response = messageService.createMessage(createRequest("prova1", "Secondo"));
        messageService.createMessage(createRequest("prova2", "Terzo"));


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
        messageService.createMessage(createRequest("salvatore", "primo"));
        messageService.createMessage(createRequest("salvatore", "secondo"));
        messageService.createMessage(createRequest("salvatore", "terzo"));

        List<MessageResponseDTO> messages = messageService.getFilteredList(20, "salvatore", null);

        assertEquals(3, messages.size());
        assertEquals("salvatore", messages.get(0).getUsername());
        assertEquals("salvatore", messages.get(1).getUsername());
        assertEquals("salvatore", messages.get(2).getUsername());
    }

    // ========================
    // Search message from text
    // ========================
    @Test
    void shouldReturnMessagesThatContainText(){
        messageService.createMessage(createRequest("salvatore", "primo"));
        messageService.createMessage(createRequest("pippo", "secondo"));
        messageService.createMessage(createRequest("mario", "terzo"));

        List<MessageResponseDTO> messages = messageService.getFilteredList(20, null, "sec");

        assertEquals(1, messages.size());
        assertEquals("pippo", messages.getFirst().getUsername());
        assertEquals("secondo", messages.getFirst().getText());
    }

    // ========================
    // Search message from both username and text
    // ========================
    @Test
    void shouldReturnUserMessagesThatContainText(){
        messageService.createMessage(createRequest("salvatore", "primo"));
        messageService.createMessage(createRequest("pippo", "secondo"));
        messageService.createMessage(createRequest("salvatore", "terzo"));

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
        for(int i = 0; i <= 30; i++){
            messageService.createMessage(createRequest("salvatore", "msg n:" + i));
        }

        List<MessageResponseDTO> messages = messageService.getFilteredList(20, null, null);

        assertEquals(20, messages.size());
        assertEquals("salvatore", messages.getFirst().getUsername());
        assertEquals("msg n:30", messages.getFirst().getText());
    }


    private MessageRequestDTO createRequest(String username, String text){
        MessageRequestDTO r = new MessageRequestDTO();
        r.setUsername(username);
        r.setText(text);
        return r;
    }
}

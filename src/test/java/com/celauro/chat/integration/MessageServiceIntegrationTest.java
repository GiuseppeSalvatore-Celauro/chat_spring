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

    private MessageRequestDTO createRequest(String username, String text){
        MessageRequestDTO r = new MessageRequestDTO();
        r.setUsername(username);
        r.setText(text);
        return r;
    }
}

package com.celauro.chat.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import com.celauro.chat.DTO.MessageRequestDTO;
import com.celauro.chat.DTO.MessageResponseDTO;
import com.celauro.chat.entity.Message;
import com.celauro.chat.entity.User;
import com.celauro.chat.exception.NotFoundException;
import com.celauro.chat.repository.MessageRepository;
import com.celauro.chat.service.MessageService;
import com.celauro.chat.service.NotificationService;
import com.celauro.chat.service.UserService;

@ExtendWith(MockitoExtension.class)
public class MessageServiceUnitTest {
    @Mock
    private  MessageRepository messageRepository;
    @Mock
    private UserService userService;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private MessageService messageService;
    

    // ========================
    // Create message
    // ========================
    @Test
    void shouldCreateMessage_whenValidRequest_returnCorrectResponse(){
        MessageRequestDTO request = new MessageRequestDTO();
        request.setUsername("testUsername");
        request.setText("ciao sono un test");

        User user = new User();
        user.setUsername(request.getUsername());

        when(userService.getOrCreateUser("testUsername")).thenReturn(user);
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MessageResponseDTO response = messageService.createMessage(request);

        assertNotNull(response);
        assertEquals("testUsername", response.getUsername());
        assertEquals("ciao sono un test", response.getText());
        assertTrue(response.getTimestamp() > 0);

        verify(userService).getOrCreateUser("testUsername");
        verify(messageRepository).save(any(Message.class));
    }

    // ========================
    // Create Message - Edge case
    // ========================
    @Test
    void shouldThrowException_whenRepositoryFailsDuringSave(){
        MessageRequestDTO request = new MessageRequestDTO();
        request.setUsername("testUsername");
        request.setText("ciao sono un test");

        User user = new User();
        user.setUsername(request.getUsername());

        when(userService.getOrCreateUser("testUsername")).thenReturn(user);
        when(messageRepository.save(any())).thenThrow(new RuntimeException("DB down"));

        assertThrows(RuntimeException.class, () ->{
            messageService.createMessage(request);
        });

        verify(messageRepository).save(any());
    }

    // ========================
    // Recent Messages
    // ========================
    @Test
    void shouldReturnMessages_whenLimitsIsValid(){
        int limit = 1;

        User user = new User();
        user.setUsername("test");

        Message message = new Message();
        message.setUser(user);
        message.setText("test di limite");

        when(messageRepository.findLimitMessagesByOrderByTimestampDesc(any(PageRequest.class))).thenReturn(List.of(message));

        List<MessageResponseDTO> responses = messageService.getRecentMessages(limit);

        assertNotNull(responses);
        assertEquals(limit, responses.size());
        assertEquals("test", responses.getFirst().getUsername());
        assertEquals("test di limite", responses.getFirst().getText());
        verify(messageRepository).findLimitMessagesByOrderByTimestampDesc(any(PageRequest.class));
    }

    // ========================
    // Recent Messages - Edge cases
    // ========================
    @Test
    void shouldThrowException_whenLimitIsZeroOrNegative(){
        assertThrows(IllegalArgumentException.class, () -> {
            messageService.getRecentMessages(0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            messageService.getRecentMessages(-1);
        });
    }

    @Test
    void shouldReturnEmptyList_whenMessagesFound(){
        when(messageRepository.findLimitMessagesByOrderByTimestampDesc(any(PageRequest.class))).thenReturn(List.of());

        List<MessageResponseDTO> messages = messageService.getRecentMessages(1);

        assertTrue(messages.isEmpty());
        verify(messageRepository).findLimitMessagesByOrderByTimestampDesc(any());
    }
    
    @Test
    void shouldReturnLargeNumberOfMessages_whenRepositoryReturnsMany(){
        User user = new User();
        user.setUsername("test");
        List<Message> messages = new ArrayList<>();
        for(int i=0; i < 100; i++){
            Message message = new Message(user, "messaggio numero: " + i);
            messages.add(message);
        }

        when(messageRepository.findLimitMessagesByOrderByTimestampDesc(any(PageRequest.class))).thenReturn(messages);

        List<MessageResponseDTO> response = messageService.getRecentMessages(100);
        assertEquals(100, response.size());
        assertEquals("messaggio numero: 0", response.getFirst().getText());

        verify(messageRepository).findLimitMessagesByOrderByTimestampDesc(any(PageRequest.class));
    }

    @Test
    void shouldReturnAllMessages_whenRepositoryReturnsLessThenLimit(){
        User user = new User();
        user.setUsername("test");

        Message message = new Message();
        message.setUser(user);
        message.setText("Messaggio");

        when(messageRepository.findLimitMessagesByOrderByTimestampDesc(any(PageRequest.class))).thenReturn(List.of(message));

        List<MessageResponseDTO> response = messageService.getRecentMessages(100);
        assertEquals(1, response.size());
        assertEquals("Messaggio", response.getFirst().getText());

        verify(messageRepository).findLimitMessagesByOrderByTimestampDesc(any(PageRequest.class));

    }

    // ========================
    // Desc messages - Edge case
    // ========================
    @Test
    void shouldReturnEmptyList_whenNoMessagesExist(){
        when(messageRepository.findAllByOrderByTimestampDesc()).thenReturn(List.of());

        List<MessageResponseDTO> response = messageService.getMessageDesc();

        assertTrue(response.isEmpty());
    }

    // ========================
    // User messages - Edge case
    // ========================
    @Test
    void shouldThrowExceptionIfUsernameNotFound(){
        when(userService.getOrThrowExceptionUserByUsername("luca")).thenThrow(new NotFoundException("Nessun user trovato"));

        assertThrows(NotFoundException.class, () -> {
            messageService.getUserMessages("luca");
        });

        verify(userService).getOrThrowExceptionUserByUsername("luca");
    }

    // ========================
    // Delete message - Edge case
    // ========================
    @Test
    void shouldThrowExceptionIfMessageNotFound(){
        when(messageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            messageService.deleteMessage(1L);
        });

        verify(messageRepository).findById(1L);
    }

}

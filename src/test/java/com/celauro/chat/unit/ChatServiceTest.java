package com.celauro.chat.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.celauro.chat.DTO.MessageRequestDTO;
import com.celauro.chat.DTO.MessageResponseDTO;
import com.celauro.chat.entity.Message;
import com.celauro.chat.entity.User;
import com.celauro.chat.exception.NotFoundException;
import com.celauro.chat.repository.MessageRepository;
import com.celauro.chat.repository.UserRepository;
import com.celauro.chat.service.ChatService;
import com.celauro.chat.service.NotificationService;
import com.celauro.chat.service.UserService;

@ExtendWith(MockitoExtension.class)
public class ChatServiceTest {
    @Mock
    private  MessageRepository repository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private ChatService chatService;
    
    @Test
    void shouldCreateMessageSuccessffuly(){
        MessageRequestDTO request = new MessageRequestDTO();
        request.setUsername("testUsername");
        request.setText("ciao sono un test");

        User user = new User();
        user.setUsername(request.getUsername());

        when(userService.getOrCreateUser(any())).thenReturn(user);
        
        Message message = new Message();
        message.setUser(user);
        message.setText("ciao sono un test");
        message.setTimestamp(System.currentTimeMillis());

        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        MessageResponseDTO response = chatService.createMessage(request);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);

        // Test di verifica che i metodi siano stati chiamati almeno una volta
        verify(userService).getOrCreateUser("testUsername");
        verify(repository).save(captor.capture());
        
        Message captured = captor.getValue();
        
        // Test per contenuto del message
        assertNotNull(captured);
        assertEquals("testUsername", captured.getUser().getUsername());
        assertEquals("ciao sono un test", captured.getText());
        assertTrue(captured.getTimestamp() > 0);

        assertNotNull(response);
        assertEquals("testUsername", response.getUsername());
        assertEquals("ciao sono un test", response.getText());
        assertTrue(response.getTimestamp() > 0);

    }

    @Test
    void shouldThrowRuntimeException_whenRepoOff(){
        MessageRequestDTO request = new MessageRequestDTO();
        request.setUsername("testUsername");
        request.setText("ciao sono un test");

        when(repository.save(any())).thenThrow(new RuntimeException("DB down"));

        assertThrows(RuntimeException.class, () ->{
                chatService.createMessage(request);
        });
    }

    @Test
    void shouldVerifyTheCorrectCreationOfPageable(){
        int limit = 1;

        User user = new User();
        user.setUsername("test");

        Message message = new Message();
        message.setUser(user);
        message.setText("test di limite");

        when(repository.findLimitMessagesByOrderByTimestampDesc(any())).thenReturn(List.of(message));

        List<MessageResponseDTO> responses = chatService.getRecentMessages(limit);

        ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);

        verify(repository).findLimitMessagesByOrderByTimestampDesc(captor.capture());

        PageRequest page = captor.getValue();
        
        assertEquals(1, page.getPageSize());
        assertEquals(0, page.getPageNumber());

        assertNotNull(responses);
        assertEquals(limit, responses.size());
        assertEquals("test", responses.getFirst().getUsername());
        assertEquals("test di limite", responses.getFirst().getText());
    }

    @Test
    void shouldThrowIllegalArgumentException_whenLimitIsZeroOrLesser(){
        assertThrows(IllegalArgumentException.class, () -> {
            chatService.getRecentMessages(0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            chatService.getRecentMessages(-1);
        });
    }

    @Test
    void shouldReturnEmptyListOfMessages(){
        when(repository.findLimitMessagesByOrderByTimestampDesc(any())).thenReturn(List.of());
        List<MessageResponseDTO> messages = chatService.getRecentMessages(1);
        assertTrue(messages.isEmpty());
    }
    
    @Test
    void shouldReturnLargeNumberOfMessages(){
        List<Message> messages = new ArrayList<>();
        User user = new User();
        user.setUsername("test");
        for(int i=0; i < 100; i++){
            Message message = new Message(user, "messagio numero: " + i);
            messages.add(message);
        }

        when(repository.findLimitMessagesByOrderByTimestampDesc(any())).thenReturn(messages);

        List<MessageResponseDTO> response = chatService.getRecentMessages(100);
        assertEquals(100, response.size());
    }

    @Test
    void shouldReturnAllMessages_whenLimitIsGreaterThenList(){
        User user = new User();
        user.setUsername("test");

        Message message = new Message();
        message.setUser(user);
        message.setText("Messaggio");

        when(repository.findLimitMessagesByOrderByTimestampDesc(any())).thenReturn(List.of(message));

        List<MessageResponseDTO> response = chatService.getRecentMessages(100);
        assertEquals(1, response.size());
    }
    

    // @Test
    // void shouldThrowExceptionIfMessageNotFound(){
    //     when(repository.findById(1L)).thenReturn(Optional.empty());

    //     assertThrows(NotFoundException.class, () -> {
    //         chatService.deleteMessage(1L);
    //     });
    // }

    // @Test
    // void shouldReturnMessagesFromUsername(){
    //     User user = new User();
    //     user.setUsername("luca");

    //     Message message1 = new Message(user, "ciao");
    //     Message message2 = new Message(user, "ciao");

    //     when(repository.findByUserOrderByTimestampDesc(user))
    //         .thenReturn(List.of(message1, message2));

    //     when(userRepository.findByUsername(any()))
    //         .thenReturn(Optional.of(user));

    //     when(userRepository.findAll())
    //         .thenReturn(List.of(user));

    //     List<MessageResponseDTO> response = chatService.getUserMessages("luca");

    //     assertEquals(2, response.size());
    //     assertEquals("luca", response.get(0).getUsername());
    //     assertEquals(1, userRepository.findAll().size());
    // }

    // @Test
    // void shouldThrowExceptionIfUsernameNotFound(){
    //     when(userRepository.findByUsername(any())).thenReturn(Optional.empty());

    //     assertThrows(NotFoundException.class, () -> {
    //         chatService.getUserMessages("luca");
    //     });
    // }

    // @Test
    // void shouldReturnMessageFilteredByALimit(){
    //     User user = new User();
    //     user.setUsername("pippo");

    //     Message message = new Message(user, "ciao");

    //     when(repository.findLimitMessagesByOrderByTimestampDesc(any()))
    //         .thenReturn(List.of(message));

    //     List<MessageResponseDTO> response = chatService.getRecentMessages(1);
    //     assertEquals(1, response.size());

    //     ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
    //     verify(repository).findLimitMessagesByOrderByTimestampDesc(captor.capture());
    //     assertEquals(1, captor.getValue().getPageSize());
    //     assertEquals(0, captor.getValue().getPageNumber());
    // }

    // @Test 
    // void shouldThrowIllegalArgumentException(){
    //     assertThrows(IllegalArgumentException.class, () -> {
    //         chatService.getRecentMessages(0);
    //     });
    // }

}

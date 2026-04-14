package com.celauro.chat.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.celauro.chat.DTO.MessageRequestDTO;
import com.celauro.chat.DTO.MessageResponseDTO;
import com.celauro.chat.entity.Message;
import com.celauro.chat.entity.User;
import com.celauro.chat.exception.NotFoundException;
import com.celauro.chat.repository.MessageRepository;
import com.celauro.chat.utils.Logger;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final NotificationService notificationService;
    private final MessageRepository messageRespository;
    private final UserService userService;

    @Transactional
    public MessageResponseDTO createMessage(MessageRequestDTO request){
        
        User user = userService.getOrCreateUser(request.getUsername());

        Message message = createNewMessage(request, user);

        messageRespository.save(message);

        notificationService.createNotification(request.getUsername(), request.getText());

        Logger.info("Messaggio salvato");
        return toDto(message);
    }

    public List<MessageResponseDTO> getMessageDesc() {
        List<Message> messages = messageRespository.findAllByOrderByTimestampDesc();
        Logger.info("Spedita lista messaggi");
        return toListOfDto(messages);
    }

    public List<MessageResponseDTO> getRecentMessages(int limit) {
        if(limit <= 0) throw new IllegalArgumentException("Il limite deve essere maggiore di 0");
        
        PageRequest pageable = PageRequest.of(0, limit);

        List<Message> messages = messageRespository.findLimitMessagesByOrderByTimestampDesc(pageable);

        Logger.info("Spedita lista messaggi recenti");
        return toListOfDto(messages);
    }

    public List<MessageResponseDTO> getUserMessages(String username){
        User user = userService.getOrThrowExceptionUserByUsername(username);

        List<Message> messages = messageRespository.findByUserOrderByTimestampDesc(user);
                                            
        Logger.info("Spedita lista messaggi filtrati per utente");
        return toListOfDto(messages);
    }

    public MessageResponseDTO deleteMessage(long id) {
        Message message = messageRespository.findById(id).orElseThrow(() -> new NotFoundException("Messaggio non esiste"));
        
        messageRespository.delete(message);

        Logger.info("Messaggio eliminato");
        return toDto(message);
    }

    private List<MessageResponseDTO> toListOfDto(List<Message> messages){
        List<MessageResponseDTO> response = new ArrayList<>();
    
        for(Message message: messages){
            response.add(toDto(message));
        }

        return response;
    }

    private MessageResponseDTO toDto(Message message){
        MessageResponseDTO singleResponse = new MessageResponseDTO();
        singleResponse.setId(message.getId());
        singleResponse.setText(message.getText());
        singleResponse.setUsername(message.getUser().getUsername());
        singleResponse.setTimestamp(message.getTimestamp());
        return singleResponse;
    }
    
    private Message createNewMessage(MessageRequestDTO request, User user) {
        Message message = new Message();
        message.setText(request.getText());
        message.setUser(user);
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }
}

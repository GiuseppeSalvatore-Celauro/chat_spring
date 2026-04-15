package com.celauro.chat.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final MessageRepository messageRepository;
    private final UserService userService;

    private static final int MAX_AMOUNT_OF_MESSAGES = 100;

    @Transactional
    public MessageResponseDTO createMessage(MessageRequestDTO request){
        
        User user = userService.getOrCreateUser(request.getUsername());

        Message message = createNewMessage(request, user);

        messageRepository.save(message);

        notificationService.createNotification(request.getUsername(), request.getText());

        Logger.info("Messaggio salvato");
        return toDto(message);
    }

    public List<MessageResponseDTO> getMessageDesc() {
        List<Message> messages = messageRepository.findAllByOrderByTimestampDesc();
        Logger.info("Spedita lista messaggi");
        return toListOfDto(messages);
    }

    public List<MessageResponseDTO> getRecentMessages(int limit) {
        if(limit <= 0) throw new IllegalArgumentException("Il limite deve essere maggiore di 0");
        
        PageRequest pageable = PageRequest.of(0, limit);

        List<Message> messages = messageRepository.findLimitMessagesByOrderByTimestampDesc(pageable);

        Logger.info("Spedita lista messaggi recenti");
        return toListOfDto(messages);
    }

    public List<MessageResponseDTO> getUserMessages(String username){
        User user = userService.getOrThrowExceptionUserByUsername(username);

        List<Message> messages = messageRepository.findByUserOrderByTimestampDesc(user);
                                            
        Logger.info("Spedita lista messaggi filtrati per utente");
        return toListOfDto(messages);
    }

    public MessageResponseDTO deleteMessage(long id) {
        Message message = messageRepository.findById(id).orElseThrow(() -> new NotFoundException("Messaggio non esiste"));
        
        messageRepository.delete(message);

        Logger.info("Messaggio eliminato");
        return toDto(message);
    }

    public List<MessageResponseDTO> getFilteredList(Integer limit, String username, String textContains) {
        if(limit <= 0) throw  new IllegalArgumentException("Non sono ammessi limiti inferiori a zero");
        if(limit > MAX_AMOUNT_OF_MESSAGES) limit = MAX_AMOUNT_OF_MESSAGES;

        PageRequest pageable = PageRequest.of(0, limit);

        List<Message> messageList = new ArrayList<>();

        if(username != null && textContains != null) {
            messageList = messageRepository.findMessageByUserUsernameAndTextContainingIgnoreCaseOrderByTimestampDesc(username, textContains, pageable);
        }else if(textContains != null){
            messageList = messageRepository.findMessageByTextContainingIgnoreCaseOrderByTimestampDesc(textContains, pageable);
        }else if(username != null){
            messageList = messageRepository.findMessageByUserUsernameOrderByTimestampDesc(username, pageable);
        }else{
            return getRecentMessages(limit);
        }

        return toListOfDto(messageList);
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

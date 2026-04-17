package com.celauro.chat.service;

import java.util.ArrayList;
import java.util.List;

import com.celauro.chat.DTO.MessageCountResponseDTO;
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
    private final MessageRepository messageRepository;
    private final UserService userService;

    private static final int MAX_AMOUNT_OF_MESSAGES = 100;

    @Transactional
    public MessageResponseDTO createMessage(MessageRequestDTO request){
        
        User senderUser = userService.getOrThrowExceptionUserByUsername(request.getSender(), "User mandante non trovato");
        User reciverUser = userService.getOrThrowExceptionUserByUsername(request.getReceiver(), "User ricevente non trovato");

        Message message = createNewMessage(request, senderUser, reciverUser);

        messageRepository.save(message);

        notificationService.createNotification(request.getReceiver(), request.getText());

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
        User user = userService.getOrThrowExceptionUserByUsername(username, "User non trovato");

        List<Message> messages = messageRepository.findBySenderOrderByTimestampDesc(user);
                                            
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

        boolean hasUsername = username != null && !username.isBlank();
        boolean hasText = textContains != null && !textContains.isBlank();

        if(!hasText && !hasUsername) return getRecentMessages(limit);

        PageRequest pageable = PageRequest.of(0, limit);

        List<Message> messageList;

        if(hasUsername && hasText) {
            messageList = messageRepository.findMessageBySenderUsernameAndTextContainingIgnoreCaseOrderByTimestampDesc(username, textContains, pageable);
        }else if(hasText){
            messageList = messageRepository.findMessageByTextContainingIgnoreCaseOrderByTimestampDesc(textContains, pageable);
        }else{
            messageList = messageRepository.findMessageBySenderUsernameOrderByTimestampDesc(username, pageable);
        }

        if(messageList.isEmpty()){
            throw new NotFoundException("Non ci sono elementi");
        }

        Logger.info("Spedita lista messaggi filtrati");
        return toListOfDto(messageList);
    }

    public MessageCountResponseDTO getCountOfMessages(String username) {
        User user = userService.getOrThrowExceptionUserByUsername(username, "User non trovato");
        int numberOfMessages = messageRepository.countMessageBySenderUsername(user.getUsername());

        if(numberOfMessages <= 0){
            throw new NotFoundException("Questo utente non ha mandato nessun messaggio");
        }

        Logger.info("Spedito conteggio dei messaggi totali");
        return new MessageCountResponseDTO(username, numberOfMessages);
    }

    public List<MessageResponseDTO> getConversationsBetweenUsers(String username1, String username2) {
        userService.getOrThrowExceptionUserByUsername(username1, "Primo user non esiste");
        userService.getOrThrowExceptionUserByUsername(username2, "Secondo user non esiste");

        List<Message> conversation = messageRepository.findConversation(username1, username2);

        return toListOfDto(conversation);
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
        singleResponse.setSender(message.getSender().getUsername());
        singleResponse.setReceiver(message.getReceiver().getUsername());
        singleResponse.setTimestamp(message.getTimestamp());
        return singleResponse;
    }
    
    private Message createNewMessage(MessageRequestDTO request, User senderUser, User reciverUser) {
        Message message = new Message();
        message.setText(request.getText());
        message.setSender(senderUser);
        message.setReceiver(reciverUser);
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }
}

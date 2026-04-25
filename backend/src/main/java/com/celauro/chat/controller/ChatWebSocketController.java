package com.celauro.chat.controller;

import com.celauro.chat.DTO.MessageRequestDTO;
import com.celauro.chat.service.MessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    public ChatWebSocketController(SimpMessagingTemplate messagingTemplate, MessageService messageService){
        this.messagingTemplate = messagingTemplate;
        this.messageService = messageService;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(MessageRequestDTO request){
        messageService.createMessage(request);

        messagingTemplate.convertAndSend(
                "/topic/messages/" + request.getReceiver(),
                request
        );
    }
}

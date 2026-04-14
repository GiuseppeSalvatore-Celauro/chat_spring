package com.celauro.chat.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.celauro.chat.DTO.MessageRequestDTO;
import com.celauro.chat.DTO.MessageResponseDTO;
import com.celauro.chat.service.MessageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    // private final MessageRepository repository;
    private final MessageService service;

    @GetMapping("/hello")
    public String hello(){
        return "Server is runing";
    }

    @PostMapping("/message")
    public MessageResponseDTO sendMessage(@RequestBody @Valid MessageRequestDTO request){
            return service.createMessage(request);
    }

    @GetMapping("/messages")
    public List<MessageResponseDTO> showMessage() {
        return service.getMessageDesc();
    }

    @GetMapping("/messages/recent")
    public List<MessageResponseDTO> showLastNumberOfMessages(@RequestParam(name = "limit", required = false, defaultValue = "10") int limit) {
        return service.getRecentMessages(limit);
    }

    @GetMapping("/messages/user/{username}")
    public List<MessageResponseDTO> showMessagesFromUser(@PathVariable String username){
        return service.getUserMessages(username);
    }
    
    @DeleteMapping("/messages/{id}")
    public MessageResponseDTO delete(@PathVariable @Valid long id){
        return service.deleteMessage(id);
    }

    // @GetMapping("/sleep-db")
    // public String getMethodName() {
    //     repository.sleep();
    //     return "done";
    // }
    
    
}

package com.celauro.chat.controller;

import java.util.ArrayList;
import java.util.List;

import com.celauro.chat.DTO.MessageCountResponseDTO;
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

    // ========================
    // GET Endpoints
    // ========================
    @GetMapping("/hello")
    public String hello(){
        return "Server is runing";
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

    @GetMapping("/messages/search")
    public List<MessageResponseDTO> showListOfMessageWithFilter(
            @RequestParam(name="limit", defaultValue = "20") Integer limit,
            @RequestParam(name="username", required = false) String username,
            @RequestParam(name="textContains", required = false) String textContains
    ){
        return service.getFilteredList(limit, username, textContains);
    }

    @GetMapping("/messages/count")
    public MessageCountResponseDTO showUserCountOfMessages(@RequestParam(name = "username") String username){
        return service.getCountOfMessages(username);
    }

    // ========================
    // POST Endpoints
    // ========================
    @PostMapping("/message")
    public MessageResponseDTO sendMessage(@RequestBody @Valid MessageRequestDTO request){
            return service.createMessage(request);
    }

    // ========================
    // DELETE Endpoints
    // ========================
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

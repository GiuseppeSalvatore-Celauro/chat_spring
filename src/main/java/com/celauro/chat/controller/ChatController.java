package com.celauro.chat.controller;

import java.util.List;

import com.celauro.chat.DTO.*;
import com.celauro.chat.service.UserService;
import org.springframework.web.bind.annotation.*;

import com.celauro.chat.service.MessageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    // private final MessageRepository repository;
    private final MessageService messageService;
    private final UserService userService;

    // ========================
    // GET Endpoints
    // ========================
    @GetMapping("/hello")
    public String hello(){
        return "Server is running";
    }

    @GetMapping("/messages")
    public List<MessageResponseDTO> showMessage() {
        return messageService.getMessageDesc();
    }

    @GetMapping("/messages/recent")
    public List<MessageResponseDTO> showLastNumberOfMessages(@RequestParam(name = "limit", required = false, defaultValue = "10") int limit) {
        return messageService.getRecentMessages(limit);
    }

    @GetMapping("/messages/user/{username}")
    public List<MessageResponseDTO> showMessagesFromUser(@PathVariable String username){
        return messageService.getUserMessages(username);
    }

    @GetMapping("/messages/search")
    public List<MessageResponseDTO> showListOfMessageWithFilter(
            @RequestParam(name="limit", defaultValue = "20") Integer limit,
            @RequestParam(name="username", required = false) String username,
            @RequestParam(name="textContains", required = false) String textContains)
    {
        return messageService.getFilteredList(limit, username, textContains);
    }

    @GetMapping("/messages/count")
    public MessageCountResponseDTO showUserCountOfMessages(@RequestParam(name = "username") String username){
        return messageService.getCountOfMessages(username);
    }

    @GetMapping("/messages/conversation")
    public List<MessageResponseDTO> showConversationsBetweenUsers(@RequestParam(name = "user1") String user1, @RequestParam(name = "user2") String user2){
        return messageService.getConversationsBetweenUsers(user1, user2);
    }

    @GetMapping("/messages/conversations/{username}")
    public List<ConversationResponseDTO> showUserConversations(@PathVariable String username){
        return messageService.getUserConversations(username);
    }

    @GetMapping("/users/{username}/status")
    public UserResponseDTO showUserStatus(@PathVariable String username){
        return userService.getUserStatus(username);
    }

    @GetMapping("/messages/unread/{username}")
    public List<MessageCountResponseDTO> showUnreadMessages(@PathVariable String username){
        return messageService.getUnreadMessages(username);
    }

    @GetMapping("/users")
    public List<UserResponseDTO> showAllUsers(){
        return userService.getAllUsers();
    }
    // ========================
    // POST Endpoints
    // ========================
    @PostMapping("/message")
    public MessageResponseDTO sendMessage(@RequestBody @Valid MessageRequestDTO request){
            return messageService.createMessage(request);
    }

    @PostMapping("/user")
    public UserResponseDTO createUser(@RequestBody @Valid UserRequestDTO request){
        return userService.createUser(request);
    }

    @PostMapping("/messages/read")
    public void readMessages(@RequestBody @Valid ReadRequestDTO request){
        messageService.readMessages(request);
    }

    // ========================
    // PUT Endpoints
    // ========================
    @PutMapping("/users/login")
    public UserResponseDTO userLogin(@RequestBody @Valid UserRequestDTO request){
        return userService.userLogin(request);
    }

    @PutMapping("/users/logout")
    public UserResponseDTO userLogout(@RequestBody @Valid UserRequestDTO request){
        return userService.userLogout(request);
    }


    // ========================
    // DELETE Endpoints
    // ========================
    @DeleteMapping("/messages/{id}")
    public MessageResponseDTO delete(@PathVariable @Valid long id){
        return messageService.deleteMessage(id);
    }



    // @GetMapping("/sleep-db")
    // public String getMethodName() {
    //     repository.sleep();
    //     return "done";
    // }
    
    
}

package com.celauro.chat.service;

import org.springframework.stereotype.Service;

import com.celauro.chat.entity.User;
import com.celauro.chat.exception.NotFoundException;
import com.celauro.chat.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    public User getOrThrowExceptionUserByUsername(String username){
        User user = repository.findByUsername(username)
                                .orElseThrow(() -> new NotFoundException("Nessun user trovato"));
        return user;
    }

    public User getOrCreateUser(String username){
        User user = repository.findByUsername(username)
                                .orElseGet(() -> repository.save(new User(username)));
        return user;
    }
}

package com.celauro.chat.service;

import com.celauro.chat.DTO.UserRequestDTO;
import com.celauro.chat.DTO.UserResponseDTO;
import org.springframework.stereotype.Service;

import com.celauro.chat.entity.User;
import com.celauro.chat.exception.NotFoundException;
import com.celauro.chat.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    public UserResponseDTO createUser(UserRequestDTO request){
        User user = new User();
        user.setUsername(request.getUsername());

        repository.save(user);

        return toDto(user);
    }

    public User getOrThrowExceptionUserByUsername(String username){
        User user = repository.findByUsername(username)
                                .orElseThrow(() -> new NotFoundException("Nessun user trovato"));
        return user;
    }

    private UserResponseDTO toDto(User user){
        UserResponseDTO r = new UserResponseDTO();
        r.setUsername(user.getUsername());
        return r;
    }
}

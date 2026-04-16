package com.celauro.chat.service;

import com.celauro.chat.DTO.UserRequestDTO;
import com.celauro.chat.DTO.UserResponseDTO;
import com.celauro.chat.utils.Logger;
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

        Logger.info("Effettuata creazione utente");
        return toDto(user);
    }

    public User getOrThrowExceptionUserByUsername(String username, String errorMessage){
        User user = repository.findByUsername(username)
                                .orElseThrow(() -> new NotFoundException(errorMessage));

        Logger.info("Utente trovato");
        return user;
    }

    private UserResponseDTO toDto(User user){
        UserResponseDTO r = new UserResponseDTO();
        r.setUsername(user.getUsername());
        return r;
    }
}

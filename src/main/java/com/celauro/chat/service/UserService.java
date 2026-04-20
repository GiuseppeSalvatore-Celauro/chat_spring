package com.celauro.chat.service;

import com.celauro.chat.DTO.UserRequestDTO;
import com.celauro.chat.DTO.UserResponseDTO;
import com.celauro.chat.exception.UserOfflineException;
import com.celauro.chat.exception.UserOnlineException;
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
        user.setOnline(false);
        user.setLastSeen(System.currentTimeMillis());

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

    public UserResponseDTO userLogin(UserRequestDTO request){
        User user = this.getOrThrowExceptionUserByUsername(request.getUsername(), "Username non esistente, perfavore registrati");

        if(user.isOnline()) throw new UserOnlineException("Questo utente è gia loggato");

        user.setOnline(true);
        user.setLastSeen(0L);

        repository.save(user);

        Logger.info("Utente loggato con successo");
        return toDto(user);
    }

    public UserResponseDTO userLogout(UserRequestDTO request){
        User user = this.getOrThrowExceptionUserByUsername(request.getUsername(), "Username non esistente, perfavore registrati");

        if(!user.isOnline()) throw new UserOfflineException("Questo utente non è loggato");

        user.setOnline(false);
        user.setLastSeen(System.currentTimeMillis());

        repository.save(user);

        Logger.info("Utente ha fatto logout con successo");
        return toDto(user);
    }

    public UserResponseDTO getUserStatus(String username){
        User user = this.getOrThrowExceptionUserByUsername(username, "Utente non esiste");

        if(!user.isOnline()) throw new UserOfflineException("Questo utente non è loggato");

        Logger.info("Richiesto status dell'utente: " + username);
        return toDto(user);
    }

    //=============
    //Helper methods
    //=============
    private UserResponseDTO toDto(User user){
        UserResponseDTO r = new UserResponseDTO();
        r.setUsername(user.getUsername());
        r.setOnline(user.isOnline());
        r.setLastSeen(user.getLastSeen());
        return r;
    }
}

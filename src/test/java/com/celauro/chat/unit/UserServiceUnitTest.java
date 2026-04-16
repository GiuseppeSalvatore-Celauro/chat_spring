package com.celauro.chat.unit;

import com.celauro.chat.DTO.UserRequestDTO;
import com.celauro.chat.DTO.UserResponseDTO;
import com.celauro.chat.entity.User;
import com.celauro.chat.exception.NotFoundException;
import com.celauro.chat.repository.UserRepository;
import com.celauro.chat.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {
    @Mock
    private UserRepository repository;
    @InjectMocks
    private UserService service;

    // ========================
    // Find user
    // ========================
    @Test
    void shouldReturnUser_whenUserExist(){
        User user = new User();
        user.setUsername("test");

        when(repository.findByUsername("test")).thenReturn(Optional.of(user));

        User result = service.getOrThrowExceptionUserByUsername("test", any());

        assertEquals("test", result.getUsername());

        verify(repository).findByUsername("test");
    }

    // ========================
    // Find user - Edge case
    // ========================
    @Test
    void shouldThrowException_whenUserNotFound(){
        when(repository.findByUsername("test")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, ()-> service.getOrThrowExceptionUserByUsername("test", any()));

        verify(repository).findByUsername("test");
    }

    // ========================
    // Create user
    // ========================
    @Test
    void shouldCreateUser_whenUserDoesNotExist(){
        UserRequestDTO request = new UserRequestDTO();
        request.setUsername("test");

        User user = new User();
        user.setUsername(request.getUsername());

        when(repository.save(any(User.class))).thenReturn(user);

        UserResponseDTO result = service.createUser(request);

        assertEquals("test", result.getUsername());

        verify(repository).save(any(User.class));
    }
}

package com.celauro.chat.unit;

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
public class UserServiceTest {
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

        User result = service.getOrThrowExceptionUserByUsername("test");

        assertEquals("test", result.getUsername());

        verify(repository).findByUsername("test");
    }

    // ========================
    // Find user - Edge case
    // ========================
    @Test
    void shouldThrowException_whenUserNotFound(){
        when(repository.findByUsername("test")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, ()->{
           service.getOrThrowExceptionUserByUsername("test");
        });

        verify(repository).findByUsername("test");
    }

    // ========================
    // Create user
    // ========================
    @Test
    void shouldCreateUser_whenUserDoesNotExist(){
        User user = new User();
        user.setUsername("test");

        when(repository.findByUsername("test")).thenReturn(Optional.empty());
        when(repository.save(any(User.class))).thenReturn(user);

        User result = service.getOrCreateUser("test");

        assertEquals("test", result.getUsername());

        verify(repository).findByUsername("test");
        verify(repository).save(any(User.class));
    }

    // ========================
    // Create user - Edge case
    // ========================
    @Test
    void shouldReturnExistingUser_whenUserAlreadyExist(){
        User user = new User();
        user.setUsername("test");

        when(repository.findByUsername("test")).thenReturn(Optional.of(user));

        User result = service.getOrCreateUser("test");

        assertEquals("test", result.getUsername());

        verify(repository).findByUsername("test");
        verify(repository, never()).save(any(User.class));
    }
}

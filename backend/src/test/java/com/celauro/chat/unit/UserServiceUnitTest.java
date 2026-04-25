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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

    // ========================
    // Login user
    // ========================
    @Test
    void shouldChangeUserStatusInOnline(){
        UserRequestDTO request = new UserRequestDTO();
        request.setUsername("test");

        User user = new User();
        user.setOnline(false);
        user.setLastSeen(0L);
        user.setUsername("test");

        when(repository.findByUsername(eq("test"))).thenReturn(Optional.of(user));
        when(repository.save(any())).thenReturn(user);

        UserResponseDTO response = service.userLogin(request);

        assertEquals("test", response.getUsername());
        assertTrue(response.isOnline());
        assertEquals(0L, response.getLastSeen());

        verify(repository).save(any());
    }

    // ========================
    // Login user - edge case
    // ========================
    @Test
    void shouldThrowException_whenUserDoesNotExist(){
        UserRequestDTO request = new UserRequestDTO();
        request.setUsername("test");

        when(repository.findByUsername(eq("test"))).thenThrow(new NotFoundException("not found"));

        assertThrows(NotFoundException.class, ()-> service.userLogin(request));

        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowException_whenUserAlreadyOnline(){
        UserRequestDTO request = new UserRequestDTO();
        request.setUsername("test");

        User user = new User();
        user.setOnline(true);
        user.setLastSeen(0L);
        user.setUsername("test");

        when(repository.findByUsername(eq("test"))).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> service.userLogin(request));

        verify(repository, never()).save(any());
    }

    // ========================
    // Logout user
    // ========================
    @Test
    void shouldChangeUserStatusInOffline(){
        UserRequestDTO request = new UserRequestDTO();
        request.setUsername("test");

        User user = new User();
        user.setOnline(true);
        user.setLastSeen(0L);
        user.setUsername("test");

        when(repository.findByUsername(eq("test"))).thenReturn(Optional.of(user));
        when(repository.save(any())).thenReturn(user);

        UserResponseDTO response = service.userLogout(request);

        assertEquals("test", response.getUsername());
        assertTrue(!response.isOnline());
        assertTrue(response.getLastSeen() > 0);

        verify(repository).save(any());
    }

    // ========================
    // Logout user - edge case
    // ========================
    @Test
    void shouldThrowException_whenUserAlreadyOffline(){
        UserRequestDTO request = new UserRequestDTO();
        request.setUsername("test");

        User user = new User();
        user.setOnline(false);
        user.setLastSeen(0L);
        user.setUsername("test");

        when(repository.findByUsername(eq("test"))).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> service.userLogout(request));

        verify(repository, never()).save(any());
    }

    // ========================
    // User status
    // ========================
    @Test
    void shouldReturnTheUserStatus(){
        User u = new User();
        u.setUsername("test");
        u.setOnline(true);
        u.setLastSeen(1L);

        when(repository.findByUsername(eq("test"))).thenReturn(Optional.of(u));

        UserResponseDTO res = service.getUserStatus("test");

        assertNotNull(res);
        assertEquals("test", res.getUsername());
        assertTrue(res.isOnline());
        assertTrue(res.getLastSeen() > 0L);

        verify(repository).findByUsername(any());
    }

    // ========================
    // Users list
    // ========================
    @Test
    void shouldReturnListOfUsers(){
        User u = new User("salvatore", false, 1L);
        User u1 = new User("pippo", false, 1L);
        User u2 = new User("marco", false, 1L);

        when(repository.findAll()).thenReturn(List.of(u,u1,u2));

        List<UserResponseDTO> response = service.getAllUsers();

        assertEquals(3, response.size());

        verify(repository).findAll();
    }

    // ========================
    // Users list - edge case
    // ========================
    @Test
    void shouldReturnAnEmptyList_whenUserIsEmpty(){
        when(repository.findAll()).thenReturn(List.of());

        List<UserResponseDTO> response = service.getAllUsers();

        assertEquals(0, response.size());

        verify(repository).findAll();
    }

    // ========================
    // User status - edge case
    // ========================
    @Test
    void shouldThrowException_whenUserDoesNotExistInStatus(){
        assertThrows(NotFoundException.class, () -> service.getUserStatus("test"));
    }

}

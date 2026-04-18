package com.celauro.chat.integration;

import com.celauro.chat.DTO.UserRequestDTO;
import com.celauro.chat.DTO.UserResponseDTO;
import com.celauro.chat.entity.User;
import com.celauro.chat.repository.UserRepository;
import com.celauro.chat.service.UserService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class UserServiceIntegrationTest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    // ========================
    // Create user
    // ========================
    @Test
    void shouldCreateUser(){
        userService.createUser(toDto("creationTest"));

        List<User> users = userRepository.findAll();

        assertEquals(1, users.size());
        assertEquals("creationTest", users.getFirst().getUsername());
        assertFalse(users.getFirst().isOnline());
        assertTrue(users.getFirst().getLastSeen() > 0);
    }

    // ========================
    // Create user - edge case
    // ========================
    @Test
    void shouldThrowException_whenUserAlreadyExist(){
        userService.createUser(toDto("creationTest"));
        assertThrows(RuntimeException.class, () -> userService.createUser(toDto("creationTest")));
    }

    // ========================
    // Login user
    // ========================
    @Test
    void shouldReturnUserLogged(){
        userService.createUser(toDto("loginTest"));
        userService.userLogin(toDto("loginTest"));

        List<User> users = userRepository.findAll();
        assertEquals(1, users.size());
        assertEquals("loginTest", users.getFirst().getUsername());
        assertTrue(users.getFirst().isOnline());
        assertEquals(0, users.getFirst().getLastSeen());
    }

    // ========================
    // Login user - edge case
    // ========================
    @Test
    void shouldThrowException_whenUserAlreadyOnline(){
        userService.createUser(toDto("loginTest"));
        userService.userLogin(toDto("loginTest"));

        assertThrows(RuntimeException.class, () -> userService.userLogin(toDto("loginTest")));
    }

    // ========================
    // Logout user
    // ========================
    @Test
    void shouldReturnUserLoggedOut(){
        userService.createUser(toDto("logoutTest"));
        userService.userLogin(toDto("logoutTest"));
        userService.userLogout(toDto("logoutTest"));

        List<User> users = userRepository.findAll();

        assertEquals(1, users.size());
        assertEquals("logoutTest", users.getFirst().getUsername());
        assertFalse(users.getFirst().isOnline());
        assertTrue(users.getFirst().getLastSeen() > 0);
    }

    // ========================
    // Status user
    // ========================
    @Test
    void shouldReturnUserStatus(){
        userService.createUser(toDto("statusTest"));

        UserResponseDTO status = userService.getUserStatus("statusTest");

        assertEquals("statusTest", status.getUsername());
        assertFalse(status.isOnline());
        assertTrue(status.getLastSeen() > 0);
    }

    // ========================
    // Helper methods
    // ========================
    private UserRequestDTO toDto(String username){
        UserRequestDTO r = new UserRequestDTO();
        r.setUsername(username);
        return r;
    }
}

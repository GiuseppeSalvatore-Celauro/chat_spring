package com.celauro.chat.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.celauro.chat.repository.UserRepository;
import com.celauro.chat.service.NotificationService;

import jakarta.transaction.Transactional;


@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class MessageIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private NotificationService notificationService;
    

    @Test
    @Transactional
    void shouldCreateAndReciveMessage() throws Exception{

        String json = """
                {
                    "username": "salvatore",
                    "text": "ciao"
                }
                """;

        //POST
        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isOk());

        //GET
        mockMvc.perform(get("/api/chat/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].username").exists())
                .andExpect(jsonPath("$[0].text").exists())
                .andExpect(jsonPath("$[0].timestamp").exists())
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].username").value("salvatore"))
                .andExpect(jsonPath("$[0].text").value("ciao"))
                .andExpect(jsonPath("$[0].timestamp").isNumber());
        
    }

    @Test
    @Transactional
    void shouldReturnBadRequest_whenJsonEmpty() throws Exception{
         String json = "{}";

        //POST
        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void shouldReturnBadRequest_whenUsernameAndTestInvalid() throws Exception{
         String json = """
                 {
                    "username": "",
                    "text": ""
                 }
                 """;;

        //POST
        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[*].field", hasItem("username")))
                .andExpect(jsonPath("$.messages[*].field", hasItem("text")))
                .andExpect(jsonPath("$.messages[*].message", hasItem("username obbligatorio")))
                .andExpect(jsonPath("$.messages[*].message", hasItem("testo obbligatorio")));
    }

    @Test
    @Transactional
    void shouldReturnBadRequest_whenUsernameTooShort() throws Exception{
         String json = """
                 {
                    "username": "a",
                    "text": "ciao"
                 }
                 """;;

        //POST
        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[*].field", hasItem("username")))
                .andExpect(jsonPath("$.messages[*].message", hasItem("username deve essere lungo dai 3 ai 20 caratteri")));
    }

    @Test
    @Transactional
    void shouldReturnBadRequest_whenTextTooLong() throws Exception{
        String json = """
                 {
                    "username": "ciao",
                    "text": "ciaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociao"
                 }
                 """;;

        //POST
        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[*].field", hasItem("text")))
                .andExpect(jsonPath("$.messages[*].message", hasItem("il testo del messaggio è troppo lungo")));
    }

    @Test
    @Transactional
    void shouldReturnBadRequest_whenUsernameAndTextTooLong() throws Exception{
        String json = """
                 {
                    "username": "ciaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociao",
                    "text": "ciaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociaociao"
                 }
                 """;;

        //POST
        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[*].field", hasItem("username")))
                .andExpect(jsonPath("$.messages[*].field", hasItem("text")))
                .andExpect(jsonPath("$.messages[*].message", hasItem("username deve essere lungo dai 3 ai 20 caratteri")))
                .andExpect(jsonPath("$.messages[*].message", hasItem("il testo del messaggio è troppo lungo")));
    }

    @Test
    @Transactional
    void shouldReturnEmptyList_whenNoMessages() throws Exception{
        mockMvc.perform(get("/api/chat/messages"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldRollBackAll_whenErrorOccurs() throws Exception{
        String json = """
                 {
                    "username": "salvatore",
                    "text": "ciao"
                 }
                 """;;

        doThrow(new RuntimeException("Errore simulato"))
                .when(notificationService)
                .createNotification(any(), any());

        mockMvc.perform(post("/api/chat/message")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isInternalServerError());

        mockMvc.perform(get("/api/chat/messages"))
                .andExpect(jsonPath("$.length()").value(0));

        assertTrue(userRepository.findAll().isEmpty());
    }

    @Test
    void shouldThrowException_whenUserNotFound() throws Exception{
        mockMvc.perform(get("/api/chat/messages/user/mario"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", hasToString("Nessun user trovato")));
    }

    @Test
    @Transactional
    void shouldReturnADescList() throws Exception{
        String json = """
                 {
                    "username": "salvatore",
                    "text": "primo"
                 }
                 """;;
        
        String json2 = """
                {
                    "username": "salvatore",
                    "text": "secondo"
                 }
                """;

        String json3 = """
                {
                    "username": "salvatore",
                    "text": "terzo"
                 }
                """;

        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json2))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json3))
                .andExpect(status().isOk());
                        
        mockMvc.perform(get("/api/chat/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text").value("terzo"));
        
    }

    @Test
    @Transactional
    void shouldNotCreateDuplicateUsers() throws Exception{
        String json = """
                 {
                    "username": "salvatore",
                    "text": "ciao"
                 }
                 """;

        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        assertEquals(1, userRepository.findAll().size());
    }
    
    @Test
    @Transactional
    void shouldReturnAllMessagesIfLimitIsGreater() throws Exception{
        String json = """
                 {
                    "username": "salvatore",
                    "text": "ciao"
                 }
                 """;

        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/chat/messages/recent?limit=100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}

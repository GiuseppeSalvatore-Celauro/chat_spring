package com.celauro.chat.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.transaction.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnHello() throws Exception{
        mockMvc.perform(get("/api/chat/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Server is runing"));
    }

    @Test
    void shouldCreateMessage() throws Exception{
        String json = """
                {
                    "username": "marco",
                    "text": "Ciao!"
                }
                """;
        
        mockMvc.perform(post("/api/chat/message")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnBadRequest() throws Exception{
        String json = "{}";

        mockMvc.perform(post("/api/chat/message")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());   
    }
}

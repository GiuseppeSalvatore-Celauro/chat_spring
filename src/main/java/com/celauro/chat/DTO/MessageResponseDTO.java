package com.celauro.chat.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageResponseDTO {
    private long id;
    private String username;
    private String text;   
    private long timestamp;
}

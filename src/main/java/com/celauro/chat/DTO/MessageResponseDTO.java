package com.celauro.chat.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageResponseDTO {
    private long id;
    private String sender;
    private String receiver;
    private String text;   
    private long timestamp;
}

package com.celauro.chat.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConversationResponseDTO {
    private String withUser;
    private String lastMessage;
    private boolean online;
    private Long timestamp;
}

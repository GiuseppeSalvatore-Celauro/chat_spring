package com.celauro.chat.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MessageCountResponseDTO {
    private String username;
    private int count;
}

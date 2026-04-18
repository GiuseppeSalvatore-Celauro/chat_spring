package com.celauro.chat.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDTO {
    public String Username;
    public boolean online;
    public long lastSeen;
}

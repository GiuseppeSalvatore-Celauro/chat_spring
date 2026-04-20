package com.celauro.chat.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReadRequestDTO {
    @NotBlank
    private String username;
    @NotBlank
    private String withUser;
}

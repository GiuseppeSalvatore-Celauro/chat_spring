package com.celauro.chat.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReadRequestDTO {
    @NotBlank
    private String username;
    @NotBlank
    private String withUser;
}

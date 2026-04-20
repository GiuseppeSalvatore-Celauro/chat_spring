package com.celauro.chat.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageRequestDTO {
    @NotBlank(message = "username obbligatorio")
    @Size(min = 3, max = 20, message = "username deve essere lungo dai 3 ai 20 caratteri")
    private String sender;

    @NotBlank(message = "username obbligatorio")
    @Size(min = 3, max = 20, message = "username deve essere lungo dai 3 ai 20 caratteri")
    private String receiver;

    @NotBlank(message = "testo obbligatorio")
    @Size(max = 200, message = "il testo del messaggio è troppo lungo")
    private String text;

}

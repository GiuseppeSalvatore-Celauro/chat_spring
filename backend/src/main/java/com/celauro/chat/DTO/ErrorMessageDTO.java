package com.celauro.chat.DTO;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorMessageDTO {
    private List<ValidationErrorDTO> messages;
    private String message;
    private long timestamp;
    private int status;
    private String error;
    private String path;
    private String method;    
}

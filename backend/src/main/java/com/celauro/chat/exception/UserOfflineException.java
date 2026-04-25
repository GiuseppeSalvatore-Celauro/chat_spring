package com.celauro.chat.exception;

public class UserOfflineException extends RuntimeException{
    public UserOfflineException(String message) {
        super(message);
    }
}

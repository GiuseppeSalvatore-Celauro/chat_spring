package com.celauro.chat.handler;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import com.celauro.chat.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.celauro.chat.DTO.ErrorMessageDTO;
import com.celauro.chat.DTO.ValidationErrorDTO;
import com.celauro.chat.utils.Logger;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessageDTO MethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request){

        List<ValidationErrorDTO> errors = e.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(err -> new ValidationErrorDTO(err.getField(), err.getDefaultMessage()))
                                .toList();

        ErrorMessageDTO error =  errorFormatter(e, request);
        error.setMessages(errors);
        error.setMessage(null);
        
        Logger.error("Campo mancante nella richiesta", e);
        return error;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessageDTO DataIntegrityViolationException(DataIntegrityViolationException e, HttpServletRequest request){
        Logger.error("Campo univoco nel db", e);
        return errorFormatter(e, request);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessageDTO NotFoundException(NotFoundException e, HttpServletRequest request){
        Logger.error("Messaggio non trovato", e);
        return errorFormatter(e, request);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessageDTO handleException(Exception e, HttpServletRequest request){
        Logger.error("Errore interno al server", e);
        return errorFormatter(e, request);
    }

    private ErrorMessageDTO errorFormatter(Exception e, HttpServletRequest request){
        ErrorMessageDTO error = new ErrorMessageDTO();
        error.setStatus(HttpStatus.NOT_FOUND.value());
        error.setTimestamp(System.currentTimeMillis());
        error.setError(HttpStatus.NOT_FOUND.getReasonPhrase());
        error.setMessage(e.getMessage());
        error.setPath(request.getRequestURI());
        error.setMethod(request.getMethod());

        return error;
    }
}

package org.example.exceptions;

import com.fasterxml.jackson.core.JsonParseException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

//Custom exceptions
@ExceptionHandler(JsonParseException.class)
public void jsonParsingException(JsonParseException e){
    System.out.println(e);
}
}

package org.example.exceptions;

public class LLMResponseParseException extends RuntimeException{
    public LLMResponseParseException(String message){
        super(message);
    }
}

package org.example.exceptions;

public class LLMPromptException extends RuntimeException{
    public LLMPromptException(String message){
        super(message);
    }
}

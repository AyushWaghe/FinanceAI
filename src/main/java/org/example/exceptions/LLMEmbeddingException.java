package org.example.exceptions;

public class LLMEmbeddingException extends RuntimeException{
    public LLMEmbeddingException(String objectKey){
        super(objectKey);
    }
}

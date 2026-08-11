package org.example.exceptions;

public class LuceneServiceException extends RuntimeException{
    public LuceneServiceException(String message){
        super(message);
    }
}

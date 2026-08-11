package org.example.exceptions;

public class QdrantServiceException extends RuntimeException{
    public QdrantServiceException(String message){
        super(message);
    }
}

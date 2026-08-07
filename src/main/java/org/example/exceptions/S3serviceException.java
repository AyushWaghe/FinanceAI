package org.example.exceptions;

public class S3serviceException extends RuntimeException{
    public S3serviceException(String message){
        super(message);
    }
}

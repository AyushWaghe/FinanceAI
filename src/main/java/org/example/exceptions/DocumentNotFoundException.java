package org.example.exceptions;

public class DocumentNotFoundException extends RuntimeException{
    public DocumentNotFoundException(String objectKey){
        super(objectKey);
    }
}

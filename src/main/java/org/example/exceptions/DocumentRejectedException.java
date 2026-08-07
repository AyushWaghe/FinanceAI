package org.example.exceptions;

public class DocumentRejectedException extends RuntimeException{
    public DocumentRejectedException(String message) {
        super(message);
    }
}

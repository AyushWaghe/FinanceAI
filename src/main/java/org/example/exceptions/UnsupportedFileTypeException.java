package org.example.exceptions;

public class UnsupportedFileTypeException extends RuntimeException{
    public UnsupportedFileTypeException(String message) {
        super(message);
    }
}

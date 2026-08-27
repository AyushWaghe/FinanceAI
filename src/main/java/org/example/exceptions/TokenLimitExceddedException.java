package org.example.exceptions;

public class TokenLimitExceddedException extends RuntimeException{
    public TokenLimitExceddedException(String message){
        super(message);
    }
}

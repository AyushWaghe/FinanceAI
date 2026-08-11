package org.example.exceptions;

import com.fasterxml.jackson.core.JsonParseException;
import org.example.dto.ErrorResponse;
import org.example.service.LuceneService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

//Custom exceptions
@ExceptionHandler(JsonParseException.class)
public void jsonParsingException(JsonParseException e){
    System.out.println(e);
}



@ExceptionHandler(StorageServiceException.class)
    public void storageServiceException(StorageServiceException e){
    System.out.println(e);
}

    @ExceptionHandler(UnsupportedOperationException.class)
    public void invalidDoc(UnsupportedOperationException e){
        System.out.println(e);
    }

    @ExceptionHandler(UnsupportedFileTypeException.class)
    public void noExtentionException(UnsupportedFileTypeException e){
        System.out.println(e);
    }

    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<ErrorResponse> docNotFound(DocumentRejectedException e){
        ErrorResponse err=new ErrorResponse(404,"Document with object id "+e.getMessage()+" not found",e.toString());
        return new ResponseEntity<>(err, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> RuntimeException(RuntimeException e){
        ErrorResponse err=new ErrorResponse(500,e.getMessage(),e.toString());
        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(LLMResponseParseException.class)
    public ResponseEntity<ErrorResponse> llmResponseParserException(LLMResponseParseException e){
        ErrorResponse err=new ErrorResponse(500,e.getMessage(),e.toString());
        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(LLMPromptException.class)
    public ResponseEntity<ErrorResponse> llmPromptException(LLMPromptException e){
        ErrorResponse err=new ErrorResponse(500,e.getMessage(),e.toString());
        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(LLMEmbeddingException.class)
    public ResponseEntity<ErrorResponse> llmEmbeddingException(LLMEmbeddingException e){
        ErrorResponse err=new ErrorResponse(500,e.getMessage(),e.toString());
        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(S3serviceException.class)
    public ResponseEntity<ErrorResponse> s3ServiceException(S3serviceException e){
        ErrorResponse err=new ErrorResponse(500,e.getMessage(),e.toString());
        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(LuceneServiceException.class)
    public ResponseEntity<ErrorResponse> luceneServiceException(LuceneServiceException e){
        ErrorResponse err=new ErrorResponse(500,e.getMessage(),e.toString());
        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(QdrantServiceException.class)
    public ResponseEntity<ErrorResponse> qdrantServiceException(QdrantServiceException e){
        ErrorResponse err=new ErrorResponse(500,e.getMessage(),e.toString());
        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }




}

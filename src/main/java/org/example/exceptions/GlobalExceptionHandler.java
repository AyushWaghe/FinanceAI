package org.example.exceptions;

import com.fasterxml.jackson.core.JsonParseException;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.ErrorResponse;
import org.example.service.LuceneService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    //Custom exceptions
    @ExceptionHandler(JsonParseException.class)
    public void jsonParsingException(JsonParseException e){
        log.error("JSON parsing exception", e);
    }



    @ExceptionHandler(StorageServiceException.class)
    public void storageServiceException(StorageServiceException e){
        log.error("Storage service exception", e);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public void invalidDoc(UnsupportedOperationException e){
        log.error("Unsupported operation exception", e);
    }

    @ExceptionHandler(UnsupportedFileTypeException.class)
    public void noExtentionException(UnsupportedFileTypeException e){
        log.error("Unsupported file type exception", e);
    }

    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<ErrorResponse> docNotFound(DocumentRejectedException e){
        log.error("Document not found exception", e);
        ErrorResponse err=new ErrorResponse(404,"Document with object id "+e.getMessage()+" not found",e.toString());
        return new ResponseEntity<>(err, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> RuntimeException(RuntimeException e){
        log.error("Runtime exception", e);
        ErrorResponse err=new ErrorResponse(500,e.getMessage(),e.toString());
        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(LLMResponseParseException.class)
    public ResponseEntity<ErrorResponse> llmResponseParserException(LLMResponseParseException e){
        log.error("LLM response parse exception", e);
        ErrorResponse err=new ErrorResponse(500,e.getMessage(),e.toString());
        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(LLMPromptException.class)
    public ResponseEntity<ErrorResponse> llmPromptException(LLMPromptException e){
        log.error("LLM prompt exception", e);
        ErrorResponse err=new ErrorResponse(500,e.getMessage(),e.toString());
        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(LLMEmbeddingException.class)
    public ResponseEntity<ErrorResponse> llmEmbeddingException(LLMEmbeddingException e){
        log.error("LLM embedding exception", e);
        ErrorResponse err=new ErrorResponse(500,e.getMessage(),e.toString());
        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(S3serviceException.class)
    public ResponseEntity<ErrorResponse> s3ServiceException(S3serviceException e){
        log.error("S3 service exception", e);
        ErrorResponse err=new ErrorResponse(500,e.getMessage(),e.toString());
        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(LuceneServiceException.class)
    public ResponseEntity<ErrorResponse> luceneServiceException(LuceneServiceException e){
        log.error("Lucene service exception", e);
        ErrorResponse err=new ErrorResponse(500,e.getMessage(),e.toString());
        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(QdrantServiceException.class)
    public ResponseEntity<ErrorResponse> qdrantServiceException(QdrantServiceException e){
        log.error("Qdrant service exception", e);
        ErrorResponse err=new ErrorResponse(500,e.getMessage(),e.toString());
        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(TokenLimitExceddedException.class)
    public ResponseEntity<ErrorResponse> tokenLimitExceddedException(QdrantServiceException e){
        log.error("Token limit exceeded exception", e);
        ErrorResponse err=new ErrorResponse(500,e.getMessage(),e.toString());
        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ToolClientException.class)
    public ResponseEntity<ErrorResponse> toolClientException(QdrantServiceException e){
        log.error("Tool client exception", e);
        ErrorResponse err=new ErrorResponse(500,e.getMessage(),e.toString());
        System.out.println(err);
        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }




}
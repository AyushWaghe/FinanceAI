package org.example.service;

import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.example.config.StorageProperties;
import org.example.dao.UserDocRepository;
import org.example.enums.DocumentState;
import org.example.event.UserDocumentUploadedEvent;
import org.example.exceptions.StorageServiceException;
import org.example.model.UserDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {
    private final S3Client s3Client;
    private final StorageProperties storageProperties;
    private final UserDocRepository userDocRepository;
    private final KafkaTemplate<String, UserDocumentUploadedEvent>kafkaTemplate;

    public String upload(MultipartFile file, Integer userId) {
        String objectKey = null;
        try {

            objectKey = "doc/" + userId.toString() + "/" + UUID.randomUUID() + "/" + file.getOriginalFilename();

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(storageProperties.getBucketName())
                    .key(objectKey)
                    .contentType(file.getContentType()) //This is required so that browsers know how to handle that file while downloading that file
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(  //Request body is the file content
                            file.getInputStream(),
                            file.getSize()
                    )
            );

            UserDocument userDocument = new UserDocument();
            userDocument.setUserId(userId);
            userDocument.setObjectKey(objectKey);
            userDocument.setDocumentState(DocumentState.PROCESSING);
            userDocRepository.save(userDocument);

//            documentIngestionService.ingestDocument(objectKey,userId);
            UserDocumentUploadedEvent userDocumentUploadedEvent=new UserDocumentUploadedEvent(userId,objectKey);
            kafkaTemplate.send(
                    "user-document-uploaded-topic",
                    userId.toString(),
                    userDocumentUploadedEvent
            );

            return objectKey;
        } catch (DataAccessException e) {
            // Upload succeeded but DB failed
           delete(objectKey);
            throw e;
        } catch (IOException e) {
            System.out.println(e);
            throw new StorageServiceException("Failed to read upload file ", e);
        } catch (S3Exception | SdkClientException e) {
            System.out.println(e);
            throw new StorageServiceException("Failed to upload file to object storage", e);
        }
    }

    public InputStream download(String objectKey) {
        GetObjectRequest request=GetObjectRequest.builder()
                .bucket(storageProperties.getBucketName())
                .key(objectKey)
                .build();

        return s3Client.getObject(request);
    }

    public void delete(String objectKey) {
        DeleteObjectRequest request=
                DeleteObjectRequest.builder()
                        .bucket(storageProperties.getBucketName())
                        .key(objectKey)
                        .build();

        s3Client.deleteObject(request);
    }
}

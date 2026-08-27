package org.example.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.example.enums.DocumentState;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


@Table(name = "user_docs")
@AllArgsConstructor
@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
public class UserDocument {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "doc_seq_gen")
    @SequenceGenerator(name="doc_seq_gen",sequenceName = "doc_seq",allocationSize = 50)
    private Integer Id;

    @NotNull
    private Integer userId;

    private String docType;

    private String docSummary;

    @Enumerated(EnumType.STRING)
    private DocumentState documentState;

    private String rejectedReason;

    @NotNull
    private String objectKey;

    @CreationTimestamp
    @Column(name="created_at")
    private LocalDateTime createdAt;

}

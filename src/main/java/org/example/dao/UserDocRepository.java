package org.example.dao;

import org.apache.catalina.User;
import org.example.model.UserDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface UserDocRepository extends JpaRepository<UserDocument,Integer> {
    Optional<UserDocument> findByObjectKey(
            String objectKey
    );

    @Query("""
    SELECT DISTINCT d.docType
    FROM UserDocument d
    WHERE d.userId = :userId
""")
    Set<String> findDistinctCategoriesByUserId(Integer userId);
}

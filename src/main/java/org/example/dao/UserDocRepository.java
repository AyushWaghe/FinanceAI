package org.example.dao;

import jakarta.transaction.Transactional;
import org.apache.catalina.User;
import org.example.model.UserDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    List<UserDocument> findByUserId(Integer userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserDocument d WHERE d.objectKey = :objectKey")
    void deleteByObjectKey(@Param("objectKey") String objectKey);
}

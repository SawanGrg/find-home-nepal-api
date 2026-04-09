package com.beta.FindHome.repository;

import com.beta.FindHome.model.Blogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BlogRepository extends JpaRepository<Blogs, UUID>, JpaSpecificationExecutor<Blogs> {

    @Modifying
    @Query("UPDATE Blogs b SET b.published = :published WHERE b.id = :id")
    void updatePublishedStatus(@Param("id") UUID id, @Param("published") boolean published);
}

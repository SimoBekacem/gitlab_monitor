package com.example.gitlab_monitor.repository;

import com.example.gitlab_monitor.model.CommitModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommitsRepository extends JpaRepository<CommitModel, String> {

    // JPQL query to select all commit IDs
    @Query("SELECT c.commitId FROM CommitModel c")
    List<String> findAllCommitIds();

    // Spring Data JPA method to check existence by commitId, no need for @Query
    boolean existsByCommitId(String commitId);
}

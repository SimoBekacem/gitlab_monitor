package com.example.gitlab_monitor.repository;

import org.springframework.stereotype.Repository;

import com.example.gitlab_monitor.model.CommitModel;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

@Repository
public interface CommitsRepository extends MongoRepository<CommitModel, String> {
    @Query(value = "{}", fields = "{ '_id' : 1 }")
    List<String> findAllCommitIds();    

    @Query(value = "{ '_id' : ?0 }")
    boolean existsByCommitId(String commitId);
}

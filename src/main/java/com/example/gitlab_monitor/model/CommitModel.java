package com.example.gitlab_monitor.model;

import java.util.Date;
import java.text.SimpleDateFormat; 

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "commits")
public class CommitModel {
    @Id
    @Field("commitId")
    private String commitId;
    private String projectName;
    private String committerName;
    private String commitDate;
    private String message;
    private String branchName;

    // Standard date format for consistent output
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

    public CommitModel(String projectName, String committerName, Date commitDate, String commitId, String message, String branchName) {
        this.projectName = projectName;
        this.committerName = committerName;
        this.commitDate = commitDate != null ? dateFormat.format(commitDate) : "N/A";
        this.commitId = commitId;
        this.message = message;
        this.branchName = branchName;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getCommitterName() {
        return committerName;
    }

    public String getCommitDate() {
        return commitDate;
    }

    public String getCommitId() {
        return commitId;
    }

    public String getMessage() {
        return message;
    }
}

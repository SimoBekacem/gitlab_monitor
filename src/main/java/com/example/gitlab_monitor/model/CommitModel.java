package com.example.gitlab_monitor.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "commits")
public class CommitModel {
    @Id
    @Column(name = "commit_id", nullable = false, unique = true)
    private String commitId;
    @Column(name = "project_name", nullable = false)
    private String projectName;
    @Column(name = "committer_name", nullable = false)
    private String committerName;
    @Column(name = "commit_date", nullable = false)
    private String commitDate;
    @Column(name = "message")
    private String message;
    @Column(name = "branch_name")
    private String branchName;

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    public void setCommitterName(String committerName) {
        this.committerName = committerName;
    }
    public void setCommitDate(String commitDate) {
        this.commitDate = commitDate;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    // Standard date format for consistent output
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

    public CommitModel() {}
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

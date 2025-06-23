package com.example.gitlab_monitor.model;

import java.util.Date;
import java.text.SimpleDateFormat; 

public class CommitInfo {
    private String projectName;
    private String committerName;
    private String commitDate;
    private String commitId;
    private String message;

    // Standard date format for consistent output
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

    /**
     * Constructor for CommitInfo.
     *
     * @param projectName   The name of the project the commit belongs to.
     * @param committerName The name of the committer.
     * @param commitDate    The date of the commit.
     * @param commitId      The full ID (SHA) of the commit.
     * @param message       The commit message.
     */
    public CommitInfo(String projectName, String committerName, Date commitDate, String commitId, String message) {
        this.projectName = projectName;
        this.committerName = committerName;
        // Format the Date object to a String for consistent JSON output
        this.commitDate = commitDate != null ? dateFormat.format(commitDate) : "N/A";
        this.commitId = commitId;
        this.message = message;
    }

    public String getProjectName() {
        return projectName;
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

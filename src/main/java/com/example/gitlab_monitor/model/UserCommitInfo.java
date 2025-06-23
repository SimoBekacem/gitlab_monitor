package com.example.gitlab_monitor.model;

public class UserCommitInfo {
    private String userName;
    private Integer numberOfCommits;

    public UserCommitInfo(String userName, Integer numberOfCommits) {
        this.userName = userName;
        this.numberOfCommits = numberOfCommits;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getNumberOfCommits() {
        return numberOfCommits;
    }

    public void setNumberOfCommits(Integer numberOfCommits) {
        this.numberOfCommits = numberOfCommits;
    }
    
}

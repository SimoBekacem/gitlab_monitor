package com.example.gitlab_monitor.controller;


import org.springframework.web.bind.annotation.RestController;

import com.example.gitlab_monitor.model.CommitInfo;
import com.example.gitlab_monitor.model.UserCommitInfo;
import com.example.gitlab_monitor.service.GitLabService;

import java.util.List;

import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;

import org.springframework.web.bind.annotation.GetMapping;




@RestController
public class MainController {
   private final GitLabService gitLabService;

    public MainController(GitLabService gitLabService) {
        this.gitLabService = gitLabService;
    }

    @GetMapping("/home")
    // @Scheduled(fixedRate = 1000)
    public List<Project> home() throws GitLabApiException {
        System.out.println("MainController: /home endpoint called. Requesting projects from GitLabService.");
        return gitLabService.getAllProjects();
    }

    @GetMapping("/commits")
    public List<CommitInfo> getProjectCommits() throws GitLabApiException {
        System.out.println("MainController: /commits endpoint called. Requesting commits from GitLabService.");
        return gitLabService.getAllCommitsForAllProjects();
    }
    @GetMapping("/usersCommits")
    public List<UserCommitInfo> getMethodName() {
        System.out.println("MainController: /usersCommits endpoint called. Requesting commits from GitLabService.");
        try {
            List<CommitInfo> commits = gitLabService.getAllCommitsForAllProjects();
            return gitLabService.getUsersCommitsInfo(commits);
        } catch (GitLabApiException e) {
            throw new RuntimeException("Failed to fetch commits from GitLab", e);
        }
    }
    
    
}

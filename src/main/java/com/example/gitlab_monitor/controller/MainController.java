package com.example.gitlab_monitor.controller;


import org.springframework.web.bind.annotation.RestController;

import com.example.gitlab_monitor.model.CommitModel;
import com.example.gitlab_monitor.service.GitLabService;

import java.util.List;

import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;






@RestController
public class MainController {
   private final GitLabService gitLabService;

    public MainController(GitLabService gitLabService) {
        this.gitLabService = gitLabService;
    }

    @GetMapping("/sync-commits")
    public String syncCommits() {
        try {
            gitLabService.addCommitFromLastCommit();
        } catch (GitLabApiException e) {
            System.out.println("Failed to fetch commits for project due to an unexpected error");
            e.printStackTrace();
        }
        return new String();
    }
    
    @GetMapping("/sync-commits-db")
    public void getProjectCommits() throws GitLabApiException {
        System.out.println("MainController: /commits endpoint called. Requesting commits from GitLabService.");
        gitLabService.getAllCommitsForAllProjects();
    }
    @PostMapping("/commit")
    public String getMethodName(@RequestBody CommitModel commit) {
        System.out.println("MainController: /commit endpoint called. Requesting commits from GitLabService with project ID: " + commit.getBranchName());
        gitLabService.storeCommit(commit);
        return new String();
    }
    
}

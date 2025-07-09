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





@RestController
public class MainController {
   private final GitLabService gitLabService;

    public MainController(GitLabService gitLabService) {
        this.gitLabService = gitLabService;
    }

    @GetMapping("/home")
    public List<Project> home() throws GitLabApiException {
        System.out.println("MainController: /home endpoint called. Requesting projects from GitLabService.");
        return gitLabService.getAllProjects();
    }

    @GetMapping("/commits")
    public List<CommitModel> getProjectCommits() throws GitLabApiException {
        System.out.println("MainController: /commits endpoint called. Requesting commits from GitLabService.");
        gitLabService.storeAllCommits(gitLabService.getAllCommitsForAllProjects());
        return gitLabService.getAllCommitsForAllProjects();
    }
    @PostMapping("/commit")
    public String getMethodName(@RequestBody CommitModel commit) {
        System.out.println("MainController: /commit endpoint called. Requesting commits from GitLabService with project ID: " + commit.getBranchName());
        gitLabService.storeCommit(commit);
        return new String();
    }
    
}

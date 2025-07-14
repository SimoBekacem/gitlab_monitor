package com.example.gitlab_monitor.controller;


import org.gitlab4j.api.GitLabApiException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.example.gitlab_monitor.POJO.GitLabPushEvent;
import com.example.gitlab_monitor.model.CommitModel;
import com.example.gitlab_monitor.service.GitLabService;
import static com.example.gitlab_monitor.util.DateUtils.isoToCommitDate;






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
    @PostMapping("/hook")
    public void postMethodName(@RequestBody GitLabPushEvent payload, @RequestHeader(value = "X-Gitlab-Token", required = false) String token) {
        if (!"push".equals(payload.getEvent_name()) || payload.getCommits() == null) {
            System.out.println("Not a push event");
            return;
        }
        String branch = payload.getRef().replace("refs/heads/", "");
        String project = payload.getProject().getName();
        
        for (GitLabPushEvent.Commit c : payload.getCommits()) {
            CommitModel commit = new CommitModel();
            commit.setCommitId(c.getId());
            commit.setCommitterName(c.getAuthor().getName());
            commit.setCommitDate(isoToCommitDate(c.getTimestamp()));
            commit.setMessage(c.getMessage());
            commit.setBranchName(branch);
            commit.setProjectName(project);
            System.out.println("the commit with name " + commit.getCommitId() + " has been saved to the database: " + branch);
            gitLabService.storeCommit(commit);
        }
        System.out.println("MainController: /hook endpoint called. the commit has been saved to the database: " + branch);
        return;
    }
    
}

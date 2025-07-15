package com.example.gitlab_monitor.controller;


import static com.example.gitlab_monitor.util.DateUtils.isoToCommitDate;

import org.gitlab4j.api.GitLabApiException;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.gitlab_monitor.POJO.GitLabPushEvent;
import com.example.gitlab_monitor.model.CommitModel;
import com.example.gitlab_monitor.service.GitLabService;






@RestController
public class MainController {
   private final GitLabService gitLabService;
   private static final Logger logger = org.slf4j.LoggerFactory.getLogger(MainController.class);

    public MainController(GitLabService gitLabService) {
        this.gitLabService = gitLabService;
    }

    @GetMapping("/sync-commits")
    public String syncCommits() {
        try {
            logger.info("📥 Endpoint [/sync-commits] called ");
            gitLabService.addCommitFromLastCommit();
        } catch (GitLabApiException e) {
            logger.error("❌ Endpoint [/sync-commits] failed: {}", e.getMessage(), e);
            e.printStackTrace();
        }
        return new String();
    }
    
    @GetMapping("/sync-commits-db")
    public void getProjectCommits() throws GitLabApiException {
        logger.info("📥 Endpoint [/sync-commits-db] called ");
        gitLabService.getAllCommitsForAllProjects();
    }
    @PostMapping("/commit-system-hook")
    public void postMethodName(@RequestBody GitLabPushEvent payload) {
        logger.info("📥 Endpoint [/commit-system-hook] called ");
        if (!"push".equals(payload.getEvent_name()) || payload.getCommits() == null) {
            logger.warn("⚠️ Webhook ignored — event is not a push event.");
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
            gitLabService.storeCommit(commit);
        }
        return;
    }
    
}

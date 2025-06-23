package com.example.gitlab_monitor.service;

import com.example.gitlab_monitor.model.CommitInfo; // Import the CommitInfo DTO
import com.example.gitlab_monitor.model.UserCommitInfo;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.Commit;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Date;
import java.util.HashMap;
import java.time.Instant;
import java.time.format.DateTimeParseException;

/**
 * Service that interacts with the GitLab API to fetch project and commit information.
 */
@Service
public class GitLabService {

    private static final String GITLAB_URL = "http://localhost:8000";
    private static final String GITLAB_PRIVATE_TOKEN = "glpat-3Mg3WYdoV9qAx7s2sodC";

    private final GitLabApi gitLabApi;

    public GitLabService() {
        this.gitLabApi = new GitLabApi(GITLAB_URL, GITLAB_PRIVATE_TOKEN);
        System.out.println("GitLabService initialized with API URL: " + GITLAB_URL);
    }

    
    public List<Project> getAllProjects() throws GitLabApiException {
        System.out.println("GitLabService: Fetching all projects from GitLab.");
        return gitLabApi.getProjectApi().getProjects();
    }

    
    public List<CommitInfo> getAllCommitsForAllProjects() throws GitLabApiException {
        List<CommitInfo> allCommitsInfo = new ArrayList<>();
        List<Project> projects = getAllProjects();
        System.out.println("GitLabService: Found " + projects.size() + " projects.");
        for (Project project : projects) {
            fetchCommitsForProject(project, allCommitsInfo);
        }
        System.out.println("GitLabService: Total commits collected: " + allCommitsInfo.size());
        return allCommitsInfo;
    }

    
    private void fetchCommitsForProject(Project project, List<CommitInfo> allCommitsInfo) throws GitLabApiException {
        try {
            Date since = Date.from(Instant.parse("2017-01-01T00:00:00Z"));
            Date until = new Date();
            fetchCommitsForProject(project, since, until, allCommitsInfo);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Failed to parse start date for commits", e);
        }
    }

    private void fetchCommitsForProject(Project project, Date since, Date until, List<CommitInfo> allCommitsInfo) throws GitLabApiException {
        String branchName = project.getDefaultBranch();
        if (branchName == null || branchName.isEmpty()) {
            System.out.println("GitLabService: Project " + project.getName() + " (ID: " + project.getId() + ") has no default branch. Skipping commit fetch.");
            return;
        }

        System.out.println("GitLabService: Fetching commits for project: " + project.getName() + " (ID: " + project.getId() + ", Branch: " + branchName + ")");
        List<Commit> commits = gitLabApi.getCommitsApi().getCommits(project.getId(), branchName, since, until);

        if (commits != null && !commits.isEmpty()) {
            for (Commit commit : commits) {
                addCommitInfo(commit, project, allCommitsInfo);
            }
            System.out.println("GitLabService: Fetched " + commits.size() + " commits for project: " + project.getName());
        } else {
            System.out.println("GitLabService: No commits found for project: " + project.getName() + " in branch " + branchName + " within the specified date range.");
        }
    }

    
    private void addCommitInfo(Commit commit, Project project, List<CommitInfo> allCommitsInfo) {
        String committerName = commit.getCommitterName() != null ? commit.getCommitterName() : commit.getAuthorName();

        allCommitsInfo.add(new CommitInfo(
            project.getName(),
            committerName,
            commit.getCommittedDate(),
            commit.getId(),
            commit.getMessage()
        ));
    }
    
    public List<UserCommitInfo> getUsersCommitsInfo(List<CommitInfo> allCommitsInfo) {
        Map<String, Integer> commitCountsByUser = new HashMap<>();
        for (CommitInfo commitInfo : allCommitsInfo) {
            String userName = commitInfo.getCommitterName();
            commitCountsByUser.put(userName, commitCountsByUser.getOrDefault(userName, 0) + 1);
        }
        return commitCountsByUser.entrySet().stream()
            .map(entry -> new UserCommitInfo(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }
    
}

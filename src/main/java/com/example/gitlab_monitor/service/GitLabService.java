package com.example.gitlab_monitor.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Branch;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.Project;
import org.springframework.stereotype.Service;

import com.example.gitlab_monitor.model.CommitModel;
import com.example.gitlab_monitor.repository.CommitsRepository;

/**
 * Service that interacts with the GitLab API to fetch project and commit information.
 */
@Service
public class GitLabService {

    private static final String GITLAB_URL = "http://localhost:8000";
    private static final String GITLAB_PRIVATE_TOKEN = "glpat-3Mg3WYdoV9qAx7s2sodC";
    private final CommitsRepository commitRepository;

    private final GitLabApi gitLabApi;

    public GitLabService(CommitsRepository commitRepository) {
        this.gitLabApi = new GitLabApi(GITLAB_URL, GITLAB_PRIVATE_TOKEN);
        System.out.println("GitLabService initialized with API URL: " + GITLAB_URL);
        this.commitRepository = commitRepository;
    }

    
    public List<Project> getAllProjects() throws GitLabApiException {
        System.out.println("--- Starting getAllProjects processing ---");
        List<Project> projects = gitLabApi.getProjectApi().getProjects();
        System.out.println("GitLabService: Found " + projects.size() + " projects.");
        System.out.println("--- Finished getAllProjects processing ---");
        return projects;
    }

    
    public List<CommitModel> getAllCommitsForAllProjects() throws GitLabApiException {
        System.out.println("--- Starting getAllCommitsForAllProjects processing ---");
        List<CommitModel> allCommitsInfo = new ArrayList<>();
        List<Project> projects = getAllProjects();
        System.out.println("GitLabService: Found " + projects.size() + " projects.");
        for (Project project : projects) {
            fetchCommitsForProject(project, allCommitsInfo);
        }
        System.out.println("GitLabService: Total commits collected: " + allCommitsInfo.size());
        System.out.println("--- Finished getAllCommitsForAllProjects processing ---");
        return allCommitsInfo;
    }
    
    public void storeAllCommits(List<CommitModel> allCommitsInfo) {
        System.out.println("--- Starting storeAllCommits processing ---");
        System.out.println("Number of commits received from GitLab API for processing: " + allCommitsInfo.size());
    
        if (!allCommitsInfo.isEmpty()) {
            System.out.println("Sample fetched commit IDs (from GitLab API): " +
                    allCommitsInfo.stream().limit(5).map(CommitModel::getCommitId).collect(Collectors.joining(", ")));
        } else {
            System.out.println("No commits fetched from GitLab API to process.");
        }
    
        List<CommitModel> newCommits = allCommitsInfo.stream()
                .filter(commit -> {
                    String commitId = commit.getCommitId();
                    boolean exists = commitRepository.existsById(commitId);
                    return !exists;
                })
                .collect(Collectors.toList());
    
        System.out.println("Number of *new* commits identified for storage: " + newCommits.size());
    

        if (!newCommits.isEmpty()) {
            commitRepository.saveAll(newCommits);
            System.out.println("Successfully stored " + newCommits.size() + " new commits to the database.");
        } else {
            System.out.println("No new commits to store (all fetched commits already exist in DB or fetched list was empty).");
        }
    
        System.out.println("--- Finished storeAllCommits processing ---");
    }
    
    // private void fetchCommitsForProject(Project project, List<CommitModel> allCommitsInfo) throws GitLabApiException {
    //     System.out.println("--- Starting fetchCommitsForProject processing ---");
    //     try {
    //         Date since = Date.from(Instant.parse("2017-01-01T00:00:00Z"));
    //         Date until = new Date();
    //         fetchCommitsForProjectBranches(project, since, until, allCommitsInfo);
    //     } catch (DateTimeParseException e) {
    //         throw new RuntimeException("Failed to parse start date for commits", e);
    //     }
    //     System.out.println("--- Finished fetchCommitsForProject processing ---");
    // }

    private void fetchCommitsForProject(Project project, List<CommitModel> allCommitsInfo) throws GitLabApiException {
    System.out.println("--- Starting fetchCommitsForProject processing ---");
    try {
        // Calculate the 'since' date as 24 hours ago from the current moment
        Date until = new Date(); // Current time
        Instant twentyFourHoursAgo = Instant.now().minus(24, ChronoUnit.HOURS);
        Date since = Date.from(twentyFourHoursAgo);

        fetchCommitsForProjectBranches(project, since, until, allCommitsInfo);
    } catch (Exception e) { // Catching a more general Exception as DateTimeParseException is no longer relevant for the 'since' calculation
        throw new RuntimeException("Failed to fetch commits for project due to an unexpected error", e);
    }
    System.out.println("--- Finished fetchCommitsForProject processing ---");
}

    private void fetchCommitsForProjectBranches(Project project, Date since, Date until, List<CommitModel> allCommitsInfo) throws GitLabApiException {
        System.out.println("--- Starting fetchCommitsForProjectBranches processing ---");
        List<Branch> branches = gitLabApi.getRepositoryApi().getBranches(project.getId());

        if (branches == null || branches.isEmpty()) {
            return;
        }

        for (Branch branch : branches) {
            String branchName = branch.getName();
            List<Commit> commits = gitLabApi.getCommitsApi().getCommits(project.getId(), branchName, since, until);

            if (commits != null && !commits.isEmpty()) {
                for (Commit commit : commits) {
                    addCommitInfo(commit, project, allCommitsInfo, branchName);
                }
            } else {
                System.out.println("No commits found for project: " + project.getName() + " in branch " + branchName + " within the specified date range.");
            }
        }
        System.out.println("--- Finished fetchCommitsForProjectBranches processing ---");
    }

    
    private void addCommitInfo(Commit commit, Project project, List<CommitModel> allCommitsInfo, String branchName) {
        String committerName = commit.getCommitterName() != null ? commit.getCommitterName() : commit.getAuthorName();

        allCommitsInfo.add(new CommitModel(
            project.getName(),
            committerName,
            commit.getCommittedDate(),
            commit.getId(),
            commit.getMessage(),
            branchName
        ));
    }
    
}

package com.example.gitlab_monitor.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Branch;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.Project;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
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

    // store all commits from all projects branches on the db and doesn't return anything
    public void getAllCommitsForAllProjects() throws GitLabApiException {
        List<Project> projects = getAllProjects();
        System.out.println("GitLabService: Found " + projects.size() + " projects.");
        for (Project project : projects) {
            fetchCommitsForProject(project);
        }
    }

    // get all projects from gitlab
    public List<Project> getAllProjects() throws GitLabApiException {
        List<Project> projects = gitLabApi.getProjectApi().getProjects();
        System.out.println("GitLabService: Found " + projects.size() + " projects.");
        return projects;
    }

// this is to split the date range in years
    public static List<Date> getDateRange(Date since, Date until) {
        List<Date> dates = new ArrayList<>();
        
        LocalDate start = since.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = until.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        while (!start.isAfter(end)) {
            Instant instant = start.atStartOfDay(ZoneId.systemDefault()).toInstant();
            dates.add(Date.from(instant));
            start = start.plusYears(1);
        }
        dates.add(until);
        return dates;
    }

    
    private void fetchCommitsForProject(Project project) throws GitLabApiException {
        System.out.println("[fetchCommitsForProject] Fetching commits for project: " + project.getName());
        try {
            Date until = new Date(); // Current time
            Date since = Date.from(Instant.parse("2025-01-01T00:00:00Z"));
            List<Date> dates = getDateRange(since, until);
            System.out.println(dates.size());
            for (int index = 0; index < dates.size() - 1; index++) {
                System.out.println("index: " + index);
                System.out.println("[fetchCommitsForProject] Fetching commits for project: " + project.getName() + " from " + dates.get(index) + " to " + dates.get(index + 1));
                fetchCommitsForProjectBranches(project, dates.get(index), dates.get(index + 1));
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch commits for project due to an unexpected error", e);
        }
    }
    
    // get the commits from projects branches
    private void fetchCommitsForProjectBranches(Project project, Date since, Date until) throws GitLabApiException {
        List<Branch> branches = gitLabApi.getRepositoryApi().getBranches(project.getId());

        if (branches == null || branches.isEmpty()) {
            return;
        }

        for (Branch branch : branches) {
            String branchName = branch.getName();
            List<Commit> commits = gitLabApi.getCommitsApi().getCommits(project.getId(), branchName, since, until);

            if (commits != null && !commits.isEmpty()) {
                for (Commit commit : commits) {
                    addCommitInfo(commit, project, branchName);
                }
            } else {
                System.out.println("[fetchCommitsForProjectBranches] No commits found for project: " + project.getName() + " in branch " + branchName + " within the specified date range.");
            }
        }
    }

    
    private void addCommitInfo(Commit commit, Project project, String branchName) {
        String committerName = commit.getCommitterName() != null ? commit.getCommitterName() : commit.getAuthorName();
        CommitModel newCommit = new CommitModel(
            project.getName(),
            committerName,
            commit.getCommittedDate(),
            commit.getId(),
            commit.getMessage(),
            branchName
        );
        commitRepository.save(newCommit);
    }
    
    public void storeCommit(CommitModel commit) {
        commitRepository.save(commit);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void addCommitFromLastCommit() throws GitLabApiException {
        try {
            Date until = new Date(); // Current time
            String commitDateString = commitRepository.findLastCommit().getCommitDate();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
            Date since = sdf.parse(commitDateString);
            System.out.println("GitLabService: Fetching commits for project from " + since + " to " + until);
            List<Project> projects = getAllProjects();
            System.out.println("GitLabService: Found " + projects.size() + " projects.");
            for (Project project : projects) {
                System.out.println("[addCommitFromLastCommit] Fetching commits for project: " + project.getName());
                fetchCommitsForProjectBranches(project, since, until);
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch commits for project due to an unexpected error", e);
        }
    }
}

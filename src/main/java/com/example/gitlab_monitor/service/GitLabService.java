package com.example.gitlab_monitor.service;

import static com.example.gitlab_monitor.util.DateUtils.getDateRange;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Branch;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.Project;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.example.gitlab_monitor.model.CommitModel;
import com.example.gitlab_monitor.repository.CommitsRepository;

@Service
public class GitLabService {

    private final CommitsRepository commitRepository;
    private final GitLabApi gitLabApi;
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(GitLabService.class);

    public GitLabService(
        CommitsRepository commitRepository, 
        @Value("${gitlab.url}") String GITLAB_URL, 
        @Value("${gitlab.token}") String GITLAB_PRIVATE_TOKEN
    ) {
        this.gitLabApi = new GitLabApi(GITLAB_URL, GITLAB_PRIVATE_TOKEN);
        logger.info("✅ GitLabService [Constructor] initialized with API URL: {}", GITLAB_URL);
        this.commitRepository = commitRepository;
    }

    // store all commits from all projects branches on the db and doesn't return anything
    public void getAllCommitsForAllProjects() throws GitLabApiException {
        List<Project> projects = getAllProjects();
        for (Project project : projects) {
            fetchCommitsForProject(project);
        }
    }

    // get all projects from gitlab
    public List<Project> getAllProjects() throws GitLabApiException {
        List<Project> projects = gitLabApi.getProjectApi().getProjects();
        logger.info("📦 GitLabService [getAllCommitsForAllProjects]: Found {} projects.", projects.size());
        return projects;
    }

    private void fetchCommitsForProject(Project project) throws GitLabApiException {
        logger.info("📥 GitLabService [fetchCommitsForProject]: Fetching commits for project: {}", project.getName());
        try {
            Date until = new Date(); // Current time
            Date since = Date.from(Instant.parse("2025-01-01T00:00:00Z"));
            List<Date> dates = getDateRange(since, until);
            System.out.println(dates.size());
            for (int index = 0; index < dates.size() - 1; index++) {
                logger.info("📥 GitLabService [fetchCommitsForProject]: Fetching commits for project: {} from {} to {}", 
                            project.getName(), dates.get(index), dates.get(index + 1));
                fetchCommitsForProjectBranches(project, dates.get(index), dates.get(index + 1));
            }
            
        } catch (Exception e) {
            logger.error("❌ GitLabService [fetchCommitsForProject]: Failed to fetch commits for project due to an unexpected error: {}", e.getMessage(), e);
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
                    logger.warn("⚠️ GitLabService [fetchCommitsForProjectBranches]: No new commits found for project: {} in branch: {} within the specified date range.",
                    project.getName(), branchName);
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
        storeCommit(newCommit);
    }
    
    public void storeCommit(CommitModel commit) {
        commitRepository.save(commit);
        logger.info("✅ Commit [{}] on branch [{}] has been saved to the database.", commit.getCommitId(), commit.getBranchName());

    }

    @EventListener(ApplicationReadyEvent.class)
    public void addCommitFromLastCommit() throws GitLabApiException {
        try {
            Date until = new Date();
            if(commitRepository.findLastCommit() == null) {
                logger.warn("⚠️ GitLabService [addCommitFromLastCommit]: No commits found in the database.");
                return;
            }
            String commitDateString = commitRepository.findLastCommit().getCommitDate();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
            Date since = sdf.parse(commitDateString);
            logger.info("📥 GitLabService [addCommitFromLastCommit]: Fetching commits for project from {} to {}", since, until);
            List<Project> projects = getAllProjects();
            for (Project project : projects) {
                logger.info("📥 GitLabService [addCommitFromLastCommit]: Fetching commits for project: {}", project.getName());
                fetchCommitsForProjectBranches(project, since, until);
            }
            logger.info("✅ GitLabService [addCommitFromLastCommit]: Successfully fetched commits for all projects.");
        } catch (Exception e) {
            logger.error("❌ GitLabService [addCommitFromLastCommit]: Failed to fetch commits for project due to an unexpected error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch commits for project due to an unexpected error", e);
        }
    }
}

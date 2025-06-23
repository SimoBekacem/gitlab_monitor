import gitlab
import os
import random
import string
import time
from datetime import datetime
from concurrent.futures import ThreadPoolExecutor, as_completed
import git

# --- Configuration ---
GITLAB_URL = "http://localhost:8000"  # Your GitLab container URL
GITLAB_PRIVATE_TOKEN = "glpat-3Mg3WYdoV9qAx7s2sodC"  # <<< IMPORTANT: Replace with your actual token
PROJECT_NAME = "performance-test-project-1"
LOCAL_REPO_PATH = "./performance-test-project-1"
NUMBER_OF_COMMITS = 100
MAX_WORKERS = 10  # Number of concurrent threads for pushing commits (adjust as needed)

# --- GitLab API Setup ---
gl = gitlab.Gitlab(GITLAB_URL, private_token=GITLAB_PRIVATE_TOKEN)

def get_or_create_project():
    """Gets an existing project or creates a new one."""
    print(f"Checking for project: {PROJECT_NAME}...")
    try:
        project = gl.projects.get(PROJECT_NAME)
        print(f"Using existing project: {PROJECT_NAME} (ID: {project.id})")
        return project
    except gitlab.exceptions.GitlabError:
        print(f"Project {PROJECT_NAME} not found. Creating a new one...")
        project = gl.projects.create({
            'name': PROJECT_NAME,
            'initialize_with_readme': True,
            'visibility': 'private'
        })
        print(f"Created new project: {PROJECT_NAME} (ID: {project.id})")
        # Give GitLab a moment to initialize the project and README
        time.sleep(2)
        return project

def clone_or_pull_repo(repo_url, local_path):
    """Clones the repository or pulls if it already exists."""
    if os.path.exists(local_path):
        print(f"Repository already exists at {local_path}. Pulling latest changes...")
        repo = git.Repo(local_path)
        origin = repo.remotes.origin
        origin.pull()
        return repo
    else:
        print(f"Cloning repository from {repo_url} to {local_path}...")
        repo = git.Repo.clone_from(repo_url, local_path)
        print("Cloning complete.")
        return repo

def generate_random_string(length=10):
    """Generates a random string of fixed length."""
    letters = string.ascii_lowercase
    return ''.join(random.choice(letters) for i in range(length))

def make_and_push_commit(repo_path, commit_num):
    """
    Creates a new file, commits it, and pushes to the remote repository.
    This function is designed to be run concurrently.
    """
    try:
        repo = git.Repo(repo_path)
        # Ensure the working directory is clean before making changes
        if repo.is_dirty(untracked_files=True):
            print(f"Worker {commit_num}: Repository is dirty. Stashing changes or handling manually.")
            # For this test, we'll assume a clean state or that each worker
            # will push unique files. If issues arise, consider:
            # repo.git.reset('--hard', 'HEAD')
            # repo.git.clean('-fd')

        timestamp = datetime.now().strftime("%Y%m%d-%H%M%S-%f")
        filename = f"commit_test_{commit_num}_{timestamp}.txt"
        file_path = os.path.join(repo_path, filename)
        content = f"This is commit number {commit_num} at {timestamp} with some random data: {generate_random_string(50)}\n"

        with open(file_path, "w") as f:
            f.write(content)

        repo.index.add([filename])
        commit_message = f"Automated commit {commit_num} - {timestamp}"
        repo.index.commit(commit_message)

        origin = repo.remotes.origin
        origin.push()
        print(f"Successfully pushed commit {commit_num} for {filename}")
        return True
    except Exception as e:
        print(f"Error pushing commit {commit_num} for {filename}: {e}")
        return False

def main():
    start_time = time.time()
    print("Starting GitLab performance test script...")

    # # 1. Get or Create Project
    # try:
    #     project = get_or_create_project()
    #     repo_url = project.http_url_to_repo
    # except gitlab.exceptions.GitlabError as e:
    #     print(f"Failed to get/create project: {e}")
    #     return

    # # 2. Clone/Pull Repository
    # try:
    #     local_repo = clone_or_pull_repo(repo_url, LOCAL_REPO_PATH)
    #     # Ensure the local repository is on the main branch for consistency
    #     local_repo.git.checkout('main' if 'main' in local_repo.branches else 'master')
    # except Exception as e:
    #     print(f"Failed to clone/pull repository: {e}")
    #     return

    # 3. Concurrent Commits and Pushes
    print(f"\nMaking {NUMBER_OF_COMMITS} concurrent commits and pushes...")
    successful_commits = 0
    failed_commits = 0
    futures = []

    with ThreadPoolExecutor(max_workers=MAX_WORKERS) as executor:
        for i in range(1, NUMBER_OF_COMMITS + 1):
            # Pass a unique local repo path to each worker to avoid race conditions
            # in the working directory (each worker gets its own cloned copy or works on a separate temp dir)
            # For simplicity, we'll use the same repo path, but advise caution if many workers touch the same files.
            # For creating *new* unique files, this approach is fine.
            futures.append(executor.submit(make_and_push_commit, LOCAL_REPO_PATH, i))

        for future in as_completed(futures):
            if future.result():
                successful_commits += 1
            else:
                failed_commits += 1

    end_time = time.time()
    duration = end_time - start_time

    print("\n--- Test Results ---")
    print(f"Total time taken: {duration:.2f} seconds")
    print(f"Successful commits: {successful_commits}")
    print(f"Failed commits: {failed_commits}")

    if successful_commits == NUMBER_OF_COMMITS:
        print("All commits pushed successfully!")
    else:
        print("Some commits failed. Check the error messages above.")

    print("\nScript finished.")

if __name__ == "__main__":
    main()
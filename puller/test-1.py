import os
import random
import string
import time
from datetime import datetime
from concurrent.futures import ThreadPoolExecutor, as_completed
import git
import shutil # For removing directories
import tempfile # For creating temporary directories

# --- Configuration ---
# IMPORTANT:
# Replace 'glpat-YOUR_PRIVATE_TOKEN' with your actual GitLab Personal Access Token.
# The 'oauth2' username is standard for PATs over HTTP.
# Ensure 'performance-test-project-1' is the correct project path on your GitLab instance.
# If your GitLab URL is different (e.g., :80), adjust localhost:8000.
GITLAB_HTTP_REPO_URL = "http://oauth2:glpat-3Mg3WYdoV9qAx7s2sodC@localhost:8000/root/performance-test-project.git"

NUMBER_OF_COMMITS = 100
MAX_WORKERS = 10 # Number of concurrent threads (each will have its own clone)
BASE_BRANCH = 'main' # The base branch to clone from (e.g., 'main' or 'master')

def generate_random_string(length=10):
    """Generates a random string of fixed length."""
    letters = string.ascii_lowercase
    return ''.join(random.choice(letters) for i in range(length))

def make_and_push_commit_worker(worker_id, base_repo_url, base_branch):
    """
    Worker function to clone the repo, create a new file, commit it, and push to a unique branch.
    Each worker will operate on its own independent clone in a temporary directory.
    """
    temp_repo_path = None # Initialize to None for cleanup in finally block
    commit_filename = None # To track the filename for error messages

    try:
        # Create a unique temporary directory for this worker's clone
        temp_repo_path = tempfile.mkdtemp(prefix=f"git_stress_test_worker_{worker_id}_")
        # print(f"Worker {worker_id}: Cloning repo to {temp_repo_path}...")

        # Clone the repository into the temporary directory
        repo = git.Repo.clone_from(base_repo_url, temp_repo_path, branch=base_branch)
        # print(f"Worker {worker_id}: Successfully cloned.")

        # Create a unique branch name for this commit
        timestamp = datetime.now().strftime("%Y%m%d-%H%M%S-%f")
        new_branch_name = f"worker-{worker_id}-commit-{timestamp}"

        # Create and checkout the new branch
        new_branch = repo.create_head(new_branch_name)
        new_branch.checkout()
        # print(f"Worker {worker_id}: Checked out new branch: {new_branch_name}")

        # Create the new file within this worker's unique clone
        commit_filename = f"commit_{worker_id}_{timestamp}.txt"
        file_path = os.path.join(temp_repo_path, commit_filename)
        content = f"This is commit from worker {worker_id} at {timestamp} with random data: {generate_random_string(50)}\n"

        with open(file_path, "w") as f:
            f.write(content)

        # Git operations (add, commit, push) are now safe as they are isolated per worker
        repo.index.add([file_path]) # Add file path directly
        commit_message = f"Automated commit from worker {worker_id} - {timestamp}"
        repo.index.commit(commit_message)
        # print(f"Worker {worker_id}: Committed changes to {commit_filename}.")

        origin = repo.remotes.origin
        # Push the commit to the newly created branch on the remote
        origin.push(new_branch_name)

        print(f"Worker {worker_id}: Successfully pushed commit for {commit_filename} to branch {new_branch_name}")
        return True
    except git.exc.GitCommandError as git_error:
        # Catch specific Git errors for more detailed logging
        print(f"Worker {worker_id}: GitCommandError pushing commit for {commit_filename or 'file'}: {git_error}")
        print(f"Worker {worker_id}: Stderr: {git_error.stderr.strip()}")
        return False
    except Exception as e:
        print(f"Worker {worker_id}: Unexpected error pushing commit for {commit_filename or 'file'}: {e}")
        return False
    finally:
        # Clean up the temporary directory after the worker finishes (or fails)
        if temp_repo_path and os.path.exists(temp_repo_path):
            try:
                shutil.rmtree(temp_repo_path)
                # print(f"Worker {worker_id}: Cleaned up temporary directory {temp_repo_path}")
            except OSError as e:
                print(f"Worker {worker_id}: Error cleaning up temporary directory {temp_repo_path}: {e}")


def main():
    start_time = time.time()
    print("Starting GitLab concurrent push test script (isolated clones, unique branches)...")

    # Critical check for PAT replacement
    if "glpat-YOUR_PRIVATE_TOKEN" in GITLAB_HTTP_REPO_URL:
        print("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
        print("!!! WARNING: Please replace 'glpat-YOUR_PRIVATE_TOKEN' in GITLAB_HTTP_REPO_URL !!!")
        print("!!!          with your actual GitLab Personal Access Token.       !!!")
        print("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
        return

    print(f"\nMaking {NUMBER_OF_COMMITS} concurrent commits and pushing each to a unique branch...")
    successful_commits = 0
    failed_commits = 0
    futures = []

    with ThreadPoolExecutor(max_workers=MAX_WORKERS) as executor:
        for i in range(1, NUMBER_OF_COMMITS + 1):
            futures.append(executor.submit(make_and_push_commit_worker, i, GITLAB_HTTP_REPO_URL, BASE_BRANCH))

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
        print("Note: The previous 'failed to push some refs' messages were warnings,")
        print("      this version should avoid them by using unique branches.")

    print("\nScript finished.")

if __name__ == "__main__":
    main()
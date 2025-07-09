import http from "k6/http";
import { check, sleep } from "k6";

const BASE_URL = "http://localhost:8000/api/v4";
const PRIVATE_TOKEN = "glpat-4WsJZkxAJxjz8ioUWpH-";
const PROJECT_ID = 1; // Change to your project ID

// Generate a unique branch name each time
function randomBranchName() {
  return "k6-branch-" + Math.random().toString(36).substring(7);
}

export let options = {
  vus: 10, // 10 concurrent users
  iterations: 200, // 200 total commits
  thresholds: {
    http_req_failed: ["rate<0.01"], // allow <1% failure
    http_req_duration: ["p(95)<5000"], // 95% under 5s
  },
};

export default function () {
  const branchName = randomBranchName();
  const commitMessage = "K6 test commit";

  const headers = {
    "PRIVATE-TOKEN": PRIVATE_TOKEN,
    "Content-Type": "application/json",
  };

  // 1. Get default branch
  let projectRes = http.get(`${BASE_URL}/projects/${PROJECT_ID}`, { headers });
  const defaultBranch = JSON.parse(projectRes.body).default_branch;

  // 2. Create a new branch
  const createBranchRes = http.post(
    `${BASE_URL}/projects/${PROJECT_ID}/repository/branches?branch=${branchName}&ref=${defaultBranch}`,
    null,
    { headers }
  );
  check(createBranchRes, {
    "branch created": (res) => res.status === 201,
  });

  // 3. Create a commit with a new file
  const commitPayload = JSON.stringify({
    branch: branchName,
    commit_message: commitMessage,
    actions: [
      {
        action: "create",
        file_path: `k6-file-${Math.random().toString(36).substring(7)}.txt`,
        content: "This is a file committed by k6",
      },
    ],
  });

  const commitRes = http.post(
    `${BASE_URL}/projects/${PROJECT_ID}/repository/commits`,
    commitPayload,
    { headers }
  );
  check(commitRes, {
    "commit created": (res) => res.status === 201,
  });

  // 4. Create a merge request
  const mrPayload = JSON.stringify({
    source_branch: branchName,
    target_branch: defaultBranch,
    title: "K6 Test MR",
  });

  const mrRes = http.post(
    `${BASE_URL}/projects/${PROJECT_ID}/merge_requests`,
    mrPayload,
    { headers }
  );

  check(mrRes, {
    "merge request created": (res) => res.status === 201,
  });

  sleep(1); // Pause between iterations
}

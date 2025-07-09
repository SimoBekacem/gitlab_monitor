import http from "k6/http";
import { check } from "k6";

// Options for stress/load
export let options = {
  vus: 10,
  iterations: 100,
};

// UUID generator for unique commit IDs
function uuidv4() {
  return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, function (c) {
    const r = (Math.random() * 16) | 0,
      v = c === "x" ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

// Generate current ISO timestamp
function isoTimestamp() {
  return new Date().toISOString(); // e.g., "2025-07-09T13:00:00Z"
}

export default function () {
  const url = "http://localhost:5000/gitlab-webhook";

  const payload = JSON.stringify({
    event_name: "push",
    ref: "refs/heads/test-branch",
    project: {
      name: "test-project",
    },
    commits: [
      {
        id: uuidv4(),
        message: "Testing K6 commit POST",
        timestamp: isoTimestamp(),
        author: {
          name: "Mohamed Belkacem",
        },
      },
    ],
  });

  const params = {
    headers: {
      "Content-Type": "application/json",
      "X-Gitlab-Token": "your-token", // If your webhook expects it, add here
    },
  };

  let res = http.post(url, payload, params);

  check(res, {
    "status is 200": (r) => r.status === 200,
    "response time < 5s": (r) => r.timings.duration < 5000,
  });
}

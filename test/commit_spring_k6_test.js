import http from "k6/http";
import { check } from "k6";

export let options = {
  vus: 10, // Number of virtual users
  iterations: 100, // Total number of requests (2 per VU)
};

// UUIDv4 generator
function uuidv4() {
  return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, function (c) {
    const r = (Math.random() * 16) | 0,
      v = c === "x" ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

export default function () {
  const url = "http://localhost:8080/commit";

  const payload = JSON.stringify({
    commitId: uuidv4(),
    projectName: "test-project",
    committerName: "Mohamed Belkacem",
    commitDate: "2025-07-09 15:00:00 +0100",
    message: "Testing K6 commit POST",
    branchName: "test-branch",
  });

  const params = {
    headers: {
      "Content-Type": "application/json",
    },
  };

  let res = http.post(url, payload, params);

  check(res, {
    "status is 200": (r) => r.status === 200,
    "response time < 5s": (r) => r.timings.duration < 5000,
  });
}

import http from "k6/http";
import { check, sleep } from "k6";

export let options = {
  vus: 10, // virtual users
  duration: "30s", // test duration
};

export default function () {
  let res = http.get("http://localhost:8080/commits");
  console.log(`Status: ${res.status}`);
  console.log(`Duration: ${res.timings.duration}ms`);
  check(res, {
    "status is 200": (r) => r.status === 200,
    "response time < 10s": (r) => r.timings.duration < 10000,
  });
}

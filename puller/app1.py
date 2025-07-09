import os, requests
from datetime import datetime, timezone
from flask import Flask, request, jsonify

app = Flask(__name__)
SPRING_BOOT_ENDPOINT = "http://localhost:8080/commit"
WEBHOOK_SECRET      = os.getenv("GITLAB_WEBHOOK_SECRET")

def iso_to_commit_date(ts: str) -> str:
    """Convert 2025-07-07T15:24:55+00:00 → 2025-07-07 15:24:55 +0000"""
    dt = datetime.fromisoformat(ts.replace("Z", "+00:00"))
    return dt.astimezone(timezone.utc).strftime("%Y-%m-%d %H:%M:%S %z")

@app.route("/gitlab-webhook", methods=["POST"])
def gitlab_webhook():
    if WEBHOOK_SECRET and request.headers.get("X-Gitlab-Token") != WEBHOOK_SECRET:
        return jsonify({"message": "Invalid secret token"}), 401

    payload = request.get_json(force=True)
    if payload.get("event_name") != "push" or not payload.get("commits"):
        print("Not a push event")
        return jsonify({"message": "Not a push event"}), 200

    # Push events may contain multiple commits
    for c in payload["commits"]:
        commit_json = {
            "commitId"      : c["id"],
            "projectName"   : payload["project"]["name"],
            "committerName" : c["author"]["name"],
            "commitDate"    : iso_to_commit_date(c["timestamp"]),
            "message"       : c["message"],
            "branchName"    : payload["ref"].split("/")[-1]  # refs/heads/main → main
        }

        try:
            resp = requests.post(
                SPRING_BOOT_ENDPOINT,
                json=commit_json,          # ← send as JSON, not form‑data
                headers={"Content-Type": "application/json"}
            )
            resp.raise_for_status()
            print(f"Sent commit {c['id']} on the branch {payload['ref']} → Spring Boot OK ({resp.status_code})")
        except requests.RequestException as e:
            print(f"Failed to send commit {c['id']}: {e}")

    return jsonify({"message": "Commits forwarded"}), 200

@app.route("/")
def index():
    return "GitLab webhook listener running."

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)

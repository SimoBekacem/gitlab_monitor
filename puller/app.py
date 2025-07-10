import os
import requests
from flask import Flask, request, jsonify

# Initialize the Flask application
app = Flask(__name__)

# --- Configuration ---
# The URL of Spring Boot application's endpoint
SPRING_BOOT_ENDPOINT = "http://localhost:8080/commits"

WEBHOOK_SECRET = os.environ.get("GITLAB_WEBHOOK_SECRET", None)

@app.route('/gitlab-webhook', methods=['POST'])
def gitlab_webhook():
    print("--- Received GitLab Webhook Request ---")

    # Verify the webhook secret if configured
    if WEBHOOK_SECRET:
        signature = request.headers.get('X-Gitlab-Token')
        if not signature or signature != WEBHOOK_SECRET:
            print(f"ERROR: Invalid or missing GitLab Secret Token. Expected: '{WEBHOOK_SECRET}', Received: '{signature}'")
            return jsonify({"message": "Invalid secret token"}), 401
        print("GitLab Secret Token verified.")

    print(f"Received GitLab Webhook Request. Event Name: {request.json.get('event_name')}")
    print(f"Request JSON: {request.json}")
    # Get commits just in push events
    if request.json.get('event_name') == 'push':
        # Call the Spring Boot endpoint
        print(f"Attempting to call Spring Boot endpoint: {SPRING_BOOT_ENDPOINT}")
        try:
            response = requests.get(SPRING_BOOT_ENDPOINT)
            response.raise_for_status() # Raise an exception for HTTP errors (4xx or 5xx)
            print(f"Successfully called Spring Boot endpoint. Status: {response.status_code}")
            print(f"Spring Boot Response: {response.text[:200]}...") # Print first 200 chars
            return response.text, response.status_code
        except requests.exceptions.RequestException as e:
            print(f"ERROR: Failed to call Spring Boot endpoint: {e}")
            return jsonify({"message": f"Failed to call Spring Boot endpoint: {e}"}), 500
        except Exception as e:
            print(f"An unexpected error occurred: {e}")
            return jsonify({"message": f"An unexpected error occurred: {e}"}), 500
    else:
        print(f"Received non-push event. Event Name: {request.json.get('event_name')}")
        return jsonify({"message": f"Received non-push event. Event Name: {request.json.get('event_name')}"}), 200

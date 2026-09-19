from flask import Flask, request, jsonify
import requests

app = Flask(__name__)

SECRET_KEY = "GURU-AI-SECRET-KEY-999"
LLAMA_SERVER_URL = "http://127.0.0.1:8080/v1/chat/completions"

@app.route("/v1/chat", methods=["POST"])
def chat():
    auth = request.headers.get("Authorization", "")
    if auth != f"Bearer {SECRET_KEY}":
        return jsonify({"error": "unauthorized"}), 401

    data = request.get_json(force=True)
    user_message = data.get("message", "")
    print("===INCOMING MESSAGE START===")
    print(user_message)
    print("===INCOMING MESSAGE END===")

    try:
        payload = {
            "messages": [
                {"role": "system", "content": "You are Guru, a helpful assistant. Reply directly and briefly in the same language the user used."},
                {"role": "user", "content": user_message}
            ],
            "max_tokens": 200
        }
        resp = requests.post(LLAMA_SERVER_URL, json=payload, timeout=60)
        resp_json = resp.json()
        reply = resp_json["choices"][0]["message"]["content"]
    except Exception as e:
        reply = f"(Gemma error: {e})"

    return jsonify({"reply": reply})

@app.route("/v1/health", methods=["GET"])
def health():
    return jsonify({"status": "ok"})

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)

from flask import Flask, jsonify, make_response
app = Flask(__name__)

run = {
    "id": 1,
    "job": {
        "id": 1,
        "name": "job_name",
        "dockerImage": "busybox:1.31",
        "command": ["sh","-c","echo \"Hello world\""],
        "type": "FREESTYLE"
    },
    "phase": "IN_PROGRESS",
    "triggerAt": "2020-05-19 03:47:01",
    "completeAt": None,
    "exitCode": None,
    "runResults": [],
    "type": "FREESTYLE"
}

@app.route('/jobs/1/runs', methods=['POST'])
def trigger_run():
    return make_response(jsonify(run))

@app.route('/jobs/1/runs/1', methods=['GET'])
def get_run():
    return make_response(jsonify(run))

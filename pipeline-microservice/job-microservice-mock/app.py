from flask import Flask, jsonify, make_response
app = Flask(__name__)

class Run:

    def __init__(self):
        self.access_count = 0

    def _status(self):
        if self.access_count <= 1:
            return "IN_PROGRESS"
        if self.access_count >= 2:
            return "SUCCEED"

    def _get(self):
        return {
            "id": 1,
            "job": {
                "id": 1,
                "name": "job_name",
                "dockerImage": "busybox:1.31",
                "command": ["sh","-c","echo \"Hello world\""],
                "type": "FREESTYLE"
            },
            "status": self._status(),
            "triggerAt": "2020-05-19 03:47:01",
            "completeAt": None,
            "exitCode": None,
            "runResults": [],
            "type": "FREESTYLE"
        }

    def initialize(self):
        self.access_count = 0
        return self._get()

    def get(self):
        self.access_count += 1
        return self._get()

run = Run()

@app.route('/jobs/1/runs', methods=['POST'])
def trigger_run():
    # Always return IN_PROGRESS
    return make_response(jsonify(run.initialize()))

@app.route('/jobs/1/runs/1', methods=['GET'])
def get_run():
    # First time return IN_PROGRESS, second time return SUCCEED
    return make_response(jsonify(run.get()))

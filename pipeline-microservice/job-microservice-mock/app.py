from flask import Flask, jsonify, make_response
app = Flask(__name__)

class Run:

    def __init__(self, job_id, run_id):
        self.job_id = job_id
        self.run_id = run_id
        self.access_count = 0

    def _status(self):
        if self.access_count <= 2:
            return "IN_PROGRESS"
        if self.access_count >= 3:
            return "SUCCEED"

    def _get(self):
        return {
            "id": self.run_id,
            "job": {
                "id": self.job_id,
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

    def get(self):
        self.access_count += 1
        return self._get()


class RunBuilder:

    def __init__(self):
        # It is a nested dictionary, with the first layer key
        # is `job_id` and the second layer key is `run_id`.
        self.runs = {}

    def initialize(self, job_id):
        if job_id not in self.runs:
            self.runs[job_id] = {}

        # Not the same behavior as SQL, where different jobs
        # don't have collapse of `run_id`.
        if not self.runs[job_id]:
            run_id = 1
        else:
            run_id = max(self.runs[job_id].keys()) + 1

        run = Run(job_id, run_id)
        self.runs[job_id][run_id] = run
        return run.get()

    def get(self, job_id, run_id):
        return self.runs[job_id][run_id].get()


run_builder = RunBuilder()

@app.route('/jobs/<int:job_id>/runs', methods=['POST'])
def trigger_run(job_id):

    # Always return IN_PROGRESS
    return make_response(jsonify(run_builder.initialize(job_id)))

@app.route('/jobs/<int:job_id>/runs/<int:run_id>', methods=['GET'])
def get_run(job_id, run_id):

    # First time return IN_PROGRESS, second time return SUCCEED
    # Get a run which is not triggered (yet) will give back 500
    # error.
    return make_response(jsonify(run_builder.get(job_id, run_id)))

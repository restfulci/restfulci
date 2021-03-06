from flask import Flask, jsonify, make_response, request


app = Flask(__name__)


def job(job_id):
    return {
        "id": job_id,
        "name": "job_name",
        "dockerImage": "busybox:1.33",
        "command": ["sh", "-c", "echo \"Hello $ENV\""],
        "type": "FREESTYLE",
        "parameters": [
            {
                "id": job_id,
                "name": "ENV"
            }
        ]
    }


class Run:

    def __init__(self, job_id, run_id, final_status, params):
        self.job_id = job_id
        self.run_id = run_id
        self.final_status = final_status
        self.access_count = 0

        if "ENV" in params:
            self.env = params["ENV"]
        else:
            raise BadRequest("Missing input for ENV", "/jobs/{}/runs/{}".format(job_id, run_id))

        params.pop("ENV")
        for param_name in params:
            raise BadRequest(
                "Input {} is not in the associated job's parameter list".format(param_name),
                "/jobs/{}/runs/{}".format(job_id, run_id))

    def _status(self):
        if self.access_count <= 2:
            return "IN_PROGRESS"
        if self.access_count >= 3:
            return self.final_status

    def _get(self):
        return {
            "id": self.run_id,
            "job": job(self.job_id),
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

    def console(self):
        return "Hello {}".format(self.env)


class RunBuilder:

    def __init__(self):
        # It is a nested dictionary, with the first layer key
        # is `job_id` and the second layer key is `run_id`.
        self.runs = {}

    def initialize(self, job_id, final_status, params):
        if job_id not in self.runs:
            self.runs[job_id] = {}

        # Not the same behavior as SQL, where different jobs
        # don't have collapse of `run_id`.
        if not self.runs[job_id]:
            run_id = 1
        else:
            run_id = max(self.runs[job_id].keys()) + 1

        run = Run(job_id, run_id, final_status, params)
        self.runs[job_id][run_id] = run
        return run.get()

    def get(self, job_id, run_id):
        return self.runs[job_id][run_id].get()

    def console(self, job_id, run_id):
        return self.runs[job_id][run_id].console()


run_builder = RunBuilder()


class BadRequest(Exception):

    def __init__(self, message, path):
        Exception.__init__(self)
        self.message = message
        self.path = path

    def payload(self):
        return {
            "timestamp": "2020-06-04T04:37:37.154+0000",
            "status": 400,
            "error": "Bad Request",
            "message": self.message,
            "path": self.path
        }


class NotFound(Exception):

    def __init__(self, message, path):
        Exception.__init__(self)
        self.message = message
        self.path = path

    def payload(self):
        return {
            "timestamp": "2020-06-04T04:37:37.154+0000",
            "status": 404,
            "error": "Not Found",
            "message": self.message,
            "path": self.path
        }


class InternalServerError(Exception):

    def __init__(self, message, path):
        Exception.__init__(self)
        self.message = message
        self.path = path

    def payload(self):
        return {
            "timestamp": "2020-06-04T04:37:37.154+0000",
            "status": 500,
            "error": "Internal Server Error",
            "message": self.message,
            "path": self.path
        }


@app.errorhandler(BadRequest)
def handle_bad_request(error):
    response = jsonify(error.payload())
    response.status_code = 400
    return response


@app.errorhandler(NotFound)
def handle_not_found(error):
    response = jsonify(error.payload())
    response.status_code = 404
    return response


@app.errorhandler(InternalServerError)
def handle_internal_server_error(error):
    response = jsonify(error.payload())
    response.status_code = 500
    return response


@app.route('/jobs/<int:job_id>', methods=['GET'])
def get_job(job_id):
    return job(job_id)


@app.route('/jobs/<int:job_id>/runs', methods=['POST'])
def trigger_run(job_id):
    if job_id in range(1, 31):
        try:
            params = request.get_json(force=True)
        except Exception:
            raise BadRequest("Required request body is missing.", request.path)

        # Always return IN_PROGRESS
        if job_id in range(1, 11):
            return make_response(jsonify(run_builder.initialize(job_id, "SUCCEED", params)))

        if job_id in range(11, 21):
            return make_response(jsonify(run_builder.initialize(job_id, "FAIL", params)))

        if job_id in range(21, 31):
            return make_response(jsonify(run_builder.initialize(job_id, "ABORT", params)))

    elif job_id in range(31, 41):
        raise BadRequest("foo", request.path)

    elif job_id in range(41, 51):
        raise InternalServerError("foo", request.path)

    else:
        raise NotFound("foo", request.path)


@app.route('/jobs/<int:job_id>/runs/<int:run_id>', methods=['GET'])
def get_run(job_id, run_id):

    # First time return IN_PROGRESS, second time return 'final_status'
    # Get a run which is not triggered (yet) will give back 500
    # error.
    if job_id in range(1, 31):
        return make_response(jsonify(run_builder.get(job_id, run_id)))

    else:
        raise NotFound("foo", request.path)


@app.route('/jobs/<int:job_id>/runs/<int:run_id>/console', methods=['GET'])
def get_run_console(job_id, run_id):
    if job_id in range(1, 31):
        return make_response(jsonify(run_builder.console(job_id, run_id)))

    else:
        raise NotFound("foo", request.path)

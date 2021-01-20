import json
import requests
from time import sleep
from urllib.parse import urljoin

from testsuites.auth_testsuite import AuthTestSuite


class TestFreestyleJob(AuthTestSuite):

    job_api_url = "http://localhost:8881"
    # job_api_url = "http://localhost:8080"
    # job_api_url = "http://35.190.162.206"

    freestyle_job_name = "freestyle_job_name"
    test_username = "freestyle-job-user"

    def test_hello_world(self):
        job_defination_json = {
            "name": self.freestyle_job_name,
            "dockerImage": "busybox:1.33",
            "command": [
                "sh",
                "-c",
                "echo \"Hello world\""
            ]
        }

        def validate_console_log(response):
            self.assertEqual(response.text, "Hello world\n")

        self._test_skeleton(
            job_defination_json,
            validate_console_log=validate_console_log)

    def test_subtraction(self):
        job_defination_json = {
            "name": self.freestyle_job_name,
            "dockerImage": "busybox:1.33",
            "command": [
                "sh",
                "-c",
                "expr $MINUEND - $SUBTRAHEND"
            ]
        }

        def validate_console_log(response):
            self.assertEqual(response.text, "2\n")

        self._test_skeleton(
            job_defination_json,
            inputs={"MINUEND": 5, "SUBTRAHEND": 3},
            validate_console_log=validate_console_log)

    def _test_skeleton(
            self,
            job_defination_json,
            inputs={},
            validate_console_log=None):

        master_token = self.get_master_token()
        self.create_user(master_token, self.test_username, "password")
        user_token = self.get_user_token(self.test_username, "password")

        response = requests.post(
            urljoin(self.job_api_url, "/jobs"),
            headers={
                "Content-Type": "application/json",
                "Authorization": "Bearer {}".format(user_token)
            },
            json=job_defination_json)
        self.assertEqual(response.status_code, 200)
        response_body = json.loads(response.text)
        job_id = response_body["id"]
        self.assertEqual(response_body["name"], self.freestyle_job_name)
        self.assertEqual(response_body["type"], "FREESTYLE")

        response = requests.get(
            urljoin(self.job_api_url, "/jobs/{}".format(job_id)),
            headers={
                "Authorization": "Bearer {}".format(user_token)
            })
        self.assertEqual(response.status_code, 200)
        response_body = json.loads(response.text)
        self.assertEqual(response_body["name"], "freestyle_job_name")
        self.assertEqual(response_body["type"], "FREESTYLE")

        for parameter_name, input_value in inputs.items():
            response = requests.post(
                urljoin(self.job_api_url, "/jobs/{}/parameters".format(job_id)),
                headers={
                    "Content-Type": "application/json",
                    "Authorization": "Bearer {}".format(user_token)
                },
                json={
                    "name": parameter_name
                })
            self.assertEqual(response.status_code, 200)
            response_body = json.loads(response.text)
            self.assertTrue(response_body["parameters"][0]["name"] in inputs.keys())

        response = requests.post(
            urljoin(self.job_api_url, "/jobs/{}/runs".format(job_id)),
            headers={
                "Content-Type": "application/json",
                "Authorization": "Bearer {}".format(user_token)
            },
            json=inputs)
        self.assertEqual(response.status_code, 200)
        response_body = json.loads(response.text)
        run_id = response_body["id"]
        self.assertEqual(response_body["status"], "IN_PROGRESS")
        self.assertEqual(response_body["type"], "FREESTYLE")
        self.assertEqual(response_body["job"]["type"], "FREESTYLE")
        self.assertEqual(response_body["user"]["username"], self.test_username)

        while response_body["status"] == "IN_PROGRESS":
            response = requests.get(
                urljoin(self.job_api_url, "/jobs/{}/runs/{}".format(job_id, run_id)),
                headers={
                    "Authorization": "Bearer {}".format(user_token)
                })
            self.assertEqual(response.status_code, 200)
            response_body = json.loads(response.text)
            sleep(1)
        self.assertEqual(response_body["status"], "SUCCEED")
        self.assertEqual(response_body["exitCode"], 0)

        if validate_console_log:
            response = requests.get(
                urljoin(self.job_api_url, "/jobs/{}/runs/{}/console".format(job_id, run_id)),
                headers={
                    "Authorization": "Bearer {}".format(user_token)
                })
            self.assertEqual(response.status_code, 200)
            validate_console_log(response)

        requests.delete(
            urljoin(self.job_api_url, "/jobs/{}".format(job_id)),
            headers={
                "Authorization": "Bearer {}".format(user_token)
            }
        )
        self.assertEqual(response.status_code, 200)

        self.delete_user(master_token, self.test_username)

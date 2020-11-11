import json
import os
import requests
from tempfile import NamedTemporaryFile
from time import sleep
from urllib.parse import urljoin
from zipfile import ZipFile

from testsuites.auth_testsuite import AuthTestSuite


class TestGitJob(AuthTestSuite):

    job_api_url = "http://localhost:8881"
    # job_api_url = "http://34.73.0.219"

    def test_hello_world(self):

        def validate_console_log(response):
            self.assertEqual(response.text, "Hello world\n")

        self._test_skeleton(
            "hello-world",
            validate_console_log=validate_console_log)

    def test_subtraction(self):

        def validate_console_log(response):
            self.assertEqual(response.text, "2\n")

        self._test_skeleton(
            "subtraction-operation",
            inputs={"MINUEND": 5, "SUBTRAHEND": 3},
            validate_console_log=validate_console_log)

    def test_shellscript(self):

        def validate_console_log(response):
            self.assertEqual(response.text, "this.txt\n")

        def validate_results(run_result_id_run_result_map, run_result_response_zip_map):
            self.assertEqual(len(run_result_id_run_result_map), 1)
            for id, response_zip in run_result_response_zip_map.items():
                self.assertEqual(run_result_id_run_result_map[id]["type"], "plain-text")
                self.assertEqual(run_result_id_run_result_map[id]["containerPath"], "/result")

                with ZipFile(response_zip) as result_zip:
                    self.assertEqual(len(result_zip.infolist()), 1)
                    self.assertEqual(result_zip.infolist()[0].filename, "this.txt")

        self._test_skeleton(
            "shellscript",
            validate_console_log=validate_console_log,
            validate_results=validate_results)

    def test_python_pytest(self):

        def validate_console_log(response):
            self.assertTrue("test session starts" in response.text)
            self.assertTrue("PASSED" in response.text)
            self.assertTrue("generated xml file: /code/test-results/report.xml" in response.text)

        def validate_results(run_result_id_run_result_map, run_result_response_zip_map):
            self.assertEqual(len(run_result_id_run_result_map), 1)
            for id, response_zip in run_result_response_zip_map.items():
                self.assertEqual(run_result_id_run_result_map[id]["type"], "junit")
                self.assertEqual(run_result_id_run_result_map[id]["containerPath"], "/code/test-results")

                with ZipFile(response_zip) as result_zip:
                    self.assertEqual(len(result_zip.infolist()), 1)
                    self.assertEqual(result_zip.infolist()[0].filename, "report.xml")
                    with result_zip.open('report.xml') as report:
                        report_content = report.read().decode()
                        self.assertTrue("testsuite" in report_content)
                        self.assertTrue("testcase" in report_content)

        self._test_skeleton(
            "python-pytest",
            validate_console_log=validate_console_log,
            validate_results=validate_results)

    def _test_skeleton(
            self,
            project_name,
            inputs={},
            validate_console_log=None,
            validate_results=None):

        master_token = self.get_master_token()
        self.create_user(master_token, "git-job-user", "password")
        user_token = self.get_user_token("git-job-user", "password")

        response = requests.post(
            urljoin(self.job_api_url, "/jobs"),
            headers={
                "Content-Type": "application/json",
                "Authorization": "Bearer {}".format(user_token)
            },
            json={
                "name": "git_job_name",
                "remoteOrigin": "https://github.com/restfulci/restfulci-examples.git",
                "configFilepath": "{}/restfulci.yml".format(project_name)
            })
        self.assertEqual(response.status_code, 200)
        response_body = json.loads(response.text)
        job_id = response_body["id"]
        self.assertEqual(response_body["name"], "git_job_name")
        self.assertEqual(response_body["type"], "GIT")

        response = requests.get(
            urljoin(self.job_api_url, "/jobs/{}".format(job_id)),
            headers={
                "Authorization": "Bearer {}".format(user_token)
            })
        self.assertEqual(response.status_code, 200)
        response_body = json.loads(response.text)
        self.assertEqual(response_body["name"], "git_job_name")
        self.assertEqual(response_body["type"], "GIT")

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
            json=dict({"branchName": "master"}, **inputs))
        self.assertEqual(response.status_code, 200)
        response_body = json.loads(response.text)
        run_id = response_body["id"]
        self.assertEqual(response_body["status"], "IN_PROGRESS")
        self.assertEqual(response_body["branchName"], "master")
        self.assertEqual(response_body["type"], "GIT")
        self.assertEqual(response_body["job"]["type"], "GIT")

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
        if validate_results:
            run_results = response_body["runResults"]

        response = requests.get(
            urljoin(self.job_api_url, "/jobs/{}/runs/{}/configuration".format(job_id, run_id)),
            headers={
                "Authorization": "Bearer {}".format(user_token)
            })
        self.assertEqual(response.status_code, 200)
        self.assertTrue(response.text.startswith("version:"))

        if validate_console_log:
            response = requests.get(
                urljoin(self.job_api_url, "/jobs/{}/runs/{}/console".format(job_id, run_id)),
                headers={
                    "Authorization": "Bearer {}".format(user_token)
                })
            self.assertEqual(response.status_code, 200)
            validate_console_log(response)

        if validate_results:
            run_result_id_run_result_map = {}
            run_result_id_response_zip_map = {}
            for run_result in run_results:
                response = requests.get(
                    urljoin(
                        self.job_api_url,
                        "/jobs/{}/runs/{}/results/{}".format(job_id, run_id, run_result["id"])),
                    headers={
                        "Authorization": "Bearer {}".format(user_token)
                    },
                    stream=True)
                self.assertEqual(response.status_code, 200)

                # Need `delete=False` because ZipFile can only read a closed file.
                fp = NamedTemporaryFile(delete=False)
                for chunk in response.iter_content(chunk_size=128):
                    fp.write(chunk)
                fp.close()

                run_result_id_run_result_map[run_result["id"]] = run_result
                run_result_id_response_zip_map[run_result["id"]] = fp.name

            validate_results(run_result_id_run_result_map, run_result_id_response_zip_map)

            for id, response_zip in run_result_id_response_zip_map.items():
                os.remove(response_zip)

        requests.delete(
            urljoin(self.job_api_url, "/jobs/{}".format(job_id)),
            headers={
                "Authorization": "Bearer {}".format(user_token)
            }
        )
        self.assertEqual(response.status_code, 200)

        self.delete_user(master_token, "git-job-user")

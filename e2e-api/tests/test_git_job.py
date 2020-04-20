import json
import requests
from time import sleep
from unittest import TestCase
from urllib.parse import urljoin


class TestFreestyleJob(TestCase):

    master_api_url = "http://localhost:8881"

    def test_hello_world(self):

        def validate_console_log(response):
            self.assertEqual(response.text, "Hello world\n")

        self._test_skeleton(
            "hello-world",
            validate_console_log=validate_console_log)

    def test_shellscript(self):

        def validate_console_log(response):
            self.assertEqual(response.text, "this.txt\n")

        def validate_results(run_result_id_run_result_map, run_result_response_map):
            self.assertEqual(len(run_result_id_run_result_map), 1)
            for id, response in run_result_response_map.items():
                self.assertEqual(run_result_id_run_result_map[id]["type"], "plain-text")
                self.assertEqual(run_result_id_run_result_map[id]["containerPath"], "/result")
                # TODO:
                # Validate ZIP file content.
                print(id, run_result_id_run_result_map[id], response.text)

        self._test_skeleton(
            "shellscript",
            validate_console_log=validate_console_log,
            validate_results=validate_results)

    def test_python_pytest(self):

        def validate_console_log(response):
            self.assertTrue("test session starts" in response.text)
            self.assertTrue("PASSED" in response.text)
            self.assertTrue("generated xml file: /code/test-results/report.xml" in response.text)

        def validate_results(run_result_id_run_result_map, run_result_response_map):
            self.assertEqual(len(run_result_id_run_result_map), 1)
            for id, response in run_result_response_map.items():
                self.assertEqual(run_result_id_run_result_map[id]["type"], "junit")
                self.assertEqual(run_result_id_run_result_map[id]["containerPath"], "/code/test-results")
                # TODO:
                # Validate ZIP file content.

        self._test_skeleton("python-pytest", validate_console_log)

    def _test_skeleton(self, project_name, validate_console_log=None, validate_results=None):

        response = requests.post(
            urljoin(self.master_api_url, "/jobs"),
            headers={
                "Content-Type": "application/json"
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
            urljoin(self.master_api_url, "/jobs/{}".format(job_id)))
        self.assertEqual(response.status_code, 200)
        response_body = json.loads(response.text)
        self.assertEqual(response_body["name"], "git_job_name")
        self.assertEqual(response_body["type"], "GIT")

        response = requests.post(
            urljoin(self.master_api_url, "/jobs/{}/runs".format(job_id)),
            headers={
                "Content-Type": "application/json"
            },
            json={
                "branchName": "master"
            })
        self.assertEqual(response.status_code, 200)
        response_body = json.loads(response.text)
        run_id = response_body["id"]
        self.assertEqual(response_body["phase"], "IN_PROGRESS")
        self.assertEqual(response_body["branchName"], "master")
        self.assertEqual(response_body["type"], "GIT")
        self.assertEqual(response_body["job"]["type"], "GIT")

        while response_body["phase"] == "IN_PROGRESS":
            response = requests.get(
                urljoin(self.master_api_url, "/jobs/{}/runs/{}".format(job_id, run_id)),
                headers={
                    "Content-Type": "application/json"
                })
            self.assertEqual(response.status_code, 200)
            response_body = json.loads(response.text)
            sleep(1)
        self.assertEqual(response_body["phase"], "COMPLETE")
        self.assertEqual(response_body["exitCode"], 0)
        print(response_body)
        if validate_results:
            run_results = response_body["runResults"]

        response = requests.get(
            urljoin(self.master_api_url, "/jobs/{}/runs/{}/configuration".format(job_id, run_id)),
            headers={
                "Content-Type": "text/plain"
            })
        self.assertEqual(response.status_code, 200)
        self.assertTrue(response.text.startswith("version:"))

        if validate_console_log:
            response = requests.get(
                urljoin(self.master_api_url, "/jobs/{}/runs/{}/console".format(job_id, run_id)),
                headers={
                    "Content-Type": "text/plain"
                })
            self.assertEqual(response.status_code, 200)
            validate_console_log(response)

        if validate_results:
            run_result_id_run_result_map = {}
            run_result_id_response_map = {}
            for run_result in run_results:
                response = requests.get(
                    urljoin(
                        self.master_api_url,
                        "/jobs/{}/runs/{}/results/{}".format(job_id, run_id, run_result["id"])),
                    headers={
                        "Content-Type": "application/zip"
                    })
                self.assertEqual(response.status_code, 200)
                run_result_id_run_result_map[run_result["id"]] = run_result
                run_result_id_response_map[run_result["id"]] = response
            validate_results(run_result_id_run_result_map, run_result_id_response_map)

        requests.delete(
            urljoin(self.master_api_url, "/jobs/{}".format(job_id)),
        )
        self.assertEqual(response.status_code, 200)

import json
import requests
from time import sleep
from unittest import TestCase
from urllib.parse import urljoin


class TestFreestyleJob(TestCase):

    master_api_url = "http://localhost:8881"

    def test(self):
        response = requests.post(
            urljoin(self.master_api_url, "/jobs"),
            headers={
                "Content-Type": "application/json"
            },
            json={
                "name": "git_job_name",
                "remoteOrigin": "https://github.com/restfulci/restfulci-examples.git",
                "configFilepath": "hello-world/restfulci.yml"
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

        response = requests.get(
            urljoin(self.master_api_url, "/jobs/{}/runs/{}/console".format(job_id, run_id)),
            headers={
                "Content-Type": "text/plain"
            })
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.text, "Hello world\n")

        requests.delete(
            urljoin(self.master_api_url, "/jobs/{}".format(job_id)),
        )
        self.assertEqual(response.status_code, 200)

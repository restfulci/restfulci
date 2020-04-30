import json
import requests
from time import sleep
from unittest import TestCase
from urllib.parse import urljoin


class TestFreestyleJob(TestCase):

    # master_api_url = "http://localhost:8881"
    master_api_url = "http://35.227.35.5"

    def test(self):
        response = requests.post(
            urljoin(self.master_api_url, "/jobs"),
            headers={
                "Content-Type": "application/json"
            },
            json={
                "name": "freestyle_job_name",
                "dockerImage": "busybox:1.31",
                "command": [
                    "sh",
                    "-c",
                    "echo \"Hello world\""
                ]
            })
        self.assertEqual(response.status_code, 200)
        response_body = json.loads(response.text)
        job_id = response_body["id"]
        self.assertEqual(response_body["name"], "freestyle_job_name")
        self.assertEqual(response_body["type"], "FREESTYLE")

        response = requests.get(
            urljoin(self.master_api_url, "/jobs/{}".format(job_id)))
        self.assertEqual(response.status_code, 200)
        response_body = json.loads(response.text)
        self.assertEqual(response_body["name"], "freestyle_job_name")
        self.assertEqual(response_body["type"], "FREESTYLE")

        response = requests.post(
            urljoin(self.master_api_url, "/jobs/{}/runs".format(job_id)),
            headers={
                "Content-Type": "application/json"
            },
            json={})
        self.assertEqual(response.status_code, 200)
        response_body = json.loads(response.text)
        run_id = response_body["id"]
        self.assertEqual(response_body["phase"], "IN_PROGRESS")
        self.assertEqual(response_body["type"], "FREESTYLE")
        self.assertEqual(response_body["job"]["type"], "FREESTYLE")

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

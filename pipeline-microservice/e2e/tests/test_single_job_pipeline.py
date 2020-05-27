import json
import requests
from time import sleep
from unittest import TestCase
from urllib.parse import urljoin


class TestSingleJobPipeline(TestCase):

    api_url = "http://localhost:8882"

    def test(self):
        response = requests.post(
            urljoin(self.api_url, "/pipelines"),
            headers={
                "Content-Type": "application/json"
            },
            json={
                "name": "single_job_pipeline_name"
            })
        self.assertEqual(response.status_code, 200)
        response_body = json.loads(response.text)
        pipeline_id = response_body["id"]
        self.assertEqual(response_body["name"], "single_job_pipeline_name")

        response = requests.get(
            urljoin(self.api_url, "/pipelines/{}".format(pipeline_id)))
        self.assertEqual(response.status_code, 200)
        response_body = json.loads(response.text)
        self.assertEqual(response_body["name"], "single_job_pipeline_name")

        response = requests.post(
            urljoin(self.api_url, "/pipelines/{}/referred-jobs".format(pipeline_id)),
            headers={
                "Content-Type": "application/json"
            },
            json={
                "originalJobId": 1
            })
        self.assertEqual(response.status_code, 200)
        response_body = json.loads(response.text)
        referred_jobs = response_body["referredJobs"]
        self.assertEqual(len(referred_jobs), 1)
        self.assertEqual(referred_jobs[0]["originalJobId"], 1)

        response = requests.post(
            urljoin(self.api_url, "/pipelines/{}/cycles".format(pipeline_id)))
        self.assertEqual(response.status_code, 200)
        response_body = json.loads(response.text)
        cycle_id = response_body["id"]
        self.assertEqual(response_body["status"], "IN_PROGRESS")
        referred_runs = response_body["referredRuns"]
        self.assertIsNone(response_body["completeAt"])
        self.assertEqual(len(referred_runs), 1)
        self.assertEqual(referred_runs[0]["originalJobId"], 1)
        self.assertEqual(referred_runs[0]["status"], "NOT_STARTED_YET")

        while response_body["status"] == "IN_PROGRESS":
            response = requests.get(
                urljoin(self.api_url, "/pipelines/{}/cycles/{}".format(pipeline_id, cycle_id)))
            self.assertEqual(response.status_code, 200)
            response_body = json.loads(response.text)
            sleep(1)
        self.assertEqual(response_body["status"], "SUCCEED")
        self.assertIsNotNone(response_body["completeAt"])
        referred_runs = response_body["referredRuns"]
        self.assertEqual(len(referred_runs), 1)
        self.assertEqual(referred_runs[0]["status"], "SUCCEED")

        requests.delete(
            urljoin(self.api_url, "/pipelines/{}".format(pipeline_id)),
        )
        self.assertEqual(response.status_code, 200)

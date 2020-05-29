import json
import requests
from time import sleep
from unittest import TestCase
from urllib.parse import urljoin


class TestPipeline(TestCase):

    api_url = "http://localhost:8882"

    def test(self):
        response = requests.post(
            urljoin(self.api_url, "/pipelines"),
            headers={
                "Content-Type": "application/json"
            },
            json={
                "name": "pipeline_name"
            })
        self.assertEqual(response.status_code, 200)
        response_body = json.loads(response.text)
        pipeline_id = response_body["id"]
        self.assertEqual(response_body["name"], "pipeline_name")

        referred_job1_id = self._add_referred_job(pipeline_id, 1)
        referred_job2_id = self._add_referred_job(pipeline_id, 2)
        referred_job3_id = self._add_referred_job(pipeline_id, 3)
        referred_job4_id = self._add_referred_job(pipeline_id, 4)

        self._link_referred_jobs(pipeline_id, referred_job1_id, referred_job2_id)
        self._link_referred_jobs(pipeline_id, referred_job1_id, referred_job3_id)
        self._link_referred_jobs(pipeline_id, referred_job2_id, referred_job4_id)
        self._link_referred_jobs(pipeline_id, referred_job3_id, referred_job4_id)

        response = requests.get(
            urljoin(self.api_url, "/pipelines/{}".format(pipeline_id)))
        self.assertEqual(response.status_code, 200)
        response_body = json.loads(response.text)
        self.assertEqual(response_body["name"], "pipeline_name")
        referred_jobs = response_body["referredJobs"]
        for referred_job in referred_jobs:
            if referred_job["id"] == referred_job1_id:
                self.assertTrue("referredUpstreamJobs" not in referred_job)
            elif referred_job["id"] == referred_job2_id:
                self.assertEqual(len(referred_job["referredUpstreamJobs"]), 1)
                self.assertEqual(referred_job["referredUpstreamJobs"][0]["id"], referred_job1_id)
            elif referred_job["id"] == referred_job3_id:
                self.assertEqual(len(referred_job["referredUpstreamJobs"]), 1)
                self.assertEqual(referred_job["referredUpstreamJobs"][0]["id"], referred_job1_id)
            elif referred_job["id"] == referred_job4_id:
                self.assertEqual(len(referred_job["referredUpstreamJobs"]), 2)
                self.assertCountEqual(
                    [referredUpstreamJob["id"]
                        for referredUpstreamJob in referred_job["referredUpstreamJobs"]],
                    [referred_job2_id, referred_job3_id])

        response = requests.post(
            urljoin(self.api_url, "/pipelines/{}/cycles".format(pipeline_id)))
        self.assertEqual(response.status_code, 200)
        response_body = json.loads(response.text)
        cycle_id = response_body["id"]
        self.assertEqual(response_body["status"], "IN_PROGRESS")
        referred_runs = response_body["referredRuns"]
        self.assertIsNone(response_body["completeAt"])
        self.assertEqual(len(referred_runs), 4)
        self.assertCountEqual(
            [referred_run["originalJobId"] for referred_run in referred_runs],
            [1, 2, 3, 4])
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
        self.assertEqual(len(referred_runs), 4)
        for i in range(4):
            self.assertEqual(referred_runs[i]["status"], "SUCCEED")

        requests.delete(
            urljoin(self.api_url, "/pipelines/{}".format(pipeline_id)),
        )
        self.assertEqual(response.status_code, 200)

    def _add_referred_job(self, pipeline_id, original_job_id):
        response = requests.post(
            urljoin(self.api_url, "/pipelines/{}/referred-jobs".format(pipeline_id)),
            headers={
                "Content-Type": "application/json"
            },
            json={
                "originalJobId": original_job_id
            })
        self.assertEqual(response.status_code, 200)
        response_body = json.loads(response.text)
        referred_jobs = response_body["referredJobs"]
        self.assertTrue(
            original_job_id in
            [referred_job["originalJobId"] for referred_job in referred_jobs])
	    # TODO:
        # A pipeline should not have 2 referred jobs with the same `originalJobId`.
        # Otherwise we cannot get the referred job id with a return pipeline JSON.
        for referred_job in referred_jobs:
            if referred_job["originalJobId"] == original_job_id:
                return referred_job["id"]

    def _link_referred_jobs(self, pipeline_id, upstream_referred_job_id, downstream_referred_job_id):
        response = requests.put(
            urljoin(self.api_url, "/pipelines/{}/referred-jobs/{}/referred-upstream-jobs/{}".format(
                pipeline_id, downstream_referred_job_id, upstream_referred_job_id)))
        self.assertEqual(response.status_code, 200)

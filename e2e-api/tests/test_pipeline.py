import json
from random import randint
import requests
from time import sleep
from unittest import TestCase
from urllib.parse import urljoin


class TestPipeline(TestCase):

    job_api_url = None
    pipeline_api_url = None

    def _setup_docker_compose_urls(self):
        self.pipeline_api_url = "http://localhost:8882"

    def _setup_kubernetes_urls(self):
        self.job_api_url = "http://35.190.162.206"
        self.pipeline_api_url = "http://35.190.139.27"

    def test_docker_compose_succeed(self):
        self._setup_docker_compose_urls()

        # Seems cannot share it for `test_docker_compose_succeed` and
        # `test_kubernetes_succeed`, as it needs to access `self`.
        def validate_cycle_succeed(response_body):
            self.assertEqual(response_body["status"], "SUCCEED")
            self.assertIsNotNone(response_body["completeAt"])
            referred_runs = response_body["referredRuns"]
            self.assertCountEqual(
                [referred_run["status"] for referred_run in referred_runs],
                ["SUCCEED", "SUCCEED", "SUCCEED", "SUCCEED"])

        self._test(
            1, 2, 3, 4,
            validate_cycle_final_state=validate_cycle_succeed)

    def test_docker_compose_fail(self):
        self._setup_docker_compose_urls()

        def validate_cycle_fail(response_body):
            self.assertEqual(response_body["status"], "FAIL")
            referred_runs = response_body["referredRuns"]
            self.assertCountEqual(
                [referred_run["status"] for referred_run in referred_runs],
                ["FAIL", "SKIP", "SKIP", "SKIP"])

        self._test(
            11, 2, 3, 4,
            validate_cycle_final_state=validate_cycle_fail)

    def test_docker_compose_abort(self):
        self._setup_docker_compose_urls()

        def validate_cycle_abort(response_body):
            self.assertEqual(response_body["status"], "ABORT")
            referred_runs = response_body["referredRuns"]
            self.assertCountEqual(
                [referred_run["status"] for referred_run in referred_runs],
                ["ABORT", "SKIP", "SKIP", "SKIP"])

        self._test(
            21, 2, 3, 4,
            validate_cycle_final_state=validate_cycle_abort)

    def test_docker_compose_error(self):
        self._setup_docker_compose_urls()

        def validate_cycle_error(response_body):
            self.assertEqual(response_body["status"], "FAIL")
            referred_runs = response_body["referredRuns"]
            self.assertCountEqual(
                [referred_run["status"] for referred_run in referred_runs],
                ["ERROR", "SKIP", "SKIP", "SKIP"])
            for referred_run in referred_runs:
                if referred_run["status"] == "ERROR":
                    self.assertTrue("400 BAD REQUEST" in referred_run["errorMessage"])

        self._test(
            31, 2, 3, 4,
            validate_cycle_final_state=validate_cycle_error)

    def test_kubernetes_succeed(self):
        self._setup_kubernetes_urls()

        def validate_cycle_succeed(response_body):
            self.assertEqual(response_body["status"], "SUCCEED")
            self.assertIsNotNone(response_body["completeAt"])
            referred_runs = response_body["referredRuns"]
            self.assertCountEqual(
                [referred_run["status"] for referred_run in referred_runs],
                ["SUCCEED", "SUCCEED", "SUCCEED", "SUCCEED"])

        job_id_1 = self._create_job_and_return_id()
        job_id_2 = self._create_job_and_return_id()
        job_id_3 = self._create_job_and_return_id()
        job_id_4 = self._create_job_and_return_id()

        self._test(
            job_id_1, job_id_2, job_id_3, job_id_4,
            validate_cycle_final_state=validate_cycle_succeed)

        self._delete_job(job_id_1)
        self._delete_job(job_id_2)
        self._delete_job(job_id_3)
        self._delete_job(job_id_4)

    def _test(self, job_id_1, job_id_2, job_id_3, job_id_4, validate_cycle_final_state):

        response = requests.post(
            urljoin(self.pipeline_api_url, "/pipelines"),
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

        response = requests.post(
            urljoin(self.pipeline_api_url, "/pipelines/{}/parameters".format(pipeline_id)),
            headers={
                "Content-Type": "application/json"
            },
            json={
                "name": "ENV"
            })
        self.assertEqual(response.status_code, 200)
        response_body = json.loads(response.text)
        self.assertEqual(response_body["name"], "pipeline_name")
        parameters = response_body["parameters"]
        self.assertEqual(len(parameters), 1)
        self.assertEqual(parameters[0]["name"], "ENV")
        parameter_id = parameters[0]["id"]

        referred_job1_id = self._add_referred_job(pipeline_id, job_id_1)
        referred_job2_id = self._add_referred_job(pipeline_id, job_id_2)
        referred_job3_id = self._add_referred_job(pipeline_id, job_id_3)
        referred_job4_id = self._add_referred_job(pipeline_id, job_id_4)

        self._update_and_link_referred_job_parameter(pipeline_id, referred_job1_id, parameter_id)
        self._update_and_link_referred_job_parameter(pipeline_id, referred_job2_id, parameter_id)
        self._update_and_link_referred_job_parameter(pipeline_id, referred_job3_id, parameter_id)
        self._update_and_link_referred_job_parameter(pipeline_id, referred_job4_id, parameter_id)

        self._link_referred_job_dependency(pipeline_id, referred_job1_id, referred_job2_id)
        self._link_referred_job_dependency(pipeline_id, referred_job1_id, referred_job3_id)
        self._link_referred_job_dependency(pipeline_id, referred_job2_id, referred_job4_id)
        self._link_referred_job_dependency(pipeline_id, referred_job3_id, referred_job4_id)

        response = requests.get(
            urljoin(self.pipeline_api_url, "/pipelines/{}".format(pipeline_id)))
        self.assertEqual(response.status_code, 200)
        response_body = json.loads(response.text)
        self.assertEqual(response_body["name"], "pipeline_name")
        referred_jobs = response_body["referredJobs"]
        for referred_job in referred_jobs:
            parameter_maps = referred_job["parameterMaps"]
            self.assertEqual(len(parameter_maps), 1)
            self.assertEqual(parameter_maps[0]["parameter"]["id"], parameter_id)
            self.assertEqual(parameter_maps[0]["remoteName"], "ENV")

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
            urljoin(self.pipeline_api_url, "/pipelines/{}/cycles".format(pipeline_id)),
            headers={
                "Content-Type": "application/json"
            },
            json={
                "ENV": "stage"
            })
        self.assertEqual(response.status_code, 200)
        response_body = json.loads(response.text)
        cycle_id = response_body["id"]
        self.assertEqual(response_body["status"], "IN_PROGRESS")
        referred_runs = response_body["referredRuns"]
        self.assertIsNone(response_body["completeAt"])
        self.assertEqual(len(referred_runs), 4)
        self.assertCountEqual(
            [referred_run["originalJobId"] for referred_run in referred_runs],
            [job_id_1, job_id_2, job_id_3, job_id_4])
        self.assertEqual(referred_runs[0]["status"], "NOT_STARTED_YET")

        while response_body["status"] == "IN_PROGRESS":
            response = requests.get(
                urljoin(self.pipeline_api_url, "/pipelines/{}/cycles/{}".format(pipeline_id, cycle_id)))
            self.assertEqual(response.status_code, 200)
            response_body = json.loads(response.text)
            sleep(1)
        self.assertIsNotNone(response_body["completeAt"])
        validate_cycle_final_state(response_body)

        requests.delete(
            urljoin(self.pipeline_api_url, "/pipelines/{}".format(pipeline_id)),
        )
        self.assertEqual(response.status_code, 200)

    def _create_job_and_return_id(self):
        response = requests.post(
            urljoin(self.job_api_url, "/jobs"),
            headers={
                "Content-Type": "application/json"
            },
            json={
                "name": "freestyle_job_name_{}".format(randint(0, 10**10)),
                "dockerImage": "busybox:1.31",
                "command": [
                    "sh",
                    "-c",
                    "echo \"Hello world\""
                ]
            })
        response_body = json.loads(response.text)
        return response_body["id"]

    def _delete_job(self, job_id):
        requests.delete(
            urljoin(self.job_api_url, "/jobs/{}".format(job_id)),
        )

    def _add_referred_job(self, pipeline_id, original_job_id):
        response = requests.post(
            urljoin(self.pipeline_api_url, "/pipelines/{}/referred-jobs".format(pipeline_id)),
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

    def _update_and_link_referred_job_parameter(self, pipeline_id, referred_job_id, parameter_id):
        response = requests.put(
            urljoin(self.pipeline_api_url, "/pipelines/{}/referred-jobs/{}".format(pipeline_id, referred_job_id)))
        self.assertEqual(response.status_code, 200)
        response_body = json.loads(response.text)
        parameter_maps = response_body["parameterMaps"]
        self.assertEqual(len(parameter_maps), 1)
        self.assertEqual(parameter_maps[0]["remoteName"], "ENV")
        parameter_map_id = parameter_maps[0]["id"]

        response = requests.put(
            urljoin(
                self.pipeline_api_url,
                "/pipelines/{}/referred-jobs/{}/parameter-maps/{}".format(
                    pipeline_id, referred_job_id, parameter_map_id)),
            data={
                "parameterId": parameter_id
            })
        self.assertEqual(response.status_code, 200)
        response_body = json.loads(response.text)

    def _link_referred_job_dependency(self, pipeline_id, upstream_referred_job_id, downstream_referred_job_id):
        response = requests.put(
            urljoin(self.pipeline_api_url, "/pipelines/{}/referred-jobs/{}/referred-upstream-jobs/{}".format(
                pipeline_id, downstream_referred_job_id, upstream_referred_job_id)))
        self.assertEqual(response.status_code, 200)

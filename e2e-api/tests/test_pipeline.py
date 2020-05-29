import json
import requests
from unittest import TestCase
from urllib.parse import urljoin


class TestPipeline(TestCase):

    # api_url = "http://localhost:8882"
    api_url = "http://34.73.68.176"

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

        requests.delete(
            urljoin(self.api_url, "/pipelines/{}".format(pipeline_id)),
        )
        self.assertEqual(response.status_code, 200)

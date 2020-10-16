import json
import requests
from lxml import html
from time import sleep
from unittest import TestCase
from urllib.parse import urljoin


class TestLogin(TestCase):

    job_api_url = "http://localhost:8080"

    def test_job_login(self):

        response = requests.get(
            urljoin(self.job_api_url, "/jobs"))
        print(response.headers)
        cookie = response.headers["Set-Cookie"]
        # print(response.text)
        login_url = html.fromstring(response.text).xpath(
            '//form[@id="kc-form-login"]')[0].get('action')
        print(login_url)
        print(cookie)
        print("========")
        response = requests.post(
            login_url,
            headers={
                "Content-Type": "application/x-www-form-urlencoded",
                "Cookie": cookie,
            },
            data={
                "username": "test-user",
                "password": "password",
                "credentialId": ""
            })
        print(response.url)
        print(response.headers)
        print(response.text)
        print("========")
        for history in response.history:
            print(history.url)
            print(history.headers)

import json
import requests
from unittest import TestCase
from urllib.parse import urljoin


class AuthTestSuite(TestCase):

    auth_api_url = "http://localhost:8880"

    def get_master_token(self):
        response = requests.post(
            urljoin(self.auth_api_url, "/auth/realms/master/protocol/openid-connect/token"),
            headers={
                "Content-Type": "application/x-www-form-urlencoded"
            },
            data={
                "client_id": "admin-cli",
                "username": "restfulci",
                "password": "secretpassword",
                "grant_type": "password"
            })
        response_body = json.loads(response.text)
        return response_body["access_token"]

    def create_user(self, master_token, username, password):
        # Returns 409 if "{'errorMessage': 'User exists with same username'}"
        # It doesn't matter through.
        requests.post(
            urljoin(self.auth_api_url, "/auth/admin/realms/restfulci/users"),
            headers={
                "Content-Type": "application/json",
                "Authorization": "Bearer {}".format(master_token)
            },
            json={
                "enabled": "true",
                "username": username
            })

        response = requests.get(
            urljoin(self.auth_api_url, "/auth/admin/realms/restfulci/users"),
            headers={
                "Content-Type": "application/json",
                "Authorization": "Bearer {}".format(master_token)
            },
            json={
                "username": username
            })
        response_body = json.loads(response.text)
        user_id = response_body[0]["id"]

        response = requests.put(
            urljoin(self.auth_api_url, "/auth/admin/realms/restfulci/users/{}/reset-password".format(user_id)),
            headers={
                "Content-Type": "application/json",
                "Authorization": "Bearer {}".format(master_token)
            },
            json={
                "type": "password",
                "temporary": False,
                "value": password
            })
        self.assertEquals(response.status_code, 204)

    def delete_user(self, master_token, username):
        response = requests.get(
            urljoin(self.auth_api_url, "/auth/admin/realms/restfulci/users"),
            headers={
                "Content-Type": "application/json",
                "Authorization": "Bearer {}".format(master_token)
            },
            json={
                "username": username
            })
        response_body = json.loads(response.text)
        user_id = response_body[0]["id"]

        response = requests.delete(
            urljoin(self.auth_api_url, "/auth/admin/realms/restfulci/users/{}".format(user_id)),
            headers={
                "Authorization": "Bearer {}".format(master_token)
            })
        self.assertEquals(response.status_code, 204)

    def get_user_token(self, username, password):
        response = requests.post(
            urljoin(self.auth_api_url, "/auth/realms/restfulci/protocol/openid-connect/token"),
            headers={
                "Content-Type": "application/x-www-form-urlencoded"
            },
            data={
                "client_id": "job-microservice",
                "client_secret": "25c7ad47-b784-4c15-b2ed-4969f4ceb1a0",
                "username": username,
                "password": password,
                "grant_type": "password"
            })
        response_body = json.loads(response.text)
        return response_body["access_token"]

    def get_me_info(self, user_token):
        response = requests.get(
            urljoin(self.auth_api_url, "/auth/realms/restfulci/protocol/openid-connect/userinfo"),
            headers={
                "Authorization": "Bearer {}".format(user_token)
            })
        response_body = json.loads(response.text)
        return response_body

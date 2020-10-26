import json
import requests
import subprocess
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

    def get_user_token(self, username, password):
        # TODO:
        # Currently only works in local (not in docker).
        # As in docker, JWT token can only be getting from
        # `http://keycloak:8080` inside of docker,
        # otherwise 401.

        # response = requests.post(
        #     urljoin(self.auth_api_url, "/auth/realms/restfulci/protocol/openid-connect/token"),
        #     headers={
        #         "Content-Type": "application/x-www-form-urlencoded"
        #     },
        #     data={
        #         "client_id": "job-microservice",
        #         "client_secret": "28a7617f-3368-41bb-b161-ceac8047d9ea",
        #         "username": username,
        #         "password": password,
        #         "grant_type": "password"
        #     })
        # response_body = json.loads(response.text)
        # return response_body["access_token"]

        #result = subprocess.run("cd ../job-microservice && docker-compose exec master-api-server curl --help")
        # result = subprocess.run(["curl", "--help"])

        process = subprocess.Popen("""
        cd ../job-microservice &&
        ls &&
        echo "hello" &&
        docker-compose exec master-api-server curl --help
        """, stdin=subprocess.PIPE, stdout=subprocess.PIPE, shell=True)
        out, err = process.communicate()
        print(out)

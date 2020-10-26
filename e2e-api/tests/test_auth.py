import json
import requests
from urllib.parse import urljoin

from testsuites.auth_testsuite import AuthTestSuite


class TestAuth(AuthTestSuite):

    def test_auth(self):
        master_token = self.get_master_token()
        print(master_token)
        self.create_user(master_token, "test-user", "password")
        user_token = self.get_user_token("test-user", "password")
        print(user_token)

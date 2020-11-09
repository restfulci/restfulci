from testsuites.auth_testsuite import AuthTestSuite


class TestAuth(AuthTestSuite):

    def test_auth(self):
        master_token = self.get_master_token()
        self.create_user(master_token, "test-user", "password")
        user_token = self.get_user_token("test-user", "password")
        userinfo = self.get_me_info(user_token)
        self.assertEqual(userinfo["preferred_username"], "test-user")
        self.delete_user(master_token, "test-user")

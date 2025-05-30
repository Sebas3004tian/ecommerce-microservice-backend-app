import random
import string
from locust import HttpUser, task, between

class UserServiceUser(HttpUser):
    wait_time = between(1, 3)

    def on_start(self):
        self.user_ids = []
        self.base_url = "/user-service/api/users"

    def generate_random_email(self):
        return f"test_{''.join(random.choices(string.ascii_lowercase + string.digits, k=8))}@example.com"

    def generate_user_payload(self, email=None):
        return {
            "firstName": "Load",
            "lastName": "Test",
            "imageUrl": f"https://picsum.photos/200/200?random={random.randint(1, 1000)}",
            "email": email or self.generate_random_email(),
            "addressDtos": [
                {
                    "fullAddress": f"{random.randint(100, 999)} Test Street",
                    "postalCode": f"{random.randint(10000, 99999)}",
                    "city": random.choice(["New York", "Los Angeles", "Chicago", "Houston", "Phoenix"])
                }
            ],
            "credential": {
                "username": f"user_{random.randint(100000, 999999)}",
                "password": "testpass123",
                "roleBasedAuthority": "ROLE_USER",
                "isEnabled": True,
                "isAccountNonExpired": True,
                "isAccountNonLocked": True,
                "isCredentialsNonExpired": True
            }
        }

    @task(3)
    def create_user(self):
        user_data = self.generate_user_payload()
        with self.client.post(self.base_url, json=user_data, catch_response=True) as response:
            if response.status_code == 200:
                try:
                    user_id = response.json().get("userId")
                    if user_id:
                        self.user_ids.append(user_id)
                        response.success()
                    else:
                        response.failure("No userId returned")
                except Exception as e:
                    response.failure(f"JSON parse error: {str(e)}")
            else:
                response.failure(f"Create user failed with {response.status_code}")

    @task(4)
    def list_users(self):
        with self.client.get(self.base_url, catch_response=True) as response:
            if response.status_code == 200:
                if "collection" in response.json():
                    response.success()
                else:
                    response.failure("Missing 'collection' key")
            else:
                response.failure(f"List users failed with {response.status_code}")

    @task(2)
    def get_user_by_id(self):
        if self.user_ids:
            user_id = random.choice(self.user_ids)
        else:
            user_id = random.randint(1, 4)  # Usuarios precargados
        with self.client.get(f"{self.base_url}/{user_id}", catch_response=True) as response:
            if response.status_code == 200:
                if "userId" in response.json():
                    response.success()
                else:
                    response.failure("Invalid user response format")
            else:
                response.failure(f"Get user failed with {response.status_code}")

    @task(1)
    def update_user(self):
        if not self.user_ids:
            return
        user_id = random.choice(self.user_ids)
        updated_data = self.generate_user_payload(email=f"updated_{self.generate_random_email()}")
        updated_data["userId"] = user_id
        with self.client.put(f"{self.base_url}/{user_id}", json=updated_data, catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Update failed with {response.status_code}")

    @task(1)
    def delete_user(self):
        if len(self.user_ids) <= 5:
            return
        user_id = self.user_ids.pop(random.randint(0, len(self.user_ids) - 1))
        with self.client.delete(f"{self.base_url}/{user_id}", catch_response=True) as response:
            if response.status_code == 200 and response.text == "true":
                response.success()
            else:
                response.failure(f"Delete failed with {response.status_code}")

    @task(1)
    def test_invalid_user(self):
        with self.client.get(f"{self.base_url}/999999", catch_response=True) as response:
            if response.status_code == 400:
                try:
                    if "timestamp" in response.json():
                        response.success()
                    else:
                        response.failure("Expected error format missing")
                except Exception as e:
                    response.failure(f"Invalid JSON in error: {str(e)}")
            else:
                response.failure(f"Expected 400, got {response.status_code}")

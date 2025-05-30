package com.selimhorri.app.E2E;

import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.dto.CredentialDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserServiceTest {
    @Autowired
    private TestRestTemplate restTemplate;

    private UserDto buildValidUser(String email) {
        CredentialDto cred = CredentialDto.builder()
            .username("testuser")
            .password("1234")
            .isEnabled(true)
            .isAccountNonExpired(true)
            .isAccountNonLocked(true)
            .isCredentialsNonExpired(true)
            .build();
        return UserDto.builder()
            .firstName("Test")
            .lastName("User")
            .email(email)
            .credentialDto(cred)
            .build();
    }

    @Test
    void testUserRegisters() {
        UserDto user = buildValidUser("test@mail.com");
        ResponseEntity<UserDto> response = restTemplate.postForEntity("/api/users", user, UserDto.class);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }
    @Test
    void testUserDeletesAccount() {
        UserDto user = restTemplate.postForEntity("/api/users", buildValidUser("test2@mail.com"), UserDto.class).getBody();
        restTemplate.delete("/api/users/" + user.getUserId());
        assertNotNull(user);
    }
    @Test
    void testUserUpdatesEmail() {
        UserDto user = restTemplate.postForEntity("/api/users", buildValidUser("test3@mail.com"), UserDto.class).getBody();
        user.setEmail("updated@mail.com");
        restTemplate.put("/api/users/" + user.getUserId(), user);
        assertNotNull(user);
    }
} 
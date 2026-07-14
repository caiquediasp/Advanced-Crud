package com.caique.advancedcrud.auth;

import com.caique.advancedcrud.TestcontainersConfiguration;
import com.caique.advancedcrud.shared.errorlog.ErrorLogRepository;
import com.caique.advancedcrud.user.Role;
import com.caique.advancedcrud.user.RoleRepository;
import com.caique.advancedcrud.user.User;
import com.caique.advancedcrud.user.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class SecurityFlowIntegrationTest {

    private static final String PASSWORD = "password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StringRedisTemplate redis;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ErrorLogRepository errorLogRepository;

    @BeforeEach
    void cleanRedis() {
        redis.getConnectionFactory().getConnection().serverCommands().flushDb();
    }

    private String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }

    private void register(String email) throws Exception {
        String body = """
                {
                  "name": "Security Flow User",
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, PASSWORD);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    private ResultActions login(String email, String password) throws Exception {
        String body = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);

        return mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private record Tokens(String accessToken, String refreshToken) {
    }

    private Tokens loginTokens(String email) throws Exception {
        String response = login(email, PASSWORD)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return new Tokens(
                JsonPath.read(response, "$.accessToken"),
                JsonPath.read(response, "$.refreshToken"));
    }

    private Tokens registerAndLogin(String email) throws Exception {
        register(email);
        return loginTokens(email);
    }

    private void promoteToAdmin(String email) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email).orElseThrow();
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
        user.getRoles().add(adminRole);
        userRepository.save(user);
    }

    private ResultActions refresh(String refreshToken) throws Exception {
        String body = """
                {
                  "refreshToken": "%s"
                }
                """.formatted(refreshToken);

        return mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @Test
    void refresh_rotatesToken_oldTokenReuseKillsWholeFamily() throws Exception {
        String firstToken = registerAndLogin(uniqueEmail()).refreshToken();

        String secondToken = JsonPath.read(
                refresh(firstToken)
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                        .andReturn().getResponse().getContentAsString(),
                "$.refreshToken");

        refresh(firstToken).andExpect(status().isUnauthorized());

        refresh(secondToken).andExpect(status().isUnauthorized());
    }

    @Test
    void randomToken_refreshReturnsUnauthorized() throws Exception {
        refresh(UUID.randomUUID().toString()).andExpect(status().isUnauthorized());
    }

    @Test
    void changePassword_revokesExistingSessions() throws Exception {
        Tokens tokens = registerAndLogin(uniqueEmail());

        String changePasswordBody = """
                {
                  "currentPassword": "%s",
                  "newPassword": "newPassword456"
                }
                """.formatted(PASSWORD);

        mockMvc.perform(patch("/api/v1/users/me/password")
                        .header("Authorization", "Bearer " + tokens.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(changePasswordBody))
                .andExpect(status().isNoContent());

        refresh(tokens.refreshToken()).andExpect(status().isUnauthorized());
    }

    @Test
    void changeEmail_revokesExistingSessions() throws Exception {
        Tokens tokens = registerAndLogin(uniqueEmail());

        String changeEmailBody = """
                {   
                    "name" : "Security Flow Integration",
                    "email" : "changeEmail@example.com",
                    "currentPassword" : "password123"
                }
                """;

        mockMvc.perform(put("/api/v1/users/me")
                .header("Authorization", "Bearer " + tokens.accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(changeEmailBody)
        ).andExpect(status().isOk());

        refresh(tokens.refreshToken()).andExpect(status().isUnauthorized());
    }

    @Test
    void adminDisablesUser_userSessionsAreRevoked() throws Exception {
        String targetEmail = uniqueEmail();
        Tokens targetTokens = registerAndLogin(targetEmail);

        String adminEmail = uniqueEmail();
        register(adminEmail);
        promoteToAdmin(adminEmail);
        Tokens adminTokens = loginTokens(adminEmail);

        UUID targetPublicId = userRepository
                .findByEmailAndDeletedAtIsNull(targetEmail).orElseThrow()
                .getPublicId();

        mockMvc.perform(patch("/api/v1/admin/users/{publicId}/status", targetPublicId)
                        .header("Authorization", "Bearer " + adminTokens.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"enabled": false}
                                """))
                .andExpect(status().isOk());

        refresh(targetTokens.refreshToken()).andExpect(status().isUnauthorized());
    }

    @Test
    void fiveWrongPasswords_blockLoginEvenWithCorrectPassword() throws Exception {
        String email = uniqueEmail();
        register(email);

        for (int i = 0; i <= 4; i++) {
            login(email, "wrongPassword");
        }

        login(email, PASSWORD).andExpect(status().isTooManyRequests());
    }

    @Test
    void successfulLogin_doesNotResetIpCounter() throws Exception {
        String email = uniqueEmail();
        register(email);

        for (int i = 0; i < 19; i++) {
            login(uniqueEmail(), "wrongPassword").andExpect(status().isUnauthorized());
        }

        login(email, PASSWORD).andExpect(status().isOk());

        login(uniqueEmail(), "wrongPassword").andExpect(status().isUnauthorized());

        login(email, PASSWORD).andExpect(status().isTooManyRequests());
    }

    @Test
    void thirtyRefreshes_thirtyFirstReturnsTooManyRequests() throws Exception {
        for (int i = 0; i < 30; i++) {
            Tokens tokens = registerAndLogin(uniqueEmail());
            refresh(tokens.refreshToken()).andExpect(status().isOk());
        }

        Tokens tokens = registerAndLogin(uniqueEmail());
        refresh(tokens.refreshToken()).andExpect(status().isTooManyRequests());
    }

    @Test
    void multiBytePassword_registerReturnsValidationError() throws Exception {
        // 50 'ç' passes 100 bytes limit
        String body = """
                {
                  "name": "Security Flow User",
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(uniqueEmail(), "ç".repeat(50));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void invalidCepFormat_lookupReturnsBadRequestWithoutErrorLog() throws Exception {
        Tokens tokens = registerAndLogin(uniqueEmail());
        long errorLogsBefore = errorLogRepository.count();

        mockMvc.perform(get("/api/v1/addresses/lookup/{cep}", "abc")
                        .header("Authorization", "Bearer " + tokens.accessToken()))
                .andExpect(status().isBadRequest());

        Thread.sleep(1000);
        assertThat(errorLogRepository.count()).isEqualTo(errorLogsBefore);
    }

}

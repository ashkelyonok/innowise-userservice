package org.ashkelyonok.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ashkelyonok.userservice.AbstractIntegrationTest;
import org.ashkelyonok.userservice.model.dto.StatusUpdateDto;
import org.ashkelyonok.userservice.model.dto.UserCreateDto;
import org.ashkelyonok.userservice.model.dto.UserUpdateDto;
import org.ashkelyonok.userservice.repository.UserRepository;
import org.ashkelyonok.userservice.security.SecurityUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Objects;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private SecurityUtil securityUtil;

    @BeforeEach
    void setupSecurity() {
        doNothing().when(securityUtil).checkOwnership(any());
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        Objects.requireNonNull(cacheManager.getCache("userWithCards")).clear();
    }

    @Test
    @DisplayName("Create User -> Get User (Check Cache) -> Update User -> Check Cache Eviction")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testFullUserLifecycle() throws Exception {
        UserCreateDto createDto = new UserCreateDto();
        createDto.setEmail("integration@test.com");
        createDto.setName("John");
        createDto.setSurname("Doe");
        createDto.setBirthDate(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());

        Long userId = userRepository.findByEmail("integration@test.com").orElseThrow().getId();

        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("integration@test.com"));

        assertThat(cacheManager.getCache("userWithCards").get(userId)).isNotNull();

        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("Johnny");
        updateDto.setSurname("Doe");
        updateDto.setBirthDate(LocalDate.of(1990, 1, 1));

        mockMvc.perform(put("/api/v1/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/users/{id}", userId))
                .andExpect(status().isNoContent());

        assertThat(cacheManager.getCache("userWithCards").get(userId)).isNull();
    }

    @Test
    @DisplayName("Exception: Duplicate User (409 Conflict)")
    void testCreateUser_Duplicate() throws Exception {
        UserCreateDto user = new UserCreateDto();
        user.setEmail("duplicate@test.com");
        user.setName("Dougie");
        user.setSurname("Plicate");
        user.setBirthDate(LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("User Already Exists"));
    }

    @Test
    @DisplayName("Exception: Invalid Data (400 Bad Request)")
    void testCreateUser_InvalidData() throws Exception {
        UserCreateDto invalidUser = new UserCreateDto();
        invalidUser.setEmail("not-an-email");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("Exception: Type Mismatch (400 Bad Request)")
    @WithMockUser(username = "user", roles = "USER")
    void testGetUser_TypeMismatch() throws Exception {
        mockMvc.perform(get("/api/v1/users/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Parameter Type"));
    }

    @Test
    @DisplayName("Exception: User Not Found (404 Not Found)")
    @WithMockUser(username = "user", roles = "USER")
    void testGetUser_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/users/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User Not Found"));
    }

    @Test
    @DisplayName("Update User Status (PATCH): Verify new StatusUpdateDto logic")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testUpdateUserStatus() throws Exception {
        UserCreateDto createDto = new UserCreateDto();
        createDto.setEmail("status@test.com");
        createDto.setName("Status");
        createDto.setSurname("Tester");
        createDto.setBirthDate(LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)));

        Long userId = userRepository.findByEmail("status@test.com").orElseThrow().getId();

        StatusUpdateDto statusDto = new StatusUpdateDto();
        statusDto.setActive(false);

        mockMvc.perform(patch("/api/v1/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDto)))
                .andExpect(status().isNoContent());

        boolean isActive = userRepository.findById(userId).orElseThrow().getActive();
        assertThat(isActive).isFalse();
    }

    @Test
    @DisplayName("Search User: By Email (Success)")
    @WithMockUser(username = "user", roles = "USER")
    void testSearchUser_Success() throws Exception {
        UserCreateDto createDto = new UserCreateDto();
        createDto.setEmail("search.me@test.com");
        createDto.setName("Search");
        createDto.setSurname("Me");
        createDto.setBirthDate(LocalDate.of(1995, 5, 5));

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)));

        mockMvc.perform(get("/api/v1/users")
                        .param("email", "search.me@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("search.me@test.com"))
                .andExpect(jsonPath("$.content[0].name").value("Search"));
    }

    @Test
    @DisplayName("Get All Users: Filtered (Success)")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testGetAllUsers_Success() throws Exception {
        when(securityUtil.isAdmin()).thenReturn(true);
        when(securityUtil.getAuthenticatedUserId()).thenReturn(1L);

        UserCreateDto user1 = new UserCreateDto();
        user1.setEmail("admin.view1@test.com");
        user1.setName("Alice");
        user1.setSurname("Smith");
        user1.setBirthDate(LocalDate.of(1990, 1, 1));

        UserCreateDto user2 = new UserCreateDto();
        user2.setEmail("admin.view2@test.com");
        user2.setName("Bob");
        user2.setSurname("Jones");
        user2.setBirthDate(LocalDate.of(1992, 2, 2));

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1)));

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2)));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));

        mockMvc.perform(get("/api/v1/users")
                        .param("name", "Alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].email").value("admin.view1@test.com"));
    }
}

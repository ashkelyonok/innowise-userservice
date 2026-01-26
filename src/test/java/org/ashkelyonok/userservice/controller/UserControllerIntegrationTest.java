package org.ashkelyonok.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ashkelyonok.userservice.AbstractIntegrationTest;
import org.ashkelyonok.userservice.model.dto.StatusUpdateDto;
import org.ashkelyonok.userservice.model.dto.UserCreateDto;
import org.ashkelyonok.userservice.model.dto.UserUpdateDto;
import org.ashkelyonok.userservice.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Objects;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        Objects.requireNonNull(cacheManager.getCache("userWithCards")).clear();
    }

    @Test
    @DisplayName("Create User -> Get User (Check Cache) -> Update User -> Check Cache Eviction")
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
    @DisplayName("Update User Status (PATCH): Verify new StatusUpdateDto logic")
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
}

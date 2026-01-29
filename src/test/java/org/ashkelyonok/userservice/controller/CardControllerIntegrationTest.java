package org.ashkelyonok.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ashkelyonok.userservice.AbstractIntegrationTest;
import org.ashkelyonok.userservice.model.dto.CardCreateDto;
import org.ashkelyonok.userservice.model.dto.CardUpdateDto;
import org.ashkelyonok.userservice.model.dto.UserCreateDto;
import org.ashkelyonok.userservice.repository.CardRepository;
import org.ashkelyonok.userservice.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Objects;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class CardControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CacheManager cacheManager;

    private long testUserId;

    @BeforeEach
    void setUp() throws Exception {
        UserCreateDto userDto = new UserCreateDto();
        userDto.setEmail("card-owner@test.com");
        userDto.setName("Card");
        userDto.setSurname("Owner");
        userDto.setBirthDate(LocalDate.of(1995, 5, 5));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated());

        this.testUserId = userRepository.findByEmail("card-owner@test.com").orElseThrow().getId();
    }

    @AfterEach
    void tearDown() {
        cardRepository.deleteAll();
        userRepository.deleteAll();
        if (cacheManager.getCache("userWithCards") != null) {
            Objects.requireNonNull(cacheManager.getCache("userWithCards")).clear();
        }
    }

    @Test
    @DisplayName("Create Card -> Check User Cache Evicted -> Get Card -> Update Card")
    void testCardLifecycle() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", testUserId))
                .andExpect(status().isOk());
        assertThat(cacheManager.getCache("userWithCards").get(testUserId)).isNotNull();

        CardCreateDto cardDto = new CardCreateDto();
        cardDto.setUserId(testUserId);
        cardDto.setNumber("1234567812345678");
        cardDto.setExpirationDate("12/24");

        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());

        assertThat(cacheManager.getCache("userWithCards").get(testUserId)).isNull();

        Long cardId = cardRepository.findAllByUserId(testUserId).get(0).getId();

        CardUpdateDto updateDto = new CardUpdateDto();
        updateDto.setExpirationDate("11/29");

        mockMvc.perform(put("/api/v1/cards/{id}", cardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expirationDate").value("11/29"));

        assertThat(cacheManager.getCache("userWithCards").get(testUserId)).isNull();
    }

    @Test
    @DisplayName("Max Limit: Cannot create more than 5 cards")
    void testMaxCardsLimit() throws Exception {
        for (int i = 0; i < 5; i++) {
            CardCreateDto dto = new CardCreateDto();
            dto.setUserId(testUserId);
            dto.setNumber("123456781234500" + i);
            dto.setExpirationDate("12/30");

            mockMvc.perform(post("/api/v1/cards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated());
        }

        CardCreateDto failDto = new CardCreateDto();
        failDto.setUserId(testUserId);
        failDto.setNumber("9999999999999999");
        failDto.setExpirationDate("12/30");

        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(failDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Get All Cards with Filters")
    void testGetAllCards_Filtered() throws Exception {
        CardCreateDto cardDto = new CardCreateDto();
        cardDto.setUserId(testUserId);
        cardDto.setNumber("1111222233334444");
        cardDto.setExpirationDate("01/25");

        mockMvc.perform(post("/api/v1/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardDto)));

        mockMvc.perform(get("/api/v1/cards")
                        .param("number", "1111222233334444"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        mockMvc.perform(get("/api/v1/cards")
                        .param("number", "0000000000000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }
}

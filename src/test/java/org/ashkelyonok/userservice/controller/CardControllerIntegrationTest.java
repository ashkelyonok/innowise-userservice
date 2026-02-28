package org.ashkelyonok.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ashkelyonok.userservice.AbstractIntegrationTest;
import org.ashkelyonok.userservice.model.dto.CardCreateDto;
import org.ashkelyonok.userservice.model.dto.CardUpdateDto;
import org.ashkelyonok.userservice.model.dto.StatusUpdateDto;
import org.ashkelyonok.userservice.model.dto.UserCreateDto;
import org.ashkelyonok.userservice.repository.CardRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import java.time.LocalDate;
import java.util.Objects;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
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

    @MockitoBean
    private SecurityUtil securityUtil;

    private long testUserId;

    @BeforeEach
    void setUp() throws Exception {
        UserCreateDto userDto = new UserCreateDto();
        userDto.setEmail("card-owner@test.com");
        userDto.setName("Card");
        userDto.setSurname("Owner");
        userDto.setBirthDate(LocalDate.of(1995, 5, 5));

        doNothing().when(securityUtil).checkOwnership(any());

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
    @WithMockUser(username = "user", roles = "USER")
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
    @WithMockUser(username = "user", roles = "USER")
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
    @WithMockUser(username = "admin", roles = "ADMIN")
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

    @Test
    @DisplayName("Get Card Operations: By ID and By User ID")
    @WithMockUser(username = "user", roles = "USER")
    void testGetCardOperations_Success() throws Exception {
        CardCreateDto cardDto = new CardCreateDto();
        cardDto.setUserId(testUserId);
        cardDto.setNumber("5555666677778888");
        cardDto.setExpirationDate("05/28");

        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDto)))
                .andExpect(status().isCreated());

        Long cardId = cardRepository.findAllByUserId(testUserId).get(0).getId();

        mockMvc.perform(get("/api/v1/cards/{id}", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("5555666677778888"));

        mockMvc.perform(get("/api/v1/cards/user/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].number").value("5555666677778888"));
    }

    @Test
    @DisplayName("State Change & Deletion: Update Active Status and Delete Card")
    @WithMockUser(username = "user", roles = "USER")
    void testCardStateAndDeletion_Success() throws Exception {
        CardCreateDto cardDto = new CardCreateDto();
        cardDto.setUserId(testUserId);
        cardDto.setNumber("9999888877776666");
        cardDto.setExpirationDate("09/29");

        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDto)))
                .andExpect(status().isCreated());

        Long cardId = cardRepository.findAllByUserId(testUserId).get(0).getId();

        StatusUpdateDto statusDto = new StatusUpdateDto();
        statusDto.setActive(false);

        mockMvc.perform(patch("/api/v1/cards/{id}", cardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDto)))
                .andExpect(status().isNoContent());

        assertThat(cardRepository.findById(cardId).orElseThrow().getActive()).isFalse();

        mockMvc.perform(delete("/api/v1/cards/{id}", cardId))
                .andExpect(status().isNoContent());

        assertThat(cardRepository.findById(cardId)).isEmpty();
    }

    @Test
    @DisplayName("Exception: Card Not Found (404)")
    @WithMockUser(username = "user", roles = "USER")
    void testGetCard_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/cards/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Card Not Found"));
    }

    @Test
    @DisplayName("Exception: User Not Found during Card Creation (404)")
    @WithMockUser(username = "user", roles = "USER")
    void testCreateCard_UserNotFound() throws Exception {
        CardCreateDto cardDto = new CardCreateDto();
        cardDto.setUserId(99999L);
        cardDto.setNumber("1111111111111111");
        cardDto.setExpirationDate("01/30");

        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User Not Found"));
    }
}

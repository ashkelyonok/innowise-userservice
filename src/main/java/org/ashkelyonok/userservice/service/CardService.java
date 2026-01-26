package org.ashkelyonok.userservice.service;

import org.ashkelyonok.userservice.model.dto.CardCreateDto;
import org.ashkelyonok.userservice.model.dto.CardResponseDto;
import org.ashkelyonok.userservice.model.dto.CardUpdateDto;
import org.ashkelyonok.userservice.model.dto.PageResponseDto;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * Service interface for managing Payment Cards.
 */
public interface CardService {

    /**
     * Issues a new card for a user.
     * @param cardDto card creation data
     * @return the created card
     * @throws org.ashkelyonok.userservice.exception.MaxCardsLimitException if user has 5 or more cards
     */
    CardResponseDto createCard(CardCreateDto cardDto);

    /**
     * Retrieves card details by ID.
     * @param id card ID
     * @return card details
     */
    CardResponseDto getCardById(Long id);

    /**
     * Retrieves all cards owned by a specific user.
     * @param userId user ID
     * @return list of cards
     */
    List<CardResponseDto> getCardsByUserId(Long userId);

    /**
     * Retrieves a paginated list of cards with optional filtering.
     * @param number partial card number
     * @param holder partial holder name
     * @param active filter by status
     * @param pageable pagination info
     * @return page of cards
     */
    PageResponseDto<CardResponseDto> getAllCards(String number, String holder, Boolean active, Pageable pageable);

    /**
     * Activates or deactivates a card.
     * @param id card ID
     * @param active new status
     */
    void updateActiveStatus(Long id, boolean active);

    /**
     * Updates card details (holder name, expiration).
     * @param id card ID
     * @param dto update data
     * @return updated card
     */
    CardResponseDto updateCard(Long id, CardUpdateDto dto);

    /**
     * Permanently deletes a card.
     * @param id card ID
     */
    void deleteCard(Long id);
}

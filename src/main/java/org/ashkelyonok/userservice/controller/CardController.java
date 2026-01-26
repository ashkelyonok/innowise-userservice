package org.ashkelyonok.userservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ashkelyonok.userservice.controller.api.CardControllerApi;
import org.ashkelyonok.userservice.model.dto.CardCreateDto;
import org.ashkelyonok.userservice.model.dto.CardResponseDto;
import org.ashkelyonok.userservice.model.dto.CardUpdateDto;
import org.ashkelyonok.userservice.model.dto.PageResponseDto;
import org.ashkelyonok.userservice.model.dto.StatusUpdateDto;
import org.ashkelyonok.userservice.service.CardService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CardController implements CardControllerApi {

    private final CardService cardService;

    @Override
    public ResponseEntity<CardResponseDto> createCard(CardCreateDto cardDto) {
        log.info("Request to issue card for User ID: {}", cardDto.getUserId());
        CardResponseDto createdCard = cardService.createCard(cardDto);
        return new ResponseEntity<>(createdCard, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<CardResponseDto> getCardById(Long id) {
        log.debug("Fetching card with ID: {}", id);
        return ResponseEntity.ok(cardService.getCardById(id));
    }

    @Override
    public ResponseEntity<List<CardResponseDto>> getCardsByUserId(Long userId) {
        log.debug("Fetching cards for User ID: {}", userId);
        return ResponseEntity.ok(cardService.getCardsByUserId(userId));
    }

    @Override
    public ResponseEntity<PageResponseDto<CardResponseDto>> getAllCards(
            String number, String holder, Boolean active, @PageableDefault(size = 20) Pageable pageable) {
        log.debug("Fetching all cards with filter - holder: {}, active: {}", holder, active);
        return ResponseEntity.ok(cardService.getAllCards(number, holder, active, pageable));
    }

    @Override
    public ResponseEntity<CardResponseDto> updateCard(Long id, CardUpdateDto cardDto) {
        log.info("Updating card details for ID: {}", id);
        return ResponseEntity.ok(cardService.updateCard(id, cardDto));
    }

    @Override
    public ResponseEntity<Void> updateActiveStatus(Long id, StatusUpdateDto statusDto) {
        log.info("Setting active status to {} for Card ID: {}", statusDto.getActive(), id);
        cardService.updateActiveStatus(id, statusDto.getActive());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> deleteCard(Long id) {
        log.info("Deleting card with ID: {}", id);
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}

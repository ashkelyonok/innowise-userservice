package org.ashkelyonok.userservice.controller;

import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cards")
public class CardController implements CardControllerApi {

    private final CardService cardService;

    @Override
    @PostMapping
    public ResponseEntity<CardResponseDto> createCard(@Valid @RequestBody CardCreateDto cardDto) {
        log.info("Request to issue card for User ID: {}", cardDto.getUserId());
        CardResponseDto createdCard = cardService.createCard(cardDto);
        return new ResponseEntity<>(createdCard, HttpStatus.CREATED);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<CardResponseDto> getCardById(@PathVariable Long id) {
        log.debug("Fetching card with ID: {}", id);
        return ResponseEntity.ok(cardService.getCardById(id));
    }

    @Override
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CardResponseDto>> getCardsByUserId(@PathVariable Long userId) {
        log.debug("Fetching cards for User ID: {}", userId);
        return ResponseEntity.ok(cardService.getCardsByUserId(userId));
    }

    @Override
    @GetMapping
    public ResponseEntity<PageResponseDto<CardResponseDto>> getAllCards(
            @RequestParam(required = false) String number,
            @RequestParam(required = false) String holder,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("Fetching all cards with filter - holder: {}, active: {}", holder, active);
        return ResponseEntity.ok(cardService.getAllCards(number, holder, active, pageable));
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<CardResponseDto> updateCard(
            @PathVariable Long id,
            @Valid @RequestBody CardUpdateDto cardDto) {
        log.info("Updating card details for ID: {}", id);
        return ResponseEntity.ok(cardService.updateCard(id, cardDto));
    }

    @Override
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateActiveStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateDto statusDto) {
        log.info("Setting active status to {} for Card ID: {}", statusDto.getActive(), id);
        cardService.updateActiveStatus(id, statusDto.getActive());
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        log.info("Deleting card with ID: {}", id);
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}

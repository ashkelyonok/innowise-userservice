package org.ashkelyonok.userservice.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.ashkelyonok.userservice.model.dto.CardCreateDto;
import org.ashkelyonok.userservice.model.dto.CardResponseDto;
import org.ashkelyonok.userservice.model.dto.CardUpdateDto;
import org.ashkelyonok.userservice.model.dto.PageResponseDto;
import org.ashkelyonok.userservice.model.dto.StatusUpdateDto;
import org.springframework.data.domain.Pageable;
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

import java.util.List;

@RequestMapping("/api/v1/cards")
@Tag(name = "Cards", description = "Operations related to Payment Cards")
public interface CardControllerApi {

    @Operation(summary = "Issue a new card", description = "Creates a card for a specific user (User ID must be in the body).")
    @ApiResponse(responseCode = "201", description = "Card issued successfully")
    @ApiResponse(responseCode = "400", description = "Invalid data or Limit Exceeded (Max 5 cards)")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PostMapping
    ResponseEntity<CardResponseDto> createCard(@Valid @RequestBody CardCreateDto cardDto);

    @Operation(summary = "Get card by ID", description = "Returns details of a specific card.")
    @GetMapping("/{id}")
    ResponseEntity<CardResponseDto> getCardById(@PathVariable Long id);

    @Operation(summary = "Get user's cards", description = "Returns all cards belonging to a specific user.")
    @GetMapping("/user/{userId}")
    ResponseEntity<List<CardResponseDto>> getCardsByUserId(@PathVariable Long userId);

    @Operation(summary = "Search all cards", description = "Admin endpoint to filter cards by number, holder, or status.")
    @GetMapping
    ResponseEntity<PageResponseDto<CardResponseDto>> getAllCards(
            @Parameter(description = "Filter by card number") @RequestParam(required = false) String number,
            @Parameter(description = "Filter by holder name") @RequestParam(required = false) String holder,
            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean active,
            @Parameter(hidden = true) Pageable pageable);

    @Operation(summary = "Update card details", description = "Updates card holder name or expiration date.")
    @PutMapping("/{id}")
    ResponseEntity<CardResponseDto> updateCard(
            @PathVariable Long id,
            @Valid @RequestBody CardUpdateDto cardDto);

    @Operation(summary = "Change card status", description = "Activates or Deactivates a specific card.")
    @PatchMapping("/{id}")
    ResponseEntity<Void> updateActiveStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateDto statusDto);

    @Operation(summary = "Delete card", description = "Permanently removes a payment card.")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteCard(@PathVariable Long id);
}

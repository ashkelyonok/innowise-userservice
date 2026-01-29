package org.ashkelyonok.userservice.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ashkelyonok.userservice.model.dto.CardCreateDto;
import org.ashkelyonok.userservice.model.dto.CardResponseDto;
import org.ashkelyonok.userservice.model.dto.CardUpdateDto;
import org.ashkelyonok.userservice.model.dto.PageResponseDto;
import org.ashkelyonok.userservice.model.dto.StatusUpdateDto;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Cards", description = "Operations related to Payment Cards")
public interface CardControllerApi {

    @Operation(summary = "Issue a new card", description = "Creates a card for a specific user (User ID must be in the body).")
    @ApiResponse(responseCode = "201", description = "Card issued successfully")
    @ApiResponse(responseCode = "400", description = "Invalid data or Limit Exceeded (Max 5 cards)")
    @ApiResponse(responseCode = "404", description = "User not found")
    ResponseEntity<CardResponseDto> createCard(CardCreateDto cardDto);

    @Operation(summary = "Get card by ID", description = "Returns details of a specific card.")
    ResponseEntity<CardResponseDto> getCardById(Long id);

    @Operation(summary = "Get user's cards", description = "Returns all cards belonging to a specific user.")
    ResponseEntity<List<CardResponseDto>> getCardsByUserId(Long userId);

    @Operation(summary = "Search all cards", description = "Admin endpoint to filter cards by number, holder, or status.")
    ResponseEntity<PageResponseDto<CardResponseDto>> getAllCards(
            @Parameter(description = "Filter by card number") String number,
            @Parameter(description = "Filter by holder name") String holder,
            @Parameter(description = "Filter by active status") Boolean active,
            @Parameter(hidden = true) Pageable pageable);

    @Operation(summary = "Update card details", description = "Updates card holder name or expiration date.")
    ResponseEntity<CardResponseDto> updateCard(Long id, CardUpdateDto cardDto);

    @Operation(summary = "Change card status", description = "Activates or Deactivates a specific card.")
    ResponseEntity<Void> updateActiveStatus(Long id, StatusUpdateDto statusDto);

    @Operation(summary = "Delete card", description = "Permanently removes a payment card.")
    ResponseEntity<Void> deleteCard(Long id);
}

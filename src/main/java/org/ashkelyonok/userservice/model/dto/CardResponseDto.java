package org.ashkelyonok.userservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO for transferring card data")
public class CardResponseDto {

    @Schema(description = "Unique card identifier",
            example = "10",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "16-digit card number", example = "1234567812345678")
    private String number;

    @Schema(description = "Card holder name", example = "ANASTASIA SHKELYONOK")
    private String holder;

    @Schema(description = "Card expiration date in MM/YY format", example = "07/28")
    private String expirationDate;

    @Schema(description = "Card active status", example = "true")
    private Boolean active;

    @Schema(description = "ID of the user who owns the card", example = "1")
    private Long userId;

    @Schema(description = "Record creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Record last update timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}

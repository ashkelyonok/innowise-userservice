package org.ashkelyonok.userservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "DTO for creating card")
public class CardCreateDto {

    @Schema(description = "16-digit card number",
            example = "1234567812345678",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "\\d{16}", message = "Card number must be exactly 16 digits")
    private String number;

    @Schema(description = "Card expiration date in MM/YY format",
            example = "07/28",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Expiration date is required")
    @Pattern(
            regexp = "^(0[1-9]|1[0-2])/(\\d{2})$",
            message = "Expiration date must be in MM/YY format")
    private String expirationDate;

    @Schema(description = "ID of the user who owns the card",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "User ID is required")
    private Long userId;
}

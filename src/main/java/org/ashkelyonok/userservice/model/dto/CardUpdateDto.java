package org.ashkelyonok.userservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Fields allowed for card details update")
public class CardUpdateDto {

    @Schema(description = "Card holder name",
            example = "ANASTASIA SHKELYONOK")
    @Size(max = 50)
    private String holder;

    @Schema(description = "Expiration date (MM/YY)", example = "07/28")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/(\\d{2})$",
            message = "Format must be MM/YY")
    private String expirationDate;
}

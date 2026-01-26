package org.ashkelyonok.userservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "DTO for transferring full user data (including cards)")
public class UserWithCardsResponseDto extends UserResponseDto {

    @Schema(description = "List of user's payment cards")
    private List<CardResponseDto> cards;
}

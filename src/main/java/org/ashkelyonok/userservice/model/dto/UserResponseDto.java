package org.ashkelyonok.userservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@Schema(description = "DTO for transferring user data (without cards)")
public class UserResponseDto {

    @Schema(description = "Unique user identifier",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "User's first name",
            example = "Anastasia")
    private String name;

    @Schema(description = "User's surname",
            example = "Shkelyonok")
    private String surname;

    @Schema(description = "User's birth date",
            example = "1995-03-25")
    private LocalDate birthDate;

    @Schema(description = "User's email",
            example = "example@gmail.com")
    private String email;

    @Schema(description = "Account active status",
            example = "true")
    private Boolean active;

    @Schema(description = "Record creation timestamp",
            accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Record last update timestamp",
            accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}

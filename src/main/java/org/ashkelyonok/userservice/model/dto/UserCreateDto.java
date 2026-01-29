package org.ashkelyonok.userservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "DTO for registering a new user")
public class UserCreateDto {

    @Schema(description = "User's first name",
            example = "Anastasia",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Name cannot be empty")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @Schema(description = "User's surname",
            example = "Shkelyonok",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Surname cannot be empty")
    @Size(min = 2, max = 50, message = "Surname must be between 2 and 50 characters")
    private String surname;

    @Schema(description = "User's birth date",
            example = "1995-03-25",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Birth date is required")
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    @Schema(description = "User's email",
            example = "example@gmail.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
}

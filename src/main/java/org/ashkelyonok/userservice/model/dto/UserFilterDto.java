package org.ashkelyonok.userservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for filtering users via query parameters")
public class UserFilterDto {

    @Schema(description = "Filter by specific User IDs", example = "1,2,3")
    private Set<Long> ids;

    @Schema(description = "Exact match by email", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Filter by name (partial match)", example = "John")
    private String name;

    @Schema(description = "Filter by surname (partial match)", example = "Doe")
    private String surname;
}

package org.ashkelyonok.userservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Standard Paginated Response Wrapper")
public class PageResponseDto<T> {

    @Schema(description = "List of items on the current page")
    private List<T> content;

    @Schema(description = "Current page number (0-indexed)", example = "0")
    private int pageNumber;

    @Schema(description = "Number of items per page", example = "10")
    private int pageSize;

    @Schema(description = "Total items across all pages", example = "50")
    private long totalElements;

    @Schema(description = "Total number of available pages", example = "5")
    private int totalPages;

    @Schema(description = "Is this the last page?", example = "false")
    private boolean last;
}
package org.ashkelyonok.userservice.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.ashkelyonok.userservice.model.dto.PageResponseDto;
import org.ashkelyonok.userservice.model.dto.StatusUpdateDto;
import org.ashkelyonok.userservice.model.dto.UserCreateDto;
import org.ashkelyonok.userservice.model.dto.UserResponseDto;
import org.ashkelyonok.userservice.model.dto.UserUpdateDto;
import org.ashkelyonok.userservice.model.dto.UserWithCardsResponseDto;
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

@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Operations related to User Management")
public interface UserControllerApi {

    @Operation(summary = "Create a new user", description = "Registers a new user in the system.")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "409", description = "User with this email already exists")
    @PostMapping
    ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserCreateDto userDto);

    @Operation(summary = "Get user profile", description = "Returns full user details including their cards.")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/{id}")
    ResponseEntity<UserWithCardsResponseDto> getUserById(@PathVariable Long id);

    @Operation(summary = "Get all users", description = "Returns a paginated list of users (Light DTOs). Useful for Admin panels.")
    @GetMapping
    ResponseEntity<PageResponseDto<UserResponseDto>> getAllUsers(
            @Parameter(description = "Filter by name") @RequestParam(required = false) String name,
            @Parameter(description = "Filter by surname") @RequestParam(required = false) String surname,
            @Parameter(hidden = true) Pageable pageable);

    @Operation(summary = "Update user profile", description = "Updates allowed fields (Name, Surname, Birthdate). Email cannot be changed here.")
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PutMapping("/{id}")
    ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDto userDto);

    @Operation(summary = "Change user status", description = "Activates or Deactivates a user account.")
    @PatchMapping("/{id}")
    ResponseEntity<Void> updateActiveStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateDto statusDto);

    @Operation(summary = "Delete user", description = "Permanently removes a user and their associated cards.")
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteUser(@PathVariable Long id);
}

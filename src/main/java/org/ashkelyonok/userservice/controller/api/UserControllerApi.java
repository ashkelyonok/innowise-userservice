package org.ashkelyonok.userservice.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ashkelyonok.userservice.model.dto.PageResponseDto;
import org.ashkelyonok.userservice.model.dto.StatusUpdateDto;
import org.ashkelyonok.userservice.model.dto.UserCreateDto;
import org.ashkelyonok.userservice.model.dto.UserResponseDto;
import org.ashkelyonok.userservice.model.dto.UserUpdateDto;
import org.ashkelyonok.userservice.model.dto.UserWithCardsResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "Users", description = "Operations related to User Management")
public interface UserControllerApi {

    @Operation(summary = "Create a new user", description = "Registers a new user in the system.")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "409", description = "User with this email already exists")
    ResponseEntity<UserResponseDto> createUser(UserCreateDto userDto);

    @Operation(summary = "Get user profile", description = "Returns full user details including their cards.")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    ResponseEntity<UserWithCardsResponseDto> getUserById(Long id);

    @Operation(summary = "Search user", description = "Finds a user by specific criteria (currently supports email).")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    ResponseEntity<UserResponseDto> searchUser(
            @Parameter(description = "Email address to search for", required = true) String email);

    @Operation(summary = "Get all users", description = "Returns a paginated list of users (Light DTOs). Useful for Admin panels.")
    ResponseEntity<PageResponseDto<UserResponseDto>> getAllUsers(
            @Parameter(description = "Filter by name") String name,
            @Parameter(description = "Filter by surname") String surname,
            @Parameter(hidden = true) Pageable pageable);

    @Operation(summary = "Update user profile", description = "Updates allowed fields (Name, Surname, Birthdate). Email cannot be changed here.")
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    ResponseEntity<UserResponseDto> updateUser(Long id, UserUpdateDto userDto);

    @Operation(summary = "Change user status", description = "Activates or Deactivates a user account.")
    ResponseEntity<Void> updateActiveStatus(Long id, StatusUpdateDto statusDto);

    @Operation(summary = "Delete user", description = "Permanently removes a user and their associated cards.")
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    ResponseEntity<Void> deleteUser(Long id);
}

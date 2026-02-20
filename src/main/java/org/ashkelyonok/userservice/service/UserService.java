package org.ashkelyonok.userservice.service;

import org.ashkelyonok.userservice.model.dto.PageResponseDto;
import org.ashkelyonok.userservice.model.dto.UserCreateDto;
import org.ashkelyonok.userservice.model.dto.UserFilterDto;
import org.ashkelyonok.userservice.model.dto.UserResponseDto;
import org.ashkelyonok.userservice.model.dto.UserUpdateDto;
import org.ashkelyonok.userservice.model.dto.UserWithCardsResponseDto;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for managing Users.
 */
public interface UserService {

    /**
     * Registers a new user.
     * @param userDto the user registration data
     * @return the created user
     * @throws org.ashkelyonok.userservice.exception.UserAlreadyExistsException if email is taken
     */
    UserResponseDto createUser(UserCreateDto userDto);

    /**
     * Retrieves user details including their cards.
     * @param id user ID
     * @return user details with card list
     */
    UserWithCardsResponseDto getUserById(Long id);

    /**
     * Retrieves a paginated list of users based on filter criteria.
     * Serves as a unified RESTful retrieval method supporting exact email lookups,
     * batch ID fetching, and administrative wild-card searches.
     *
     * @param filter   DTO containing optional search parameters (ids, email, name, surname)
     * @param pageable Pagination configuration
     * @return A paginated wrapper containing the matching UserResponseDto objects
     * @throws org.springframework.security.access.AccessDeniedException if a non-admin attempts broad filtering
     */
    PageResponseDto<UserResponseDto> getAllUsers(UserFilterDto filter, Pageable pageable);

    /**
     * Updates user's personal info. Email cannot be changed.
     * @param id user ID
     * @param userDto update data
     * @return updated user
     */
    UserResponseDto updateUser(Long id, UserUpdateDto userDto);

    /**
     * Activates or deactivates a user.
     * @param id user ID
     * @param active new status
     */
    void updateActiveStatus(Long id, boolean active);

    /**
     * Permanently deletes a user and all associated cards.
     * @param id user ID
     */
    void deleteUser(Long id);
}
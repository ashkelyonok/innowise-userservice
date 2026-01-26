package org.ashkelyonok.userservice.service;

import org.ashkelyonok.userservice.model.dto.PageResponseDto;
import org.ashkelyonok.userservice.model.dto.UserCreateDto;
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
     * Finds a user by email.
     * @param email the email to search for
     * @return user details
     */
    UserResponseDto getUserByEmail(String email);

    /**
     * Retrieves a paginated list of users with optional filtering.
     * @param name filter by first name (partial match)
     * @param surname filter by surname (partial match)
     * @param pageable pagination info
     * @return page of users
     */
    PageResponseDto<UserResponseDto> getAllUsers(String name, String surname, Pageable pageable);

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
package org.ashkelyonok.userservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ashkelyonok.userservice.controller.api.UserControllerApi;
import org.ashkelyonok.userservice.model.dto.PageResponseDto;
import org.ashkelyonok.userservice.model.dto.StatusUpdateDto;
import org.ashkelyonok.userservice.model.dto.UserCreateDto;
import org.ashkelyonok.userservice.model.dto.UserResponseDto;
import org.ashkelyonok.userservice.model.dto.UserUpdateDto;
import org.ashkelyonok.userservice.model.dto.UserWithCardsResponseDto;
import org.ashkelyonok.userservice.service.UserService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController implements UserControllerApi {

    private final UserService userService;

    @Override
    public ResponseEntity<UserResponseDto> createUser(UserCreateDto userDto) {
        log.info("Received request to create user: {}", userDto.getEmail());
        UserResponseDto createdUser = userService.createUser(userDto);
        log.info("User created successfully with ID: {}", createdUser.getId());
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<UserWithCardsResponseDto> getUserById(Long id) {
        log.debug("Fetching user with ID: {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Override
    public ResponseEntity<PageResponseDto<UserResponseDto>> getAllUsers(
            String name, String surname, @PageableDefault(size = 20) Pageable pageable) {
        log.debug("Fetching users page with filter - name: {}, surname: {}", name, surname);
        return ResponseEntity.ok(userService.getAllUsers(name, surname, pageable));
    }

    @Override
    public ResponseEntity<UserResponseDto> updateUser(Long id, UserUpdateDto userDto) {
        log.info("Updating user with ID: {}", id);
        return ResponseEntity.ok(userService.updateUser(id, userDto));
    }

    @Override
    public ResponseEntity<Void> updateActiveStatus(Long id, StatusUpdateDto statusDto) {
        log.info("Setting active status to {} for User ID: {}", statusDto.getActive(), id);
        userService.updateActiveStatus(id, statusDto.getActive());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
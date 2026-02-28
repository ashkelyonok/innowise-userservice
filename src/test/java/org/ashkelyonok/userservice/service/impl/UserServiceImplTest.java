package org.ashkelyonok.userservice.service.impl;

import org.ashkelyonok.userservice.exception.UserAlreadyExistsException;
import org.ashkelyonok.userservice.exception.UserNotFoundException;
import org.ashkelyonok.userservice.mapper.UserMapper;
import org.ashkelyonok.userservice.model.dto.PageResponseDto;
import org.ashkelyonok.userservice.model.dto.UserCreateDto;
import org.ashkelyonok.userservice.model.dto.UserFilterDto;
import org.ashkelyonok.userservice.model.dto.UserResponseDto;
import org.ashkelyonok.userservice.model.dto.UserUpdateDto;
import org.ashkelyonok.userservice.model.dto.UserWithCardsResponseDto;
import org.ashkelyonok.userservice.model.entity.User;
import org.ashkelyonok.userservice.repository.UserRepository;
import org.ashkelyonok.userservice.security.SecurityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private CacheManager cacheManager;
    @Mock private Cache cache;
    @Mock private SecurityUtil securityUtil;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Create User: Success")
    void createUser_Success() {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmail("test@test.com");

        User userEntity = new User();
        User savedUser = new User();
        savedUser.setId(1L);
        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(1L);

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userMapper.toEntity(dto)).thenReturn(userEntity);
        when(userRepository.save(userEntity)).thenReturn(savedUser);
        when(userMapper.toResponseDto(savedUser)).thenReturn(responseDto);

        UserResponseDto result = userService.createUser(dto);

        assertThat(result.getId()).isEqualTo(1L);
        verify(userRepository).save(userEntity);
    }

    @Test
    @DisplayName("Create User: Throws Exception if Email Exists")
    void createUser_UserAlreadyExists() {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmail("existing@test.com");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Get User By ID: Success")
    void getUserById_Success() {
        Long id = 1L;
        User user = new User();
        UserWithCardsResponseDto responseDto = new UserWithCardsResponseDto();

        doNothing().when(securityUtil).checkOwnership(id);
        when(userRepository.findByIdWithCards(id)).thenReturn(Optional.of(user));
        when(userMapper.toWithCardsResponseDto(user)).thenReturn(responseDto);

        UserWithCardsResponseDto result = userService.getUserById(id);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Get User By ID: Throws Not Found")
    void getUserById_NotFound() {
        Long id = 99L;
        when(userRepository.findByIdWithCards(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(id))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Get All Users: Admin Can Search By Name")
    void getAllUsers_AdminSearchByName() {
        Pageable pageable = Pageable.unpaged();
        UserFilterDto filter = UserFilterDto.builder().name("John").build();
        User user = new User();
        user.setId(1L);
        Page<User> page = new PageImpl<>(List.of(user));

        when(securityUtil.isAdmin()).thenReturn(true);
        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(userMapper.toResponseDto(user)).thenReturn(new UserResponseDto());

        PageResponseDto<UserResponseDto> result = userService.getAllUsers(filter, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(securityUtil, never()).checkOwnership(any());
    }

    @Test
    @DisplayName("Get All Users: User Can Search Self By Email")
    void getAllUsers_UserSearchSelfByEmail() {
        Pageable pageable = Pageable.unpaged();
        UserFilterDto filter = UserFilterDto.builder().email("test@test.com").build();
        User user = new User();
        user.setId(1L);
        Page<User> page = new PageImpl<>(List.of(user));

        when(securityUtil.isAdmin()).thenReturn(false);
        when(securityUtil.getAuthenticatedUserId()).thenReturn(1L);
        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        doNothing().when(securityUtil).checkOwnership(1L);
        when(userMapper.toResponseDto(user)).thenReturn(new UserResponseDto());

        PageResponseDto<UserResponseDto> result = userService.getAllUsers(filter, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(securityUtil).checkOwnership(1L);
    }

    @Test
    @DisplayName("Get All Users: User Cannot Perform Broad Search")
    void getAllUsers_UserBroadSearchDenied() {
        Pageable pageable = Pageable.unpaged();
        UserFilterDto filter = UserFilterDto.builder().name("John").build();

        when(securityUtil.isAdmin()).thenReturn(false);
        when(securityUtil.getAuthenticatedUserId()).thenReturn(1L);

        assertThatThrownBy(() -> userService.getAllUsers(filter, pageable))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
                .hasMessageContaining("Access Denied");
    }

    @Test
    @DisplayName("Update User: Success")
    void updateUser_Success() {
        Long id = 1L;
        UserUpdateDto updateDto = new UserUpdateDto();
        User existingUser = new User();
        User savedUser = new User();

        doNothing().when(securityUtil).checkOwnership(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(savedUser);
        when(userMapper.toResponseDto(savedUser)).thenReturn(new UserResponseDto());

        userService.updateUser(id, updateDto);

        verify(userMapper).updateUserFromDto(updateDto, existingUser);
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("Update User: Not Found")
    void updateUser_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        UserUpdateDto dto = new UserUpdateDto();

        assertThatThrownBy(() -> userService.updateUser(1L, dto))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Update Active Status: Success")
    void updateActiveStatus_Success() {
        Long id = 1L;
        User user = new User();
        user.setId(id);
        user.setEmail("test@email.com");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(cacheManager.getCache(anyString())).thenReturn(cache);

        userService.updateActiveStatus(id, false);

        verify(userRepository).updateActiveStatus(id, false);
        verify(cacheManager).getCache("userWithCards");
        verify(cacheManager).getCache("usersByEmail");
    }

    @Test
    @DisplayName("Update Active Status: Not Found")
    void updateActiveStatus_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateActiveStatus(1L, true))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Delete User: Success")
    void deleteUser_Success() {
        Long id = 1L;
        User user = new User();
        user.setId(id);
        user.setEmail("test@email.com");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(cacheManager.getCache(anyString())).thenReturn(cache);

        userService.deleteUser(id);

        verify(userRepository).deleteById(id);
        verify(cacheManager).getCache("userWithCards");
    }

    @Test
    @DisplayName("Delete User: Not Found")
    void deleteUser_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Delete User: Null Cache and Null Email")
    void deleteUser_CacheEviction_EdgeCases() {
        Long id1 = 1L;
        User userNoEmail = new User();
        userNoEmail.setId(id1);
        userNoEmail.setEmail(null);

        when(userRepository.findById(id1)).thenReturn(Optional.of(userNoEmail));
        when(cacheManager.getCache("userWithCards")).thenReturn(null);

        userService.deleteUser(id1);

        verify(userRepository).deleteById(id1);
        verify(cacheManager, never()).getCache("usersByEmail");

        Long id2 = 2L;
        User userWithEmail = new User();
        userWithEmail.setId(id2);
        userWithEmail.setEmail("test@test.com");

        when(userRepository.findById(id2)).thenReturn(Optional.of(userWithEmail));
        when(cacheManager.getCache("userWithCards")).thenReturn(cache);
        when(cacheManager.getCache("usersByEmail")).thenReturn(null);

        userService.deleteUser(id2);

        verify(cache).evict(id2);
        verify(cacheManager).getCache("usersByEmail");
    }
}
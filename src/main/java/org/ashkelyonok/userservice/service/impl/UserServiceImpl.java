package org.ashkelyonok.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ashkelyonok.userservice.exception.UserAlreadyExistsException;
import org.ashkelyonok.userservice.exception.UserNotFoundException;
import org.ashkelyonok.userservice.mapper.UserMapper;
import org.ashkelyonok.userservice.model.dto.PageResponseDto;
import org.ashkelyonok.userservice.model.dto.UserCreateDto;
import org.ashkelyonok.userservice.model.dto.UserResponseDto;
import org.ashkelyonok.userservice.model.dto.UserUpdateDto;
import org.ashkelyonok.userservice.model.dto.UserWithCardsResponseDto;
import org.ashkelyonok.userservice.model.entity.User;
import org.ashkelyonok.userservice.repository.UserRepository;
import org.ashkelyonok.userservice.repository.spec.UserSpecification;
import org.ashkelyonok.userservice.service.UserService;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CacheManager cacheManager;

    @Override
    public UserResponseDto createUser(UserCreateDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new UserAlreadyExistsException("User with email: " + userDto.getEmail() + " already exists");
        }

        User user = userMapper.toEntity(userDto);
        User savedUser = userRepository.save(user);

        log.info("Created user with id: {}", savedUser.getId());
        return userMapper.toResponseDto(savedUser);
    }

    @Override
    @Transactional(readOnly=true)
    @Cacheable(value = "userWithCards", key = "#id")
    public UserWithCardsResponseDto getUserById(Long id) {
        return userRepository.findByIdWithCards(id)
                .map(userMapper::toWithCardsResponseDto)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    @Transactional(readOnly=true)
    @Cacheable(value = "usersByEmail", key = "#email")
    public UserResponseDto getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toResponseDto)
                .orElseThrow(()-> new UserNotFoundException("email", email));
    }

    @Override
    @Transactional(readOnly=true)
    public PageResponseDto<UserResponseDto> getAllUsers(String name, String surname, Pageable pageable) {
        Specification<User> spec = UserSpecification.filterByNameAndSurname(name, surname);
        Page<User> page = userRepository.findAll(spec, pageable);

        return PageResponseDto.<UserResponseDto>builder()
                .content(page.getContent().stream()
                        .map(userMapper::toResponseDto)
                        .toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "userWithCards", key = "#id"),
            @CacheEvict(value = "usersByEmail", key = "#result.email")
    })
    public UserResponseDto updateUser(Long id, UserUpdateDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        userMapper.updateUserFromDto(userDto, user);
        User savedUser = userRepository.save(user);
        log.info("User updated: {}", id);

        return userMapper.toResponseDto(savedUser);
    }

    @Override
    public void updateActiveStatus(Long id, boolean active) {
        log.info("Setting active status to {} for user id: {}", active, id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        userRepository.updateActiveStatus(id, active);
        evictUserCaches(id, user.getEmail());
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        userRepository.deleteById(id);
        log.info("User deleted: {}", id);

        evictUserCaches(id, user.getEmail());
    }

    private void evictUserCaches(Long id, String email) {
        if (cacheManager == null) return;

        var idCache = cacheManager.getCache("userWithCards");
        if (idCache != null) {
            idCache.evict(id);
        }

        if (email != null) {
            var emailCache = cacheManager.getCache("usersByEmail");
            if (emailCache != null) {
                emailCache.evict(email);
            }
        }
    }
}

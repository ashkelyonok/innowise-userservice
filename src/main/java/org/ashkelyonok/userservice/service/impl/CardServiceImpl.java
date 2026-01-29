package org.ashkelyonok.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ashkelyonok.userservice.exception.CardNotFoundException;
import org.ashkelyonok.userservice.exception.MaxCardsLimitException;
import org.ashkelyonok.userservice.exception.UserNotFoundException;
import org.ashkelyonok.userservice.mapper.CardMapper;
import org.ashkelyonok.userservice.model.dto.CardCreateDto;
import org.ashkelyonok.userservice.model.dto.CardResponseDto;
import org.ashkelyonok.userservice.model.dto.CardUpdateDto;
import org.ashkelyonok.userservice.model.dto.PageResponseDto;
import org.ashkelyonok.userservice.model.entity.Card;
import org.ashkelyonok.userservice.model.entity.User;
import org.ashkelyonok.userservice.repository.CardRepository;
import org.ashkelyonok.userservice.repository.UserRepository;
import org.ashkelyonok.userservice.repository.spec.CardSpecification;
import org.ashkelyonok.userservice.service.CardService;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;

    private final CacheManager cacheManager;

    @Override
    @Caching(evict = {
            @CacheEvict(value = "userWithCards", key = "#cardDto.userId"),
            @CacheEvict(value = "userCards", key = "#cardDto.userId")
    })
    public CardResponseDto createCard(CardCreateDto cardDto) {
        Long userId = cardDto.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        int currentCount = cardRepository.countByUserId(userId);
        if (currentCount >= 5) {
            throw new MaxCardsLimitException();
        }

        Card card = cardMapper.toEntity(cardDto);
        card.setUser(user);

        String holderName = (user.getName() + " " + user.getSurname()).toUpperCase();
        card.setHolder(holderName);

        Card savedCard = cardRepository.save(card);
        log.info("Card created with id: {}", savedCard.getId());

        return cardMapper.toDto(savedCard);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "cards", key = "#id")
    public CardResponseDto getCardById(Long id) {
        return cardRepository.findByIdWithUser(id)
                .map(cardMapper::toDto)
                .orElseThrow(() -> new CardNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userCards", key = "#userId")
    public List<CardResponseDto> getCardsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        return cardRepository.findAllByUserId(userId).stream()
                .map(cardMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<CardResponseDto> getAllCards(String number, String holder, Boolean active, Pageable pageable) {
        Specification<Card> spec = Specification.where(null);

        if (number != null) {
            spec = spec.and(CardSpecification.filterByNumber(number));
        }
        if (holder != null) {
            spec = spec.and(CardSpecification.filterByHolder(holder));
        }
        if (active != null) {
            spec = spec.and(CardSpecification.filterByActive(active));
        }

        Page<Card> page = cardRepository.findAll(spec, pageable);

        return PageResponseDto.<CardResponseDto>builder()
                .content(page.getContent().stream().map(cardMapper::toDto).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    public void updateActiveStatus(Long id, boolean active) {
        log.info("Setting active status to {} for card id: {}", active, id);
        Long userId = cardRepository.findUserIdByCardId(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        cardRepository.updateActiveStatus(id, active);

        evictCardCaches(id, userId);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "cards", key = "#id"),
            @CacheEvict(value = "userWithCards", key = "#result.userId"),
            @CacheEvict(value = "userCards", key = "#result.userId")
    })
    public CardResponseDto updateCard(Long id, CardUpdateDto dto) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        cardMapper.updateCardFromDto(dto, card);
        log.info("Card updated: {}", id);

        return cardMapper.toDto(cardRepository.save(card));
    }

    @Override
    public void deleteCard(Long id) {
        Long userId = cardRepository.findUserIdByCardId(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        cardRepository.deleteById(id);
        log.info("Card deleted: {}", id);

        evictCardCaches(id, userId);
    }

    private void evictCardCaches(Long cardId, Long userId) {
        if (cacheManager == null) return;

        var cardCache = cacheManager.getCache("cards");
        if (cardCache != null) cardCache.evict(cardId);

        if (userId != null) {
            var userWithCards = cacheManager.getCache("userWithCards");
            if (userWithCards != null) userWithCards.evict(userId);

            var userCards = cacheManager.getCache("userCards");
            if (userCards != null) userCards.evict(userId);
        }
    }
}

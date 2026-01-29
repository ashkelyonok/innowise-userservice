package org.ashkelyonok.userservice.service.impl;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock private CardRepository cardRepository;
    @Mock private UserRepository userRepository;
    @Mock private CardMapper cardMapper;
    @Mock private CacheManager cacheManager;
    @Mock private Cache cache;

    @InjectMocks
    private CardServiceImpl cardService;

    @Test
    @DisplayName("Create Card: Success")
    void createCard_Success() {
        Long userId = 1L;
        CardCreateDto dto = new CardCreateDto();
        dto.setUserId(userId);
        User user = new User();
        user.setName("A");
        user.setSurname("B");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardRepository.countByUserId(userId)).thenReturn(0);
        when(cardMapper.toEntity(dto)).thenReturn(new Card());
        when(cardRepository.save(any(Card.class))).thenReturn(new Card());
        when(cardMapper.toDto(any())).thenReturn(new CardResponseDto());

        CardResponseDto result = cardService.createCard(dto);

        assertThat(result).isNotNull();
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("Create Card: Fails if User Not Found")
    void createCard_UserNotFound() {
        Long userId = 1L;
        CardCreateDto dto = new CardCreateDto();
        dto.setUserId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.createCard(dto))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Create Card: Fails if Max Limit Reached")
    void createCard_MaxLimitReached() {
        Long userId = 1L;
        CardCreateDto dto = new CardCreateDto();
        dto.setUserId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(cardRepository.countByUserId(userId)).thenReturn(5);

        assertThatThrownBy(() -> cardService.createCard(dto))
                .isInstanceOf(MaxCardsLimitException.class);
    }

    @Test
    @DisplayName("Get Card By ID: Success")
    void getCardById_Success() {
        Long id = 1L;
        when(cardRepository.findByIdWithUser(id)).thenReturn(Optional.of(new Card()));
        when(cardMapper.toDto(any())).thenReturn(new CardResponseDto());

        cardService.getCardById(id);

        verify(cardRepository).findByIdWithUser(id);
    }

    @Test
    @DisplayName("Get Card By ID: Not Found")
    void getCardById_NotFound() {
        when(cardRepository.findByIdWithUser(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cardService.getCardById(1L))
                .isInstanceOf(CardNotFoundException.class);
    }

    @Test
    @DisplayName("Get Cards By User ID: Success")
    void getCardsByUserId_Success() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        when(cardRepository.findAllByUserId(userId)).thenReturn(List.of(new Card()));

        List<CardResponseDto> result = cardService.getCardsByUserId(userId);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Get Cards By User ID: User Not Found")
    void getCardsByUserId_UserNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);
        assertThatThrownBy(() -> cardService.getCardsByUserId(1L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Get All Cards: Success with Paging")
    void getAllCards_Success() {
        Pageable pageable = Pageable.unpaged();
        Page<Card> page = new PageImpl<>(List.of(new Card()));

        when(cardRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(cardMapper.toDto(any())).thenReturn(new CardResponseDto());

        PageResponseDto<CardResponseDto> result = cardService.getAllCards(null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Get All Cards: With Filters")
    void getAllCards_WithFilters() {
        Pageable pageable = Pageable.unpaged();
        Page<Card> page = new PageImpl<>(List.of(new Card()));

        when(cardRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(cardMapper.toDto(any())).thenReturn(new CardResponseDto());

        cardService.getAllCards("1234", null, null, pageable);
        cardService.getAllCards(null, "John", null, pageable);
        cardService.getAllCards(null, null, true, pageable);
        cardService.getAllCards("1234", "John", true, pageable);

        verify(cardRepository, times(4))
                .findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Update Active Status: Success")
    void updateActiveStatus_Success() {
        Long id = 1L;
        Long userId = 100L;

        when(cardRepository.findUserIdByCardId(id)).thenReturn(Optional.of(userId));
        when(cacheManager.getCache(anyString())).thenReturn(cache);

        cardService.updateActiveStatus(id, true);

        verify(cardRepository).findUserIdByCardId(id);
        verify(cardRepository).updateActiveStatus(id, true);
        verify(cacheManager).getCache("cards");
    }

    @Test
    @DisplayName("Update Active Status: Not Found")
    void updateActiveStatus_NotFound() {
        when(cardRepository.findUserIdByCardId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.updateActiveStatus(1L, true))
                .isInstanceOf(CardNotFoundException.class);
    }

    @Test
    @DisplayName("Update Card: Success")
    void updateCard_Success() {
        Long cardId = 1L;
        CardUpdateDto dto = new CardUpdateDto();
        Card card = new Card();
        card.setId(cardId);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);
        when(cardMapper.toDto(any())).thenReturn(new CardResponseDto());

        cardService.updateCard(cardId, dto);

        verify(cardMapper).updateCardFromDto(dto, card);
        verify(cardRepository).save(card);
    }

    @Test
    @DisplayName("Update Card: Not Found")
    void updateCard_NotFound() {
        Long cardId = 1L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        CardUpdateDto dto = new CardUpdateDto();

        assertThatThrownBy(() -> cardService.updateCard(cardId, dto))
                .isInstanceOf(CardNotFoundException.class);
    }

    @Test
    @DisplayName("Delete Card: Success & Manual Cache Eviction")
    void deleteCard_Success() {
        Long cardId = 1L;
        Long userId = 100L;

        when(cardRepository.findUserIdByCardId(cardId)).thenReturn(Optional.of(userId));
        when(cacheManager.getCache(anyString())).thenReturn(cache);

        cardService.deleteCard(cardId);

        verify(cardRepository).deleteById(cardId);

        verify(cache, times(2)).evict(userId);
        verify(cache).evict(cardId);
    }

    @Test
    @DisplayName("Delete Card: Not Found")
    void deleteCard_NotFound() {
        when(cardRepository.findUserIdByCardId(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> cardService.deleteCard(1L))
                .isInstanceOf(CardNotFoundException.class);
    }

    @Test
    @DisplayName("Delete Card: Handles Null Cache Gracefully")
    void deleteCard_NullCache_SafeGuard() {
        Long cardId = 1L;
        Long userId = 100L;

        when(cardRepository.findUserIdByCardId(cardId)).thenReturn(Optional.of(userId));
        when(cacheManager.getCache(anyString())).thenReturn(null);

        cardService.deleteCard(cardId);

        verify(cardRepository).deleteById(cardId);
    }
}
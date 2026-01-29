package org.ashkelyonok.userservice.repository;

import org.ashkelyonok.userservice.model.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {

    int countByUserId(Long userId);

    List<Card> findAllByUserId(Long userId);

    @Query("SELECT c FROM Card c JOIN FETCH c.user WHERE c.id = :id")
    Optional<Card> findByIdWithUser(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Card c SET c.active = :active, c.updatedAt = CURRENT_TIMESTAMP WHERE c.id = :id")
    void updateActiveStatus(@Param("id") Long id, @Param("active") boolean active);

    @Query("SELECT c.user.id FROM Card c WHERE c.id = :id")
    Optional<Long> findUserIdByCardId(@Param("id") Long id);

    Page<Card> findAll(Pageable pageable);
}

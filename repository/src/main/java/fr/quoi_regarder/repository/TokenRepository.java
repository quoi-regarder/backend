package fr.quoi_regarder.repository;

import fr.quoi_regarder.commons.enums.TokenType;
import fr.quoi_regarder.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    @Modifying
    @Transactional
    void deleteByTypeAndExpiresAtBefore(TokenType type, Date expiresAt);

    Optional<Token> findTokenByTypeAndExpiresAtAfterAndToken(TokenType type, Date expiresAt, String token);
}
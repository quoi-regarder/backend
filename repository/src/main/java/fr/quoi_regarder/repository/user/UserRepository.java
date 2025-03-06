package fr.quoi_regarder.repository.user;

import fr.quoi_regarder.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findUserByEmail(String email);

    Optional<User> findUserById(UUID id);

    boolean existsByEmail(String email);

    @Modifying
    @Transactional
    @Query(
            value = "DELETE FROM users WHERE id IN (" +
                    "SELECT user_id FROM tokens WHERE expires_at <= :expiryDate AND type = :tokenType" +
                    " )" +
                    " AND is_email_verified = false",
            nativeQuery = true
    )
    void deleteAllByTokenTypeAndExpiryDateBefore(
            @Param("tokenType") String tokenType,
            @Param("expiryDate") Date expiryDate
    );
}
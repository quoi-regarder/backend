package fr.quoi_regarder.repository.user;

import fr.quoi_regarder.entity.user.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    boolean existsByUsername(String username);

    boolean existsByUsernameAndUserIdNot(String username, UUID userId);

    Optional<Profile> findProfileByUserId(UUID userId);
}
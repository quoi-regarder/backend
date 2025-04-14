package fr.quoi_regarder.repository;

import fr.quoi_regarder.entity.ViewingDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ViewingDetailsRepository extends JpaRepository<ViewingDetails, Long> {
    Optional<ViewingDetails> findByUserIdAndContextId(UUID userId, Long contextId);

    @Modifying
    void deleteByUserIdAndContextId(UUID userId, Long contextId);
}
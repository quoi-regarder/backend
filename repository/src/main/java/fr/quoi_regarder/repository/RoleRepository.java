package fr.quoi_regarder.repository;

import fr.quoi_regarder.commons.enums.RoleType;
import fr.quoi_regarder.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleType name);
}
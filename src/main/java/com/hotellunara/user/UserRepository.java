package com.hotellunara.user;

import com.hotellunara.common.enums.UserRole;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByActivoTrue();

    long countByRoleAndActivoTrue(UserRole role);

    List<User> findByRole(UserRole role);

    Page<User> findAllByOrderByNombreAscApellidoAsc(Pageable pageable);
}

package com.hotellunara.audit;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findAllByOrderByTimestampDesc();

    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);
}

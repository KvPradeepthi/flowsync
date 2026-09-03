package com.flowsync.repository;

import com.flowsync.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, Long entityId);
    List<AuditLog> findByUserEmailOrderByTimestampDesc(String userEmail);
    List<AuditLog> findAllByOrderByTimestampDesc();
}

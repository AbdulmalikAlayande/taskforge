package app.bola.taskforge.repository;

import app.bola.taskforge.domain.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
}

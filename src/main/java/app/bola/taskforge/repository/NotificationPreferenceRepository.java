package app.bola.taskforge.repository;

import app.bola.taskforge.domain.entity.NotificationPreference;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface NotificationPreferenceRepository extends TenantAwareRepository<NotificationPreference, String> {
	
	@Query("""
    SELECT p FROM #{#entityName} p WHERE p.member.id = :userId
    AND p.member.deleted = false
    AND p.member.organization.publicId = :#{T(app.bola.taskforge.domain.context.TenantContext).getCurrentTenant()}
    """)
	Optional<NotificationPreference> findByUserId(String userId);
	
}
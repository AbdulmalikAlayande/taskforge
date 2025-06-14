
package app.bola.taskforge.repository;

import app.bola.taskforge.domain.entity.Organization;

import java.util.List;
import java.util.Optional;

public interface OrganizationRepository extends TenantAwareRepository<Organization, String> {
	
	boolean existsByName(String name);
	
}

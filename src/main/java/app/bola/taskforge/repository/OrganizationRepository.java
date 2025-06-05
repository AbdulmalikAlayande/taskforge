
package app.bola.taskforge.repository;

import app.bola.taskforge.domain.entity.Organization;

public interface OrganizationRepository extends TenantAwareRepository<Organization, String> {
	
	boolean existsByName(String name);
}

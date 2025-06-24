
package app.bola.taskforge.repository;

import app.bola.taskforge.domain.entity.Organization;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrganizationRepository extends TenantAwareRepository<Organization, String> {
	
	boolean existsByName(String name);
	
	@Override
	@Query("SELECT o FROM Organization o WHERE o.publicId = :id")
	Optional<Organization> findByIdScoped(@Param("id") String id);
	
	@Override
	@Query("SELECT o FROM Organization o WHERE o.deleted = false")
	List<Organization> findAllScoped();
	
	@Override
	@Query("SELECT o FROM Organization o WHERE o.publicId IN :ids AND o.deleted = false")
	List<Organization> findAllByIdScoped(@Param("ids") List<String> ids);
	
	@Override
	@Modifying
	@Query("UPDATE Organization o SET o.deleted = true WHERE o.publicId = :id")
	void deleteByIdScopedInternal(@Param("id") String id, @Param("tenantId") String tenantId);
}

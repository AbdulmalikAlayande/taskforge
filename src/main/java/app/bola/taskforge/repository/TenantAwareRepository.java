package app.bola.taskforge.repository;

import app.bola.taskforge.domain.context.TenantContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface TenantAwareRepository<T, ID> extends JpaRepository<T, ID> {
	
	@Query("SELECT e FROM #{#entityName} e WHERE e.publicId = :id AND e.organization.publicId = :#{T(app.bola.taskforge.domain.context.TenantContext).getCurrentTenant()} AND e.deleted = false")
	Optional<T> findByIdScoped(@Param("id") ID id);
	
	@Query("SELECT e FROM #{#entityName} e WHERE e.organization.publicId = :#{T(app.bola.taskforge.domain.context.TenantContext).getCurrentTenant()} AND e.deleted = false")
	List<T> findAllScoped();
	
	@Query("SELECT e FROM #{#entityName} e WHERE e.publicId IN :ids AND e.organization.publicId = :#{T(app.bola.taskforge.domain.context.TenantContext).getCurrentTenant()} AND e.deleted = false")
	List<T> findAllByIdScoped(@Param("ids") List<ID> ids);
	
	@Modifying
	@Query("UPDATE #{#entityName} e SET e.deleted = true WHERE e.publicId = :id AND e.organization.publicId = :tenantId")
	void deleteByIdScopedInternal(@Param("id") ID id, @Param("tenantId") String tenantId);
	
	default void deleteByIdScoped(@Param("id") ID id) {
		deleteByIdScopedInternal(id, TenantContext.getCurrentTenant());
	}
}

package app.bola.taskforge.repository;

import app.bola.taskforge.common.entity.BaseEntity;
import app.bola.taskforge.domain.context.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public class TenantAwareRepositoryImplementation<T extends BaseEntity, ID extends Serializable>
												extends SimpleJpaRepository<T, ID>
												implements TenantAwareRepository<T, ID> {
	
	private final EntityManager entityManager;
	private final JpaEntityInformation<T, ?> entityInformation;
	
	public TenantAwareRepositoryImplementation(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
		super(entityInformation, entityManager);
		this.entityInformation = entityInformation;
		this.entityManager = entityManager;
	}
	
	@Override
	public Optional<T> findByIdScoped(ID id) {
		
		var criteriaBuilder = entityManager.getCriteriaBuilder();
		var query = criteriaBuilder.createQuery(entityInformation.getJavaType());
		var root = query.from(entityInformation.getJavaType());
		
		query.select(root)
			 .where(criteriaBuilder.and(
				 criteriaBuilder.equal(root.get("public_id"), id),
				 criteriaBuilder.equal(root.get("tenant").get("public_id"), TenantContext.getCurrentTenant())
			 ));
		
		try {
			return Optional.of(entityManager.createQuery(query).getSingleResult());
		}catch (NoResultException exception){
			return Optional.empty();
		}
	}
	
	@Override
	public List<T> findAllScoped() {
		var criteriaBuilder = entityManager.getCriteriaBuilder();
		var query = criteriaBuilder.createQuery(entityInformation.getJavaType());
		var root = query.from(entityInformation.getJavaType());
		
		query.select(root)
			 .where(criteriaBuilder.equal(root.get("tenant").get("id"), TenantContext.getCurrentTenant()));
		
		return entityManager.createQuery(query).getResultList();
	}
	
	@Override
	public void deleteByIdScoped(ID id) {
		this.findByIdScoped(id).ifPresent(entity -> {
			entity.setDeleted(true);
			entityManager.merge(entity);
		});
	}
}

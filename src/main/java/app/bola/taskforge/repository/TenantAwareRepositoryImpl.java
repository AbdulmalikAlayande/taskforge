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

public class TenantAwareRepositoryImpl<T extends BaseEntity, ID extends Serializable>
												extends SimpleJpaRepository<T, ID>
												implements TenantAwareRepository<T, ID> {

	private final EntityManager entityManager;
	private final JpaEntityInformation<T, ?> entityInformation;

	public TenantAwareRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
		super(entityInformation, entityManager);
		this.entityInformation = entityInformation;
		this.entityManager = entityManager;
	}

	@Override
	public Optional<T> findByIdScoped(ID id) {

		var criteriaBuilder = entityManager.getCriteriaBuilder();
		var query = criteriaBuilder.createQuery(entityInformation.getJavaType());
		var root = query.from(entityInformation.getJavaType());

		if (hasOrganizationField()) {
			query.select(root)
				 .where(criteriaBuilder.and(
					 criteriaBuilder.equal(root.get("publicId"), id),
					 criteriaBuilder.equal(root.get("organization").get("publicId"), TenantContext.getCurrentTenant())
				 ));
			return entityManager.createQuery(query).getResultStream().findFirst();
		}

		query.select(root)
			 .where(criteriaBuilder.equal(root.get("public_id"), id));

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

		if (hasOrganizationField()) {
			query.select(root)
					.where(criteriaBuilder.equal(root.get("organization").get("publicId"), TenantContext.getCurrentTenant()));
		} else {
			query.select(root);
		}

		return entityManager.createQuery(query).getResultList();
	}

	@Override
	public void deleteByIdScopedInternal(ID id, String tenantId) {
		var criteriaBuilder = entityManager.getCriteriaBuilder();
		var query = criteriaBuilder.createQuery(entityInformation.getJavaType());
		var root = query.from(entityInformation.getJavaType());

		query.select(root)
			 .where(criteriaBuilder.and(
				 criteriaBuilder.equal(root.get("publicId"), id),
				 criteriaBuilder.equal(root.get("organization").get("publicId"), tenantId)
			 ));

		entityManager.createQuery(query).getResultStream().findFirst().ifPresent(entity -> {
			entity.setDeleted(true);
			entityManager.merge(entity);
		});
	}

	@Override
	public void deleteByIdScoped(ID id) {
		deleteByIdScopedInternal(id, TenantContext.getCurrentTenant());
	}

	@Override
	public List<T> findAllByIdScoped(List<ID> ids) {
		var criteriaBuilder = entityManager.getCriteriaBuilder();
		var query = criteriaBuilder.createQuery(entityInformation.getJavaType());
		var root = query.from(entityInformation.getJavaType());

		if (hasOrganizationField()) {
			query.select(root)
					.where(criteriaBuilder.and(
							root.get("publicId").in(ids),
							criteriaBuilder.equal(root.get("organization").get("publicId"), TenantContext.getCurrentTenant())
					));
		} else {
			query.select(root)
					.where(root.get("publicId").in(ids));
		}

		return entityManager.createQuery(query).getResultList();
	}

	private boolean hasOrganizationField() {
		try {
			entityInformation.getJavaType().getDeclaredField("organization");
			return true;
		} catch (NoSuchFieldException e) {
			return false;
		}
	}
}

package app.bola.taskforge.repository;

import app.bola.taskforge.common.entity.BaseEntity;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.lang.NonNull;

import java.io.Serializable;

public class TenantAwareRepositoryFactoryBean<R extends JpaRepository<T, ID>, T, ID extends Serializable> extends JpaRepositoryFactoryBean<R, T, ID> {
	
	
	public TenantAwareRepositoryFactoryBean(Class<? extends R> repositoryInterface) {
		super(repositoryInterface);
	}
	
	@Override
	protected @NonNull RepositoryFactorySupport createRepositoryFactory(@NonNull EntityManager entityManager) {
		return new TenantAwareRepositoryFactory(entityManager);
	}
	
	private static class TenantAwareRepositoryFactory extends JpaRepositoryFactory {
		
		public TenantAwareRepositoryFactory(EntityManager entityManager) {
			super(entityManager);
		}
		
		@Override
		@SuppressWarnings(value = "unchecked")
		protected @NonNull JpaRepositoryImplementation<?, ?> getTargetRepository(RepositoryInformation info, @NonNull EntityManager entityManager) {
			JpaEntityInformation<?, ?> entityInfo = getEntityInformation(info.getDomainType());
			return new TenantAwareRepositoryImplementation<>((JpaEntityInformation<BaseEntity, ?>) entityInfo, entityManager);
		}
	}
}

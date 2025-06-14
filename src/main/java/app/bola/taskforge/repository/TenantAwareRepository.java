package app.bola.taskforge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface TenantAwareRepository<T, ID> extends JpaRepository<T, ID> {
	
	default Optional<T> findByIdScoped(ID id) {
		throw new UnsupportedOperationException("Method Not implemented");
	}
	
	default List<T> findAllScoped(){
		throw new UnsupportedOperationException("Method Not implemented");
	}
	
	default void deleteByIdScoped(ID id) {
		throw new UnsupportedOperationException("Not implemented");
	}
	
	default List<T> findAllByIdScoped(List<ID> ids) {
		throw new UnsupportedOperationException("Method Not implemented");
	}
}

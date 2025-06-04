package app.bola.taskforge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface TenantAwareRepository<T, ID> extends JpaRepository<T, ID> {

	Optional<T> findByIdScoped(ID id);
	List<T> findAllScoped();
	void deleteByIdScoped(ID id);
}

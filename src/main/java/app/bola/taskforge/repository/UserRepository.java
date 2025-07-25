package app.bola.taskforge.repository;

import app.bola.taskforge.domain.entity.Member;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends TenantAwareRepository<Member, String> {
	
	@Query("SELECT m FROM Member m LEFT JOIN FETCH m.roles WHERE m.email = :email")
	Optional<Member> findByEmail(String email);
	
	
	List<Member> findAllByOrganization_PublicId(String organizationPublicId);
}
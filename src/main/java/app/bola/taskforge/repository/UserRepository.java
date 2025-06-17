package app.bola.taskforge.repository;

import app.bola.taskforge.domain.entity.Member;

import java.util.Optional;

public interface UserRepository extends TenantAwareRepository<Member, String> {
	Optional<Member> findByEmail(String email);
}
package app.bola.taskforge.repository;

import app.bola.taskforge.domain.entity.User;

import java.util.Optional;

public interface UserRepository extends TenantAwareRepository<User, String> {
	Optional<User> findByEmail(String email);
}
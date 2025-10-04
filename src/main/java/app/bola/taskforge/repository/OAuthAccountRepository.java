package app.bola.taskforge.repository;

import app.bola.taskforge.domain.entity.OAuthAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, String> {
	
	Optional<OAuthAccount> findByProviderAndProviderId(String provider, String providerId);
}
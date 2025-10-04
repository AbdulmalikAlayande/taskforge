package app.bola.taskforge.repository;

<<<<<<< Updated upstream
public interface OAuthAccountRepository extends org.springframework.data.repository.Repository<app.bola.taskforge.domain.entity.OAuthAccount, java.lang.String> {
=======
import app.bola.taskforge.domain.entity.OAuthAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, String> {
	
	Optional<OAuthAccount> findByProviderAndProviderId(String provider, String providerId);
>>>>>>> Stashed changes
}
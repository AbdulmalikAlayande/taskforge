package app.bola.taskforge.security.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OAuthVerifierFactory {
    
    private final Map<String, OAuthVerifier> verifiers;
    
    public OAuthVerifierFactory(List<OAuthVerifier> verifierList) {
        this.verifiers = verifierList.stream()
            .collect(Collectors.toMap(
                OAuthVerifier::getProviderName,
                Function.identity()
            ));
    }
    
    public OAuthVerifier getVerifier(String provider) {
        OAuthVerifier verifier = verifiers.get(provider.toLowerCase());
        if (verifier == null) {
	        log.error("Unsupported OAuth provider: {}", provider);
	        throw new RuntimeException("Unsupported OAuth provider: " + provider);
        }
        return verifier;
    }
}
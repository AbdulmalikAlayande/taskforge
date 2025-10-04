package app.bola.taskforge.security.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OAuthVerifierFactory {
    
    private final Map<String, OAuthVerifier> verifiers;
    
    public OAuthVerifierFactory(List<OAuthVerifier> verifierList) {
        this.verifiers = verifierList.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(
                v -> v.getProviderName().toLowerCase(Locale.ROOT),
                Function.identity(),
                (existing, replacement) -> {
                    log.warn("Duplicate OAuthVerifier for provider '{}' found. Keeping first: {} and ignoring: {}",
                        existing.getProviderName(), existing.getClass().getSimpleName(), replacement.getClass().getSimpleName());
                    return existing;
                }
            ));
    }
    
    public OAuthVerifier getVerifier(String provider) {
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("provider must be a non-empty string");
        }
        String key = provider.toLowerCase(Locale.ROOT);
        OAuthVerifier verifier = verifiers.get(key);
        if (verifier == null) {
            log.error("Unsupported OAuth provider: {} (lookup key='{}')", provider, key);
            throw new IllegalArgumentException("Unsupported OAuth provider: " + provider);
        }
        return verifier;
    }
}
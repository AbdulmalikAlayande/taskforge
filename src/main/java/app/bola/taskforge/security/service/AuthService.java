package app.bola.taskforge.security.service;

import app.bola.taskforge.domain.context.TenantContext;
import app.bola.taskforge.domain.entity.Member;
import app.bola.taskforge.domain.entity.OAuthAccount;
import app.bola.taskforge.domain.enums.Role;
import app.bola.taskforge.repository.OAuthAccountRepository;
import app.bola.taskforge.repository.UserRepository;
import app.bola.taskforge.security.dto.AuthResponse;
import app.bola.taskforge.security.dto.LoginRequest;
import app.bola.taskforge.security.provider.JwtTokenProvider;
import app.bola.taskforge.service.dto.OAuthRequest;
import app.bola.taskforge.service.dto.OAuthUserInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class AuthService {
	
	
	private final UserRepository userRepository;
	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;
	private final OAuthVerifierFactory verifierFactory;
	private final OAuthAccountRepository oAuthAccountRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	
	public AuthResponse manageOAuthUser(OAuthRequest request) {
		log.info("Processing OAuth login for provider: {}, email: {}", request.getProvider(), request.getEmail());
		
		OAuthVerifier verifier = verifierFactory.getVerifier(request.getProvider());
		OAuthUserInfo userInfo = verifier.verifyToken(request.getAccessToken());
		if (userInfo == null || !userInfo.getEmail().equalsIgnoreCase(request.getEmail())) {
			log.error("Invalid token or email mismatch for provider: {}", request.getProvider());
			throw new RuntimeException("OAuth token verification failed, Invalid token or email mismatch");
		}
		
		log.info("OAuth token verified successfully for: {}", userInfo.getEmail());
		Member member = findOrCreateOAuthUser(request);
		Set<String> roles = member.getRoles().stream()
				                    .map(Role::name)
				                    .collect(Collectors.toSet());
		
		String accessToken = jwtTokenProvider.generateAccessToken(member.getEmail(), roles);
		String refreshToken = jwtTokenProvider.generateRefreshToken(member.getEmail(), roles);
		
		log.info("Generated JWT tokens for user: {}", member.getEmail());
		String orgId = member.getOrganization() != null ? member.getOrganization().getPublicId() : null;
		return toResponse(member, accessToken, refreshToken, orgId, roles);
		
	}
	
	private AuthResponse toResponse(Member member, String accessToken, String refreshToken, String orgId, Set<String> roles){
		return AuthResponse.builder()
				       .userId(member.getPublicId())
				       .email(member.getEmail())
				       .accessToken(accessToken)
				       .refreshToken(refreshToken)
				       .tenantId(orgId)
				       .organizationId(orgId)
				       .roles(roles)
				       .build();
		
	}
	
	
	public AuthResponse login(LoginRequest loginRequest) {
		System.out.println("Initiating Login");
		Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
		System.out.println("Authentication:: "+authentication.toString());
		Set<String> roles = authentication.getAuthorities()
										  .stream()
										  .map(GrantedAuthority::getAuthority)
										  .collect(Collectors.toSet());
		
		log.info("User {} is authenticated?: {}", loginRequest.getEmail(), authentication.isAuthenticated());
		
		String accessToken = jwtTokenProvider.generateAccessToken(loginRequest.getEmail(), roles);
		String refreshToken = jwtTokenProvider.generateRefreshToken(loginRequest.getEmail(), roles);
		
		SecurityContextHolder.getContext().setAuthentication(authentication);
		Member user = userRepository.findByEmail(loginRequest.getEmail())
				            .orElseThrow(() -> new RuntimeException("User not found with email: " + loginRequest.getEmail()));
		
		String orgId = TenantContext.getCurrentTenant() != null ? TenantContext.getCurrentTenant() : user.getOrganization() != null ? user.getOrganization().getPublicId() : null;
		AuthResponse authResponse = toResponse(user, accessToken, refreshToken, orgId, roles);
		log.info("Login successful for user: {}; Auth Response: {}", loginRequest.getEmail(), authResponse);
		return authResponse;
	}
	
	
	private Member findOrCreateOAuthUser(OAuthRequest request) {
		Optional<Member> optionalMember = userRepository.findByEmail(request.getEmail());
		if (optionalMember.isPresent()){
			Member member = optionalMember.get();
			if (request.getName() != null) {
				String[] nameParts = request.getName().split(" ", 2);
				member.setFirstName(nameParts[0]);
				member.setLastName(nameParts.length > 1 ? nameParts[1] : "");
			}
			if (request.getImageUrl() != null) {
				member.setImageUrl(request.getImageUrl());
			}
			return userRepository.save(member);
		}
		
		log.info("Creating new user for email: {}", request.getEmail());
		Member newMember = new Member();
		String[] nameParts = request.getName() != null ?
				                     request.getName().split(" ", 2) : new String[]{"User", ""};
		newMember.setFirstName(nameParts[0]);
		newMember.setLastName(nameParts.length > 1 ? nameParts[1] : "");
		newMember.setEmail(request.getEmail());
		newMember.setImageUrl(request.getImageUrl());
		newMember.setActive(true);
		// newMember.setEmailVerified(true);
		newMember.setRoles(Set.of(
				Role.ORGANIZATION_OWNER,
				Role.ORGANIZATION_ADMIN,
				Role.ORGANIZATION_MEMBER
		));
		
		return userRepository.save(newMember);
	}
	
	public AuthResponse generateRefreshToken(String refreshToken) {
		log.info("Processing token refresh request");
		
		if (!jwtTokenProvider.isValidToken(refreshToken) ||
				    jwtTokenProvider.isExpiredToken(refreshToken)) {
			log.error("Invalid or expired refresh token");
			throw new RuntimeException("Invalid or expired refresh token");
		}
		
		String email = (String) jwtTokenProvider.extractClaimFromToken(
				refreshToken, "email");
		Member user = userRepository.findByEmail(email)
				              .orElseThrow(() -> new RuntimeException("User not found"));
		
		Set<String> roles = user.getRoles().stream()
				                    .map(Role::name)
				                    .collect(Collectors.toSet());
		
		String newAccessToken = jwtTokenProvider.generateAccessToken(email, roles);
		String newRefreshToken = jwtTokenProvider.generateRefreshToken(email, roles);
		
		log.info("Token refresh successful for user: {}", email);
		
		return AuthResponse.builder()
				       .userId(user.getPublicId())
				       .accessToken(newAccessToken)
				       .refreshToken(newRefreshToken)
				       .email(email)
				       .roles(roles)
				       .build();
	}
}

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
		Member member = findOrCreateOAuthUser(request, userInfo);
		Set<String> roles = member.getRoles().stream()
				                    .map(Role::name)
				                    .collect(Collectors.toSet());
		
		String accessToken = jwtTokenProvider.generateAccessToken(member.getEmail(), roles);
		String refreshToken = jwtTokenProvider.generateRefreshToken(member.getEmail(), roles);
		
		log.info("Generated JWT tokens for user: {}", member.getEmail());
		String orgId = member.getOrganization() != null ? member.getOrganization().getPublicId() : null;
		return toResponse(member, accessToken, refreshToken, orgId, roles);
		
	}
	
	private Member findOrCreateOAuthUser(OAuthRequest request, OAuthUserInfo userInfo) {
		String provider = request.getProvider().toLowerCase();
		String providerId = userInfo.getProviderId();
		String email = userInfo.getEmail();
		
		Optional<OAuthAccount> existingOAuth = oAuthAccountRepository.findByProviderAndProviderId(provider, providerId);
		if (existingOAuth.isPresent()) {
			log.info("Found existing OAuth account for provider: {}, id: {}", provider, providerId);
			Member member = existingOAuth.get().getMember();
			OAuthAccount oauthAccount = existingOAuth.get();
			if (!email.equals(oauthAccount.getEmail())) {
				oauthAccount.setEmail(email);
				oAuthAccountRepository.save(oauthAccount);
			}
			updateMemberFromOAuth(member, userInfo);
			return member;
		}
		
		Optional<Member> optionalMember = userRepository.findByEmail(request.getEmail());
		if (optionalMember.isPresent()){
			Member member = optionalMember.get();
			OAuthAccount oauthAccount = new OAuthAccount();
			oauthAccount.setProvider(provider);
			oauthAccount.setProviderId(providerId);
			oauthAccount.setEmail(email);
			oauthAccount.setMember(member);
			oAuthAccountRepository.save(oauthAccount);
			updateMemberFromOAuth(member, userInfo);
			return member;
		}
		
		log.info("Creating new user for email: {}", request.getEmail());
		Member newMember = new Member();
		String[] nameParts = userInfo.getName() != null
				                     ? userInfo.getName().split(" ", 2)
				                     : new String[]{"", ""};
		newMember.setFirstName(nameParts[0]);
		newMember.setLastName(nameParts.length > 1 ? nameParts[1] : "");
		newMember.setEmail(email);
		newMember.setImageUrl(userInfo.getImageUrl());
		newMember.setActive(true);
//		newMember.setEmailVerified(true);
		newMember.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
		newMember.setRoles(Set.of(Role.ORGANIZATION_OWNER, Role.ORGANIZATION_ADMIN, Role.ORGANIZATION_MEMBER));
		Member savedMember = userRepository.save(newMember);
		
		OAuthAccount oauthAccount = new OAuthAccount();
		oauthAccount.setProvider(provider);
		oauthAccount.setProviderId(providerId);
		oauthAccount.setEmail(email);
		oauthAccount.setMember(savedMember);
		
		oAuthAccountRepository.save(oauthAccount);
		log.info("Successfully created new user and OAuth account for: {}", email);
		return savedMember;
	}
	
	private void updateMemberFromOAuth(Member member, OAuthUserInfo userInfo) {
		boolean updated = false;
		if (userInfo.getImageUrl() != null &&
				    !userInfo.getImageUrl().equals(member.getImageUrl())) {
			member.setImageUrl(userInfo.getImageUrl());
			updated = true;
		}
		if (userInfo.getName() != null) {
			String[] nameParts = userInfo.getName().split(" ", 2);
			String firstName = nameParts[0];
			String lastName = nameParts.length > 1 ? nameParts[1] : "";
			
			if (!firstName.equals(member.getFirstName()) ||
					    !lastName.equals(member.getLastName())) {
				member.setFirstName(firstName);
				member.setLastName(lastName);
				updated = true;
			}
		}
		if (updated) {
			userRepository.save(member);
			log.info("Updated member profile from OAuth data for: {}", member.getEmail());
		}
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

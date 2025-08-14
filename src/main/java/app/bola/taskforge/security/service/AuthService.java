package app.bola.taskforge.security.service;

import app.bola.taskforge.domain.context.TenantContext;
import app.bola.taskforge.domain.entity.Member;
import app.bola.taskforge.domain.enums.Role;
import app.bola.taskforge.repository.UserRepository;
import app.bola.taskforge.security.dto.AuthResponse;
import app.bola.taskforge.security.dto.LoginRequest;
import app.bola.taskforge.security.provider.JwtTokenProvider;
import app.bola.taskforge.service.dto.MemberResponse;
import app.bola.taskforge.service.dto.OAuthRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class AuthService {
	
	
	private final ModelMapper modelMapper;
	private final UserRepository userRepository;
	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;
	
	public MemberResponse manageOAuthUser(OAuthRequest request) {
		Optional<Member> optionalMember = userRepository.findByEmail(request.getEmail());
		if (optionalMember.isPresent()) {
			log.info("User with email {} already exists, returning existing user", request.getEmail());
			return modelMapper.map(optionalMember.get(), MemberResponse.class);
		}
		Member member = modelMapper.map(request, Member.class);
		member.setActive(true);
		member.setRoles(Set.of(Role.ORGANIZATION_ADMIN, Role.ORGANIZATION_OWNER, Role.ORGANIZATION_MEMBER));
		Member savedMember = userRepository.save(member);
		return toResponse(savedMember);
	}
	
	private MemberResponse toResponse(Member savedMember) {
		return modelMapper.map(savedMember, MemberResponse.class);
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
		
		String userId = user.getPublicId();
		AuthResponse authResponse = AuthResponse.builder()
				                     .userId(userId)
				                     .accessToken(accessToken)
				                     .refreshToken(refreshToken)
				                     .tenantId(TenantContext.getCurrentTenant() != null ? TenantContext.getCurrentTenant() : user.getOrganization() != null ? user.getOrganization().getPublicId() : null)
				                     .organizationId(TenantContext.getCurrentTenant() != null ? TenantContext.getCurrentTenant() : user.getOrganization() != null ? user.getOrganization().getPublicId() : null)
				                     .email(loginRequest.getEmail())
				                     .roles(roles)
				                     .build();
		log.info("Login successful for user: {}; Auth Response: {}", loginRequest.getEmail(), authResponse);
		return authResponse;
	}
	
	public AuthResponse generateRefreshToken(String refreshToken) {
		return null;
	}
}

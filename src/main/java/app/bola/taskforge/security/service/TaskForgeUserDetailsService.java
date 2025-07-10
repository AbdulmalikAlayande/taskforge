package app.bola.taskforge.security.service;

import app.bola.taskforge.domain.context.TenantContext;
import app.bola.taskforge.domain.entity.Member;
import app.bola.taskforge.exception.UnauthorizedException;
import app.bola.taskforge.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
@Transactional
@AllArgsConstructor
public class TaskForgeUserDetailsService implements UserDetailsService {
	
	private final UserRepository userRepository;
	
	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		Member member = userRepository.findByEmail(username)
				                      .orElseThrow(() -> new UsernameNotFoundException(username));
		
		String currentTenant = TenantContext.getCurrentTenant();
		if (member.getOrganization() != null) {
			if (!member.getOrganization().getPublicId().equals(currentTenant)) {
				throw new UnauthorizedException("User not authorized for this organization");
			}
		}
		
		List<SimpleGrantedAuthority> authorities = member.getRoles()
			.stream()
			.map(role -> new SimpleGrantedAuthority("ROLE_" + role.name())).toList();
		
		log.info("Authorities created: {}", authorities);
		return new User(member.getEmail(), member.getPassword(), authorities);
	}
}

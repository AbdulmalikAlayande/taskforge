package app.bola.taskforge.security.service;

import app.bola.taskforge.domain.context.TenantContext;
import app.bola.taskforge.domain.entity.Member;
import app.bola.taskforge.exception.UnauthorizedException;
import app.bola.taskforge.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@AllArgsConstructor
public class TaskForgeUserDetailsService implements UserDetailsService {
	
	private final UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Member member = userRepository.findByEmail(username)
				                      .orElseThrow(() -> new UsernameNotFoundException(username));
		
		String currentTenant = TenantContext.getCurrentTenant();
		if (!member.getOrganization().getPublicId().equals(currentTenant)) {
			throw new UnauthorizedException("User not authorized for this organization");
		}
		
		List<SimpleGrantedAuthority> authorities = member.getRoles()
			.stream()
			.map(role -> new SimpleGrantedAuthority("ROLE_" + role.name())).toList();
		
		return new User(member.getEmail(), member.getPassword(), authorities);
	}
}

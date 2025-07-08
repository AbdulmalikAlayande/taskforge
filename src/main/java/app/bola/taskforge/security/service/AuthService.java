package app.bola.taskforge.security.service;

import app.bola.taskforge.domain.entity.Member;
import app.bola.taskforge.domain.enums.Role;
import app.bola.taskforge.repository.UserRepository;
import app.bola.taskforge.service.dto.MemberResponse;
import app.bola.taskforge.service.dto.OAuthRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@AllArgsConstructor
public class AuthService {
	
	
	private final ModelMapper modelMapper;
	private final UserRepository userRepository;
	
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
}

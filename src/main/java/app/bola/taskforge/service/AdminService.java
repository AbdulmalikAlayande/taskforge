package app.bola.taskforge.service;

import app.bola.taskforge.domain.entity.Member;
import app.bola.taskforge.domain.enums.Role;
import app.bola.taskforge.repository.UserRepository;
import app.bola.taskforge.service.dto.CreateAdminRequest;
import app.bola.taskforge.service.dto.MemberResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
	
	final ModelMapper modelMapper;
	private final UserRepository userRepository;
	
	/**
	 * Creates a new organization admin.
	 *
	 * @param request the request containing admin details
	 * @return the response containing created admin details
	 */
	public MemberResponse createOrgAdmin(CreateAdminRequest request) {
		log.info("Creating new organization admin: {}", request);
		Member admin = modelMapper.map(request, Member.class);
		admin.setRoles(Set.of(Role.ORGANIZATION_ADMIN, Role.ORGANIZATION_OWNER, Role.ORGANIZATION_MEMBER));
		admin.setActive(true);
		
		Member savedAdmin = userRepository.save(admin);
		
		return toResponse(savedAdmin);
	}
	
	private MemberResponse toResponse(Member savedAdmin) {
		return modelMapper.map(savedAdmin, MemberResponse.class);
	}
}

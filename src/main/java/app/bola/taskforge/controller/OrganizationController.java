package app.bola.taskforge.controller;

import app.bola.taskforge.common.controller.BaseController;
import app.bola.taskforge.service.OrganizationService;
import app.bola.taskforge.service.dto.*;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@AllArgsConstructor
@RequestMapping(value = "api/organization")
public class OrganizationController implements BaseController<OrganizationRequest, OrganizationResponse> {
	
	private static final Logger log = LoggerFactory.getLogger(OrganizationController.class);
	final OrganizationService organizationService;
	
	@Override
	public ResponseEntity<OrganizationResponse> createNew(OrganizationRequest request) {
		log.info("Organization Request:: {}", request);
		return ResponseEntity.ok(organizationService.createNew(request));
	}
	
	@Override
	public ResponseEntity<OrganizationResponse> getById(@PathVariable String publicId) {
		log.info("Organization Fetch ID:: {}", publicId);
		return ResponseEntity.ok(organizationService.findById(publicId));
	}
	
	@Override
	public ResponseEntity<Collection<OrganizationResponse>> getAll() {
		return ResponseEntity.ok(organizationService.findAll());
	}
	
	public ResponseEntity<OrganizationResponse> update(@PathVariable String publicId, @RequestBody OrganizationRequest request) {
		return ResponseEntity.ok(organizationService.update(publicId, request));
	}
	
	@Override
	public ResponseEntity<Void> delete(@PathVariable String publicId) {
		organizationService.delete(publicId);
		return ResponseEntity.noContent().build();
	}
	
	/**
	 * Endpoint to invite a new member to the organization.
	 * Only accessible by users with '{@code ORGANIZATION_OWNER}' or '{@code ORGANIZATION_ADMIN}' roles.
	 *
	 * @param request the invitation request containing details of the member to be invited
	 * @return ResponseEntity with the result of the invitation process
	 */
	@PostMapping("invite-member")
	public ResponseEntity<InvitationResponse> inviteMember(@RequestBody InvitationRequest request) {
		log.info("Invitation Request:: {}", request);
		return ResponseEntity.ok(organizationService.inviteMember(request));
	}
	
	@GetMapping("{public-id}/members")
	public ResponseEntity<Collection<MemberResponse>> getMembers(@PathVariable("public-id") String publicId) {
		log.info("Fetching members for organization with publicId: {}", publicId);
		return ResponseEntity.ok(organizationService.getMembers(publicId));
	}
}

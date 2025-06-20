package app.bola.taskforge.controller;

import app.bola.taskforge.common.controller.BaseController;
import app.bola.taskforge.service.OrganizationService;
import app.bola.taskforge.service.dto.InvitationRequest;
import app.bola.taskforge.service.dto.InvitationResponse;
import app.bola.taskforge.service.dto.OrganizationRequest;
import app.bola.taskforge.service.dto.OrganizationResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/api/organizations")
public class OrganizationController implements BaseController<OrganizationRequest, OrganizationResponse> {
	
	final OrganizationService organizationService;
	
	@Override
	public ResponseEntity<OrganizationResponse> createNew(OrganizationRequest request) {
		return ResponseEntity.ok(organizationService.createNew(request));
	}
	
	/**
	 * Endpoint to invite a new member to the organization.
	 * Only accessible by users with '{@code ORGANIZATION_OWNER}' or '{@code ORGANIZATION_ADMIN}' roles.
	 *
	 * @param request the invitation request containing details of the member to be invited
	 * @return ResponseEntity with the result of the invitation process
	 */
	@PostMapping("/invite-member")
//	@PreAuthorize(value = "hasAnyRole('ROLE_ORGANIZATION_OWNER', 'ROLE_ORGANIZATION_ADMIN')")
	public ResponseEntity<InvitationResponse> inviteMember(@RequestBody InvitationRequest request) {
		return ResponseEntity.ok(organizationService.inviteMember(request));
	}
	
}

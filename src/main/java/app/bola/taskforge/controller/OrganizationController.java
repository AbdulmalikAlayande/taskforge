package app.bola.taskforge.controller;

import app.bola.taskforge.common.controller.BaseController;
import app.bola.taskforge.service.OrganizationService;
import app.bola.taskforge.service.dto.InvitationRequest;
import app.bola.taskforge.service.dto.InvitationResponse;
import app.bola.taskforge.service.dto.OrganizationRequest;
import app.bola.taskforge.service.dto.OrganizationResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/api/organizations")
public class OrganizationController implements BaseController<OrganizationRequest, OrganizationResponse> {
	
	final OrganizationService organizationService;
	
	@Override
	public ResponseEntity<OrganizationResponse> createNew(OrganizationRequest request) {
		return ResponseEntity.ok(organizationService.createNew(request));
	}
	
	@Override
	public ResponseEntity<OrganizationResponse> getById(@PathVariable String publicId) {
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
	@PostMapping("/invite-member")
//	@PreAuthorize(value = "hasAnyRole('ROLE_ORGANIZATION_OWNER', 'ROLE_ORGANIZATION_ADMIN')")
	public ResponseEntity<InvitationResponse> inviteMember(@RequestBody InvitationRequest request) {
		return ResponseEntity.ok(organizationService.inviteMember(request));
	}
}

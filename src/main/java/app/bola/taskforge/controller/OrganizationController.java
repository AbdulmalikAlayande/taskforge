package app.bola.taskforge.controller;

import app.bola.taskforge.common.controller.BaseController;
import app.bola.taskforge.service.OrganizationService;
import app.bola.taskforge.service.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@AllArgsConstructor
@RequestMapping(value = "api/organization")
@Tag(name = "Organization Management", description = "APIs for managing organizations")
@SecurityRequirement(name = "bearerAuth")
public class OrganizationController implements BaseController<OrganizationRequest, OrganizationResponse> {
	
	private static final Logger log = LoggerFactory.getLogger(OrganizationController.class);
	final OrganizationService organizationService;
	
	@Override
	@PostMapping("create-new")
	@Operation(summary = "Create a new organization", description = "Creates a new organization with the provided details")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Organization created successfully",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationResponse.class))),
		@ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
		@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
		@ApiResponse(responseCode = "403", description = "Unauthorized to create an organization", content = @Content)
	})
	public ResponseEntity<OrganizationResponse> createNew(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Organization details", required = true)
			@RequestBody OrganizationRequest request) {
		log.info("Organization Request:: {}", request);
		return ResponseEntity.ok(organizationService.createNew(request));
	}
	
	@Override
	@GetMapping("{publicId}")
	@Operation(summary = "Get organization by ID", description = "Retrieves organization details by its public ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Organization found",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationResponse.class))),
		@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
		@ApiResponse(responseCode = "404", description = "Organization not found", content = @Content)
	})
	public ResponseEntity<OrganizationResponse> getById(
			@Parameter(description = "Public ID of the organization", required = true)
			@PathVariable String publicId) {
		log.info("Organization Fetch ID:: {}", publicId);
		return ResponseEntity.ok(organizationService.findById(publicId));
	}
	
	@Override
	@GetMapping("all")
	@Operation(summary = "Get all organizations", description = "Retrieves all organizations the user has access to")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "List of organizations",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationResponse.class))),
		@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
	})
	public ResponseEntity<Collection<OrganizationResponse>> getAll() {
		return ResponseEntity.ok(organizationService.findAll());
	}
	
	@PutMapping("{publicId}")
	@Operation(summary = "Update organization", description = "Updates an existing organization with new details")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Organization updated successfully",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationResponse.class))),
		@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
		@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
		@ApiResponse(responseCode = "404", description = "Organization not found", content = @Content)
	})
	public ResponseEntity<OrganizationResponse> update(
			@Parameter(description = "Public ID of the organization", required = true) @PathVariable String publicId,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated organization details", required = true)
			@RequestBody OrganizationRequest request) {
		return ResponseEntity.ok(organizationService.update(publicId, request));
	}
	
	@Override
	@DeleteMapping("/{publicId}")
	@Operation(summary = "Delete organization", description = "Deletes an organization by its public ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "Organization deleted successfully"),
		@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
		@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
		@ApiResponse(responseCode = "404", description = "Organization not found", content = @Content)
	})
	public ResponseEntity<Void> delete(
			@Parameter(description = "Public ID of the organization", required = true)
			@PathVariable String publicId) {
		organizationService.delete(publicId);
		return ResponseEntity.noContent().build();
	}
	
	@PostMapping("invite-member")
	@Operation(summary = "Invite member to organization",
			description = "Invites a new member to join the organization. Only accessible by users with 'ORGANIZATION_OWNER' or 'ORGANIZATION_ADMIN' roles.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Invitation sent successfully",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = InvitationResponse.class))),
		@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
		@ApiResponse(responseCode = "403", description = "Unauthorized to send invitations", content = @Content)
	})
	public ResponseEntity<InvitationResponse> inviteMember(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Invitation details", required = true)
			@RequestBody InvitationRequest request) {
		log.info("Invitation Request:: {}", request);
		return ResponseEntity.ok(organizationService.inviteMember(request));
	}
	
	@GetMapping("{public-id}/members")
	@Operation(summary = "Get organization members",
			description = "Retrieves all members of an organization")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "List of members",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = MemberResponse.class))),
		@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
		@ApiResponse(responseCode = "404", description = "Organization not found", content = @Content)
	})
	public ResponseEntity<Collection<MemberResponse>> getMembers(
			@Parameter(description = "Public ID of the organization", required = true)
			@PathVariable("public-id") String publicId) {
		log.info("Fetching members for organization with publicId: {}", publicId);
		return ResponseEntity.ok(organizationService.getMembers(publicId));
	}
}

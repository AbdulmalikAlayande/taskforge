package app.bola.taskforge.controller;

import app.bola.taskforge.common.controller.BaseController;
import app.bola.taskforge.service.MemberService;
import app.bola.taskforge.service.dto.InvitationResponse;
import app.bola.taskforge.service.dto.MemberRequest;
import app.bola.taskforge.service.dto.MemberResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/api/members")
@AllArgsConstructor
@Tag(name = "Member Management", description = "APIs for managing organization members")
@SecurityRequirement(name = "bearerAuth")
public class MemberController implements BaseController<MemberRequest, MemberResponse> {
	
	private final MemberService memberService;
	
	@Override
	@PostMapping
	@Operation(summary = "Create a new member", description = "Creates a new member with the provided details")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Member created successfully",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = MemberResponse.class))),
		@ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
		@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
		@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
	})
	public ResponseEntity<MemberResponse> createNew(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Member details", required = true)
			@RequestBody MemberRequest request) {
		return ResponseEntity.ok(memberService.createNew(request));
	}
	
	@GetMapping("/{publicId}")
	@Override
	@Operation(summary = "Get member by ID", description = "Retrieves member details by their public ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Member found",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = MemberResponse.class))),
		@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
		@ApiResponse(responseCode = "404", description = "Member not found", content = @Content)
	})
	public ResponseEntity<MemberResponse> getById(
			@Parameter(description = "Public ID of the member", required = true)
			@PathVariable String publicId) {
		return ResponseEntity.ok(memberService.findById(publicId));
	}
	
	@GetMapping
	@Operation(summary = "Get all members", description = "Retrieves all members the user has access to")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "List of members",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = MemberResponse.class))),
		@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
	})
	public ResponseEntity<Collection<MemberResponse>> getAll() {
		return ResponseEntity.ok(memberService.findAll());
	}
	
	@PutMapping("/{publicId}")
	@Operation(summary = "Update member", description = "Updates an existing member with new details")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Member updated successfully",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = MemberResponse.class))),
		@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
		@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
		@ApiResponse(responseCode = "404", description = "Member not found", content = @Content)
	})
	public ResponseEntity<MemberResponse> update(
			@Parameter(description = "Public ID of the member", required = true) @PathVariable String publicId,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated member details", required = true)
			@RequestBody MemberRequest request) {
		return ResponseEntity.ok(memberService.update(publicId, request));
	}
	
	@DeleteMapping("/{publicId}")
	@Operation(summary = "Delete member", description = "Deletes a member by their public ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "Member deleted successfully"),
		@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
		@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
		@ApiResponse(responseCode = "404", description = "Member not found", content = @Content)
	})
	public ResponseEntity<Void> delete(
			@Parameter(description = "Public ID of the member", required = true)
			@PathVariable String publicId) {
		memberService.delete(publicId);
		return ResponseEntity.noContent().build();
	}
	
	@PostMapping("/accept-invitation")
	@Operation(
		summary = "Accept invitation",
		description = "Accepts an invitation to join an organization",
		security = {} // No security required for this endpoint
	)
	@SecurityRequirements // Explicitly indicate no security for this endpoint
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Invitation accepted successfully",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = InvitationResponse.class))),
		@ApiResponse(responseCode = "400", description = "Invalid token", content = @Content)
	})
	public ResponseEntity<InvitationResponse> acceptInvitation(
			@Parameter(description = "Invitation token", required = true)
			@RequestParam String token) {
		log.info("Invitation Acceptance Token: {}", token);
		return ResponseEntity.ok(memberService.acceptInvitation(token));
	}
}
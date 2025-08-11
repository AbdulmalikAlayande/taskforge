package app.bola.taskforge.controller;

import app.bola.taskforge.service.AdminService;
import app.bola.taskforge.service.dto.CreateAdminRequest;
import app.bola.taskforge.service.dto.MemberResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@AllArgsConstructor
@Tag(name = "Admin Management", description = "APIs for admin operations")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
	
	final AdminService adminService;
	
	@Operation(
		summary = "Create a new organization admin",
		description = "Creates a new administrator for an organization with appropriate permissions"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Admin created successfully",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = MemberResponse.class))),
		@ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
		@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
		@ApiResponse(responseCode = "403", description = "Unauthorized access", content = @Content)
	})
	@PostMapping("/create-new")
	public ResponseEntity<MemberResponse> createOrgAdmin(@Valid @RequestBody CreateAdminRequest request) {
		return ResponseEntity.ok(adminService.createOrgAdmin(request));
	}
}

package app.bola.taskforge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/log")
@AllArgsConstructor
@Tag(name = "Audit Logs", description = "APIs for managing system audit logs")
public class AuditLogController {
	
	@PostMapping("/create-new")
	@Operation(summary = "Create audit log entry", description = "Creates a new audit log entry with the specified details")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Log entry created successfully",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
		@ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
	})
	public ResponseEntity<String> createNew(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Log entry details", required = true)
			@RequestBody LogRequest request) {
		log.info("Creating new audit log entry: {}", request);
		// Here you would typically call a service to handle the log creation
		return ResponseEntity.ok("Audit log entry created successfully");
	}
	
	@Data
	@Schema(description = "Audit log entry request")
	static class LogRequest {
		@Schema(description = "Log message content", example = "User updated project settings")
		String message;
		
		@Schema(description = "Log level", example = "INFO", allowableValues = {"INFO", "WARNING", "ERROR", "DEBUG"})
		String level;
		
		@Schema(description = "Additional metadata in JSON format", example = "{\"userId\":\"123\",\"action\":\"UPDATE\"}")
		String meta;
	}
}

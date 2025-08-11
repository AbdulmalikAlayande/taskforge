package app.bola.taskforge.controller;

import app.bola.taskforge.common.controller.BaseController;
import app.bola.taskforge.service.CommentService;
import app.bola.taskforge.service.dto.CommentRequest;
import app.bola.taskforge.service.dto.CommentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comment")
@Tag(name = "Comments", description = "APIs for managing comments on tasks and projects")
public class CommentController implements BaseController<CommentRequest, CommentResponse> {
	
	private final CommentService commentService;
	
	@Override
	@PostMapping("create-new")
	@Operation(summary = "Create a new comment", description = "Creates a new comment with the provided content")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Comment created successfully",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentResponse.class))),
		@ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
	})
	public ResponseEntity<CommentResponse> createNew(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Comment details", required = true)
			@RequestBody CommentRequest request) {
		return ResponseEntity.ok(commentService.createNew(request));
	}
	
	@PatchMapping("{comment-id}/edit")
	@Operation(summary = "Edit a comment", description = "Updates the content of an existing comment")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Comment updated successfully",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentResponse.class))),
		@ApiResponse(responseCode = "404", description = "Comment not found", content = @Content)
	})
	public ResponseEntity<CommentResponse> edit(
			@Parameter(description = "ID of the comment to edit", required = true) @PathVariable("comment-id") String commentId,
			@Parameter(description = "ID of the user editing the comment", required = true) @RequestParam String editorId,
			@Parameter(description = "New content for the comment", required = true) @RequestParam String content) {
		return ResponseEntity.ok(commentService.edit(commentId, editorId, content));
	}
	
	@PostMapping("{comment-id}/reply")
	@Operation(summary = "Reply to a comment", description = "Creates a reply to an existing comment")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Reply created successfully",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentResponse.class))),
		@ApiResponse(responseCode = "404", description = "Parent comment not found", content = @Content)
	})
	public ResponseEntity<CommentResponse> replyToComment(
			@Parameter(description = "ID of the parent comment", required = true) @PathVariable("comment-id") String commentId,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Reply details", required = true)
			@RequestBody CommentRequest request) {
		return ResponseEntity.ok(commentService.replyToComment(commentId, request));
	}
	
	@GetMapping("{comment-id}/replies")
	@Operation(summary = "Get comment replies", description = "Retrieves all replies to a specific comment")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "302", description = "Replies found",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentResponse.class))),
		@ApiResponse(responseCode = "404", description = "Comment not found", content = @Content)
	})
	public ResponseEntity<Set<CommentResponse>> getReplies(
			@Parameter(description = "ID of the comment", required = true)
			@PathVariable("comment-id") String commentId){
		return ResponseEntity.status(HttpStatus.FOUND).body(commentService.getReplies(commentId));
	}
}

package app.bola.taskforge.controller;

import app.bola.taskforge.common.controller.BaseController;
import app.bola.taskforge.service.CommentService;
import app.bola.taskforge.service.dto.CommentRequest;
import app.bola.taskforge.service.dto.CommentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comment")
public class CommentController implements BaseController<CommentRequest, CommentResponse> {
	
	private final CommentService commentService;
	
	@Override
	public ResponseEntity<CommentResponse> createNew(CommentRequest request) {
		return ResponseEntity.ok(commentService.createNew(request));
	}
	
	@PatchMapping("{comment-id}/edit")
	public ResponseEntity<CommentResponse> edit(@PathVariable("comment-id") String commentId,
	                                            @RequestParam String editorId, @RequestParam String content) {
		return ResponseEntity.ok(commentService.edit(commentId, editorId, content));
	}
	
	@PostMapping("{comment-id}/reply")
	public ResponseEntity<CommentResponse> replyToComment(@PathVariable("comment-id") String commentId, @RequestBody CommentRequest request) {
		return ResponseEntity.ok(commentService.replyToComment(commentId, request));
	}
	
	@GetMapping("{comment-id}/replies")
	public ResponseEntity<Set<CommentResponse>> getReplies(@PathVariable("comment-id") String commentId){
		return ResponseEntity.status(HttpStatus.FOUND).body(commentService.getReplies(commentId));
	}
	
}

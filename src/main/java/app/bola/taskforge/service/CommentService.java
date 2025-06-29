package app.bola.taskforge.service;

import app.bola.taskforge.common.service.BaseService;
import app.bola.taskforge.domain.entity.Comment;
import app.bola.taskforge.service.dto.AttachmentResponse;
import app.bola.taskforge.service.dto.CommentRequest;
import app.bola.taskforge.service.dto.CommentResponse;
import app.bola.taskforge.service.dto.MentionResponse;

import java.util.List;
import java.util.Set;

public interface CommentService extends BaseService<CommentRequest, Comment, CommentResponse> {
	
	CommentResponse edit(String commentId, String editorId, String content);
	
	CommentResponse replyToComment(String parentCommentId, CommentRequest request);
	
	Set<CommentResponse> getReplies(String commentId);
	
	List<AttachmentResponse> getAttachments(String commentId);
		                                          
	void deleteAttachment(String attachmentId, Long userId);
	
	Set<MentionResponse> getAllMentionsForComment(String commentId);
	
	// extractAndSaveMentions(List<String> mentions) // → Parses @mentions, verifies project membership, stores mentions.
	//validateProjectMembership(Long userId, Long projectId) //→ Checks if the user is a member of the project the task belongs to.
	//hasAccessToComment(Long userId, Long commentId) // → Validates whether a user can view/edit/delete a comment.
}

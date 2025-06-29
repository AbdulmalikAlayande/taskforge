package app.bola.taskforge.service;

import app.bola.taskforge.service.dto.AttachmentResponse;
import app.bola.taskforge.service.dto.CommentRequest;
import app.bola.taskforge.service.dto.CommentResponse;
import app.bola.taskforge.service.dto.MentionResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
public class TaskForgeCommentService implements CommentService{
	
	
	@Override
	public CommentResponse createNew(@NonNull CommentRequest request) {
		return null;
	}
	
	@Override
	public CommentResponse update(String publicId, @NonNull CommentRequest request) {
		return null;
	}
	
	@Override
	public CommentResponse findById(String publicId) {
		return null;
	}
	
	@Override
	public Collection<CommentResponse> findAll() {
		return List.of();
	}
	
	@Override
	public CommentResponse edit(String commentId, String editorId, String content) {
		return null;
	}
	
	@Override
	public CommentResponse replyToComment(String parentCommentId, CommentRequest request) {
		return null;
	}
	
	@Override
	public Set<CommentResponse> getReplies(String commentId) {
		return Set.of();
	}
	
	@Override
	public List<AttachmentResponse> getAttachments(String commentId) {
		return List.of();
	}
	
	@Override
	public void deleteAttachment(String attachmentId, Long userId) {
	
	}
	
	@Override
	public void delete(String id) {
	
	}
	
	@Override
	public Set<MentionResponse> getAllMentionsForComment(String commentId) {
		return Set.of();
	}
}

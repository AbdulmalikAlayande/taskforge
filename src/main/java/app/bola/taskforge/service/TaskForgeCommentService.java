package app.bola.taskforge.service;

import app.bola.taskforge.domain.entity.Comment;
import app.bola.taskforge.domain.entity.Member;
import app.bola.taskforge.domain.entity.Project;
import app.bola.taskforge.domain.entity.Task;
import app.bola.taskforge.domain.enums.Role;
import app.bola.taskforge.exception.EntityNotFoundException;
import app.bola.taskforge.exception.InvalidRequestException;
import app.bola.taskforge.repository.CommentRepository;
import app.bola.taskforge.repository.ProjectRepository;
import app.bola.taskforge.repository.TaskRepository;
import app.bola.taskforge.repository.UserRepository;
import app.bola.taskforge.service.dto.AttachmentResponse;
import app.bola.taskforge.service.dto.CommentRequest;
import app.bola.taskforge.service.dto.CommentResponse;
import app.bola.taskforge.service.dto.MentionResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import jakarta.validation.Validator;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
public class TaskForgeCommentService implements CommentService{
	
	final ModelMapper modelMapper;
	private final UserRepository userRepository;
	private final TaskRepository taskRepository;
	private final CommentRepository commentRepository;
	private final ProjectRepository projectRepository;
	private final Validator validator;
	
	@Override
	public CommentResponse createNew(@NonNull CommentRequest request) {
		performValidation(validator, request);
		Member author = userRepository.findByIdScoped(request.getAuthorId())
			.orElseThrow(() -> new EntityNotFoundException("Comment author not found"));
		
		Task task = taskRepository.findByIdScoped(request.getTaskId())
			.orElseThrow(() -> new EntityNotFoundException("Task not found"));
		
		verifyProjectMembership(author, task, request.getProjectId());
		Optional<Comment> optionalParentComment = commentRepository.findByIdScoped(request.getParentCommentId());
		
		Comment comment = modelMapper.map(request, Comment.class);
		comment.setAuthor(author);
		comment.setTask(task);
		comment.setOrganization(author.getOrganization() != null ? author.getOrganization() : task.getOrganization());
		optionalParentComment.ifPresent(comment::setParentComment);
		
		return toResponse(commentRepository.save(comment));
	}
	
	private void verifyProjectMembership(Member author, Task task, String projectId) {
		Optional<Project> optionalProject = Optional.ofNullable(task.getProject());
		Project project;
		if (optionalProject.isPresent()) {
			project = optionalProject.get();
			if (!project.getMembers().contains(author) && !author.getRole().equals(Role.ORGANIZATION_ADMIN)) {
				throw new InvalidRequestException("Author is not a member of the project associated with this task");
			}
		}
		else {
			project = projectRepository.findByIdScoped(projectId)
				.orElseThrow(() -> new EntityNotFoundException("Project not found"));
			if (!project.getMembers().contains(author) && !author.getRole().equals(Role.ORGANIZATION_ADMIN)) {
				throw new InvalidRequestException("Author is not a member of the project project associated with this task");
			}
		}
	}
	
	@Override
	public CommentResponse toResponse(Comment entity) {
		return modelMapper.map(entity, CommentResponse.class);
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

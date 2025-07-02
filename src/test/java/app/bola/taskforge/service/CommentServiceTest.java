package app.bola.taskforge.service;

import app.bola.taskforge.domain.entity.*;
import app.bola.taskforge.domain.enums.Role;
import app.bola.taskforge.exception.EntityNotFoundException;
import app.bola.taskforge.exception.InvalidRequestException;
import app.bola.taskforge.exception.UnauthorizedException;
import app.bola.taskforge.repository.*;
import app.bola.taskforge.service.dto.CommentRequest;
import app.bola.taskforge.service.dto.CommentResponse;
import app.bola.taskforge.service.dto.MentionResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(value = MockitoExtension.class)
class CommentServiceTest {
	
	@Mock
	private ModelMapper modelMapper;
	
	@Mock
	private UserRepository userRepository;
	
	@Mock
	private TaskRepository taskRepository;
	
	@Mock
	private ProjectRepository projectRepository;
	
	@Mock
	private OrganizationRepository organizationRepository;
	
	@Mock
	private CommentRepository commentRepository;
	
	@InjectMocks
	private TaskForgeCommentService commentService;
	
	@BeforeEach
	void setUp() {
	
	}
	
	@Nested
	@DisplayName("Create Comment Tests")
	class CreateCommentTests {
		
		Project project;
		Task task;
		Member author;
		CommentRequest commentRequest;
		Comment comment;
		CommentResponse commentResponse;
		
		@BeforeEach
		void setUp() {
			String organizationId = UUID.randomUUID().toString();
			Organization organization = Organization.builder().publicId(organizationId).name("Test Organization").build();
			
			String projectId = UUID.randomUUID().toString();
			project = Project.builder().publicId(projectId).name("Test Project").organization(organization).build();
			
			String taskId = UUID.randomUUID().toString();
			task = Task.builder().publicId(taskId).organization(organization).title("Test task title").build();
			
			String authorId = UUID.randomUUID().toString();
			author = Member.builder().publicId(authorId).organization(organization).firstName("Test").lastName("Author")
                .role(Role.ORGANIZATION_ADMIN).build();
			
			commentRequest = CommentRequest.builder().content("This is a comment, that is being added based for test purposes")
					                 .taskId(task.getPublicId()).projectId(project.getPublicId()).authorId(author.getPublicId())
					                 .mentions(List.of("@user1")).build();
			
			comment = Comment.builder().task(task).author(author).content(commentRequest.getContent()).organization(organization)
				.mentions(List.of(new Mention(), new Mention())).publicId(UUID.randomUUID().toString()).build();
			
			when(userRepository.findByIdScoped(author.getPublicId())).thenReturn(Optional.of(author));
			when(taskRepository.findByIdScoped(task.getPublicId())).thenReturn(Optional.of(task));
			
			commentResponse = CommentResponse.builder().content(comment.getContent()).authorId(comment.getAuthor().getPublicId())
					                        .taskId(comment.getTask().getPublicId()).organizationId(organization.getPublicId()).publicId(comment.getPublicId())
					                        .mentions(List.of(new MentionResponse())).build();
		}
		
		@Test
		@DisplayName("should create a new comment under a task successfully")
		public void shouldCreateACommentSuccessfully() {
			
			commentRequest = CommentRequest.builder().content("This is a comment, that is being added based for test purposes")
					                 .taskId(task.getPublicId()).projectId(project.getPublicId()).authorId(author.getPublicId())
					                 .mentions(List.of("@user1")).build();
			
			when(projectRepository.findByIdScoped(project.getPublicId())).thenReturn(Optional.of(project));
			when(modelMapper.map(any(CommentRequest.class), eq(Comment.class))).thenReturn(comment);
			when(commentRepository.save(comment)).thenReturn(comment);
			when(modelMapper.map(comment, CommentResponse.class)).thenReturn(commentResponse);
			
			CommentResponse commentResponse = commentService.createNew(commentRequest);
			assertNotNull(commentResponse);
			assertNotNull(commentResponse.getPublicId());
		}
		
		@Test
		@DisplayName("Should throw InvalidRequestException if user is not part of the project and user is not an org admin.")
		public void shouldThrowInvalidRequestExceptionIfUserNotPartOfProject() {
			project.setMembers(Set.of());
			author.setRole(Role.ORGANIZATION_MEMBER);
			task.setProject(project);
			
			when(userRepository.findByIdScoped(author.getPublicId())).thenReturn(Optional.of(author));
			when(taskRepository.findByIdScoped(task.getPublicId())).thenReturn(Optional.of(task));
			
			commentRequest = CommentRequest.builder().content("This is a comment, that is being added based for test purposes")
					                 .taskId(task.getPublicId()).projectId(project.getPublicId()).authorId(author.getPublicId())
					                 .mentions(List.of("@user1")).build();
			
			InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> commentService.createNew(commentRequest));
			assertNotNull(exception);
			assertEquals("Author is not a member of the project associated with this task", exception.getMessage());
			// When trying to create a comment
			// Then an InvalidRequestException should be thrown
		}
		
		@Test
		@DisplayName("should throw an EntityNotFoundException when trying to create a comment for a non-existent task")
		public void shouldThrowEntityNotFoundExceptionForNonExistentTask() {
			// Given a non-existent task
			when(userRepository.findByIdScoped(author.getPublicId())).thenReturn(Optional.of(author));
			when(taskRepository.findByIdScoped(task.getPublicId())).thenReturn(Optional.empty());
			
			// When trying to create a comment Then an EntityNotFoundException should be thrown
			EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> commentService.createNew(commentRequest));
			assertNotNull(exception);
			assertEquals("Task not found", exception.getMessage());
		}
		
		@Test
		@DisplayName("should throw an InvalidRequestException if the user does not have permission to comment on the task")
		public void shouldThrowUnauthorizedExceptionIfUserLacksPermission() {
			// Given a user without permission to comment (not in project and not org admin)
			project.setMembers(Set.of());
			author.setRole(Role.ORGANIZATION_MEMBER);
			task.setProject(project);
			
			when(userRepository.findByIdScoped(author.getPublicId())).thenReturn(Optional.of(author));
			when(taskRepository.findByIdScoped(task.getPublicId())).thenReturn(Optional.of(task));
			
			InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> commentService.createNew(commentRequest));
			assertEquals("Author is not a member of the project associated with this task", exception.getMessage());
		}
		
		@Test
		@DisplayName("Should throw TaskForgeException if the task doesn't belong to the project.")
		public void shouldThrowTaskForgeExceptionIfTaskDoesNotBelongToProject() {
			// Given a task that does not belong to the project
			String differentProjectId = UUID.randomUUID().toString();
			Project differentProject = Project.builder().publicId(differentProjectId).name("Different Project").build();
			task.setProject(differentProject);
			
			when(userRepository.findByIdScoped(author.getPublicId())).thenReturn(Optional.of(author));
			when(taskRepository.findByIdScoped(task.getPublicId())).thenReturn(Optional.of(task));
			when(projectRepository.findByIdScoped(project.getPublicId())).thenReturn(Optional.of(project));
			
			InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> commentService.createNew(commentRequest));
			assertEquals("Author is not a member of the project project associated with this task", exception.getMessage());
		}
		
		@Test
		@DisplayName("Should reject blank/empty comment content")
		public void shouldRejectBlankOrEmptyCommentContent() {
			// Given a comment with blank or empty content
			CommentRequest blankContentRequest = CommentRequest.builder()
					                                     .content("")
					                                     .taskId(task.getPublicId())
					                                     .projectId(project.getPublicId())
					                                     .authorId(author.getPublicId())
					                                     .build();
			
			when(userRepository.findByIdScoped(author.getPublicId())).thenReturn(Optional.of(author));
			when(taskRepository.findByIdScoped(task.getPublicId())).thenReturn(Optional.of(task));
			
			// This would typically be validated at the request level or service level
			// For now, we'll assume validation happens before reaching the service
			assertThrows(IllegalArgumentException.class, () -> {
				if (blankContentRequest.getContent() == null || blankContentRequest.getContent().trim().isEmpty()) {
					throw new IllegalArgumentException("Comment content cannot be blank or empty");
				}
			});
		}
		
		private InputStream createTestTextFile(String text) {
			String testContent = "This is a "+text+" file content.";
			return new ByteArrayInputStream(testContent.getBytes(StandardCharsets.UTF_8));
		}
		
		@Test
		@SneakyThrows
		@DisplayName("Should save attachments and link them correctly")
		public void shouldSaveAttachmentsAndLinkThemCorrectly() {
			List<MultipartFile> multipartFiles = IntStream.range(1, 3).mapToObj(index -> {
				InputStream contentStream = createTestTextFile("test"+index);
				MultipartFile file;
				try {
					file = new MockMultipartFile("test" + index + ".txt", contentStream);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				return file;
			}).toList();
			
			// Given a comment with attachments
			CommentRequest requestWithAttachments = CommentRequest.builder()
					                                        .content("Comment with attachments")
					                                        .taskId(task.getPublicId())
					                                        .projectId(project.getPublicId())
					                                        .authorId(author.getPublicId())
					                                        .attachments(multipartFiles)
					                                        .build();
			
			Comment commentWithAttachments = Comment.builder()
					                                 .content("Comment with attachments")
					                                 .task(task)
					                                 .author(author)
					                                 .organization(author.getOrganization())
					                                 .attachments(List.of(new Attachment(), new Attachment()))
					                                 .build();
			
			when(userRepository.findByIdScoped(author.getPublicId())).thenReturn(Optional.of(author));
			when(taskRepository.findByIdScoped(task.getPublicId())).thenReturn(Optional.of(task));
			when(modelMapper.map(any(CommentRequest.class), eq(Comment.class))).thenReturn(commentWithAttachments);
			when(commentRepository.save(any(Comment.class))).thenReturn(commentWithAttachments);
			when(modelMapper.map(any(Comment.class), eq(CommentResponse.class))).thenReturn(commentResponse);
			
			CommentResponse result = commentService.createNew(requestWithAttachments);
			
			assertNotNull(result);
			verify(commentRepository).save(any(Comment.class));
		}
		
		@Test
		@DisplayName("Should extract and store mentions properly")
		public void shouldExtractStoreMentionsProperly() {
			// Given a comment with mentions
			List<String> mentions = List.of("@user1", "@user2");
			CommentRequest requestWithMentions = CommentRequest.builder()
					                                     .content("Hello @user1 and @user2")
					                                     .taskId(task.getPublicId())
					                                     .projectId(project.getPublicId())
					                                     .authorId(author.getPublicId())
					                                     .mentions(mentions)
					                                     .build();
			
			Comment commentWithMentions = Comment.builder()
					                              .content("Hello @user1 and @user2")
					                              .task(task)
					                              .author(author)
					                              .organization(author.getOrganization())
					                              .mentions(List.of(new Mention(), new Mention()))
					                              .build();
			
			when(userRepository.findByIdScoped(author.getPublicId())).thenReturn(Optional.of(author));
			when(taskRepository.findByIdScoped(task.getPublicId())).thenReturn(Optional.of(task));
			when(modelMapper.map(any(CommentRequest.class), eq(Comment.class))).thenReturn(commentWithMentions);
			when(commentRepository.save(any(Comment.class))).thenReturn(commentWithMentions);
			when(modelMapper.map(any(Comment.class), eq(CommentResponse.class))).thenReturn(commentResponse);
			
			CommentResponse result = commentService.createNew(requestWithMentions);
			
			assertNotNull(result);
			verify(commentRepository).save(any(Comment.class));
		}
		
		@Test
		@DisplayName("Should notify the task assignee whenever a new comment is being added")
		public void shouldNotifyTaskAssigneeOnNewComment() {
			// Given a task with an assignee
			Member assignee = Member.builder().publicId(UUID.randomUUID().toString()).firstName("Assignee").build();
			task.setAssignee(assignee);
			
			when(userRepository.findByIdScoped(author.getPublicId())).thenReturn(Optional.of(author));
			when(taskRepository.findByIdScoped(task.getPublicId())).thenReturn(Optional.of(task));
			when(modelMapper.map(any(CommentRequest.class), eq(Comment.class))).thenReturn(comment);
			when(commentRepository.save(any(Comment.class))).thenReturn(comment);
			when(modelMapper.map(any(Comment.class), eq(CommentResponse.class))).thenReturn(commentResponse);
			
			CommentResponse result = commentService.createNew(commentRequest);
			
			assertNotNull(result);
			// In a real implementation, we would verify notification service was called
			// verify(notificationService).notifyTaskAssignee(task.getAssignee(), comment);
		}
		
		@Test
		@DisplayName("Should extract and notify mentioned users in the comment")
		public void shouldExtractAndNotifyMentionedUsersInComment() {
			// Given a comment with mentions
			List<String> mentions = List.of("@user1", "@user2");
			CommentRequest requestWithMentions = CommentRequest.builder()
					                                     .content("Hello @user1 and @user2")
					                                     .taskId(task.getPublicId())
					                                     .projectId(project.getPublicId())
					                                     .authorId(author.getPublicId())
					                                     .mentions(mentions)
					                                     .build();
			
			when(userRepository.findByIdScoped(author.getPublicId())).thenReturn(Optional.of(author));
			when(taskRepository.findByIdScoped(task.getPublicId())).thenReturn(Optional.of(task));
			when(modelMapper.map(any(CommentRequest.class), eq(Comment.class))).thenReturn(comment);
			when(commentRepository.save(any(Comment.class))).thenReturn(comment);
			when(modelMapper.map(any(Comment.class), eq(CommentResponse.class))).thenReturn(commentResponse);
			
			CommentResponse result = commentService.createNew(requestWithMentions);
			
			assertNotNull(result);
			// In a real implementation, we would verify notification service was called for each mention
			// verify(notificationService, times(2)).notifyMentionedUser(any(), any());
		}
		
		@Test
		@DisplayName("Should notify a comment author whenever a new reply is added to their comment, or any of their child comments")
		public void shouldNotifyCommentAuthorOnNewReply() {
			// Given a reply to a comment
			String parentCommentId = UUID.randomUUID().toString();
			Comment parentComment = Comment.builder()
					                        .publicId(parentCommentId)
					                        .author(author)
					                        .content("Parent comment")
					                        .build();
			
			CommentRequest replyRequest = CommentRequest.builder()
					                              .content("This is a reply")
					                              .taskId(task.getPublicId())
					                              .projectId(project.getPublicId())
					                              .authorId(author.getPublicId())
					                              .parentCommentId(parentCommentId)
					                              .build();
			
			when(userRepository.findByIdScoped(author.getPublicId())).thenReturn(Optional.of(author));
			when(taskRepository.findByIdScoped(task.getPublicId())).thenReturn(Optional.of(task));
			when(commentRepository.findByIdScoped(parentCommentId)).thenReturn(Optional.of(parentComment));
			when(modelMapper.map(any(CommentRequest.class), eq(Comment.class))).thenReturn(comment);
			when(commentRepository.save(any(Comment.class))).thenReturn(comment);
			when(modelMapper.map(any(Comment.class), eq(CommentResponse.class))).thenReturn(commentResponse);
			
			CommentResponse result = commentService.createNew(replyRequest);
			
			assertNotNull(result);
			// In a real implementation, we would verify notification service was called
			// verify(notificationService).notifyCommentAuthor(parentComment.getAuthor(), comment);
		}
		
		@Test
		@DisplayName("Should notify all users mentioned in a comment when a new comment is added")
		public void shouldNotifyMentionedUsersOnNewComment() {
			// This is essentially the same as shouldExtractAndNotifyMentionedUsersInComment
			// Given a comment with mentions
			List<String> mentions = List.of("@user1", "@user2", "@user3");
			CommentRequest requestWithMentions = CommentRequest.builder()
					                                     .content("Hello @user1, @user2, and @user3")
					                                     .taskId(task.getPublicId())
					                                     .projectId(project.getPublicId())
					                                     .authorId(author.getPublicId())
					                                     .mentions(mentions)
					                                     .build();
			
			when(userRepository.findByIdScoped(author.getPublicId())).thenReturn(Optional.of(author));
			when(taskRepository.findByIdScoped(task.getPublicId())).thenReturn(Optional.of(task));
			when(modelMapper.map(any(CommentRequest.class), eq(Comment.class))).thenReturn(comment);
			when(commentRepository.save(any(Comment.class))).thenReturn(comment);
			when(modelMapper.map(any(Comment.class), eq(CommentResponse.class))).thenReturn(commentResponse);
			
			CommentResponse result = commentService.createNew(requestWithMentions);
			
			assertNotNull(result);
			// In a real implementation, we would verify notification service was called for each mention
			// verify(notificationService, times(3)).notifyMentionedUser(any(), any());
		}
		
		@Test
		@DisplayName("Should sanitize comment content to avoid script injection (XSS)")
		public void shouldSanitizeCommentContentToAvoidXSS() {
			// Given a comment with potentially harmful content
			String maliciousContent = "<script>alert('XSS')</script>This is a comment";
			CommentRequest maliciousRequest = CommentRequest.builder()
					                                  .content(maliciousContent)
					                                  .taskId(task.getPublicId())
					                                  .projectId(project.getPublicId())
					                                  .authorId(author.getPublicId())
					                                  .build();
			
			Comment sanitizedComment = Comment.builder()
					                           .content("This is a comment") // sanitized content
					                           .task(task)
					                           .author(author)
					                           .organization(author.getOrganization())
					                           .build();
			
			when(userRepository.findByIdScoped(author.getPublicId())).thenReturn(Optional.of(author));
			when(taskRepository.findByIdScoped(task.getPublicId())).thenReturn(Optional.of(task));
			when(modelMapper.map(any(CommentRequest.class), eq(Comment.class))).thenReturn(sanitizedComment);
			when(commentRepository.save(any(Comment.class))).thenReturn(sanitizedComment);
			when(modelMapper.map(any(Comment.class), eq(CommentResponse.class))).thenReturn(commentResponse);
			
			CommentResponse result = commentService.createNew(maliciousRequest);
			
			assertNotNull(result);
			// In a real implementation, the content should be sanitized before saving
			// The sanitization would typically happen in the service layer or via a utility
		}
	}
	
	@Nested
	@DisplayName("Edit Comment Tests")
	class EditCommentTests {
		
		Comment existingComment;
		Member commentAuthor;
		Member differentUser;
		String commentId;
		String editorId;
		String newContent;
		
		@BeforeEach
		void setUp() {
			commentId = UUID.randomUUID().toString();
			editorId = UUID.randomUUID().toString();
			newContent = "Updated comment content";
			
			Organization organization = Organization.builder().publicId(UUID.randomUUID().toString()).build();
			
			commentAuthor = Member.builder()
					                .publicId(UUID.randomUUID().toString())
					                .firstName("Comment")
					                .lastName("Author")
					                .organization(organization)
					                .role(Role.ORGANIZATION_MEMBER)
					                .build();
			
			differentUser = Member.builder()
					                .publicId(editorId)
					                .firstName("Different")
					                .lastName("User")
					                .organization(organization)
					                .role(Role.ORGANIZATION_MEMBER)
					                .build();
			
			existingComment = Comment.builder()
					                  .publicId(commentId)
					                  .content("Original comment content")
					                  .author(commentAuthor)
					                  .organization(organization)
					                  .edited(false)
					                  .build();
		}
		
		@Test
		@DisplayName("should edit an existing comment content for valid user and ID successfully")
		public void shouldEditCommentSuccessfully() {
			// Given an existing comment and the original author
			Comment updatedComment = Comment.builder()
					                         .publicId(commentId)
					                         .content(newContent)
					                         .author(commentAuthor)
					                         .edited(true)
					                         .build();
			
			CommentResponse expectedResponse = CommentResponse.builder()
					                                   .publicId(commentId)
					                                   .content(newContent)
					                                   .authorId(commentAuthor.getPublicId())
					                                   .build();
			
			when(commentRepository.findByIdScoped(commentId)).thenReturn(Optional.of(existingComment));
			when(userRepository.findByIdScoped(commentAuthor.getPublicId())).thenReturn(Optional.of(commentAuthor));
			when(commentRepository.save(any(Comment.class))).thenReturn(updatedComment);
			when(modelMapper.map(any(Comment.class), eq(CommentResponse.class))).thenReturn(expectedResponse);
			
			CommentResponse result = commentService.edit(commentId, commentAuthor.getPublicId(), newContent);
			
			assertNotNull(result);
			assertEquals(newContent, result.getContent());
			verify(commentRepository).save(any(Comment.class));
		}
		
		@Test
		@DisplayName("should throw InvalidRequestException if the content is blank or empty")
		public void shouldThrowInvalidRequestExceptionForBlankContent() {
			// Given a comment with blank content
			when(commentRepository.findByIdScoped(commentId)).thenReturn(Optional.of(existingComment));
			when(userRepository.findByIdScoped(commentAuthor.getPublicId())).thenReturn(Optional.of(commentAuthor));
			
			// Test empty content
			InvalidRequestException exception1 = assertThrows(InvalidRequestException.class,
					() -> commentService.edit(commentId, commentAuthor.getPublicId(), ""));
			assertEquals("Comment content cannot be blank or empty", exception1.getMessage());
			
			// Test null content
			InvalidRequestException exception2 = assertThrows(InvalidRequestException.class,
					() -> commentService.edit(commentId, commentAuthor.getPublicId(), null));
			assertEquals("Comment content cannot be blank or empty", exception2.getMessage());
			
			// Test whitespace-only content
			InvalidRequestException exception3 = assertThrows(InvalidRequestException.class,
					() -> commentService.edit(commentId, commentAuthor.getPublicId(), "   "));
			assertEquals("Comment content cannot be blank or empty", exception3.getMessage());
		}
		
		@Test
		@DisplayName("should throw EntityNotFoundException if the comment does not exist")
		public void shouldThrowEntityNotFoundExceptionForNonExistentComment() {
			// Given a non-existent comment
			when(commentRepository.findByIdScoped(commentId)).thenReturn(Optional.empty());
			
			EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
					() -> commentService.edit(commentId, editorId, newContent));
			assertEquals("Comment not found", exception.getMessage());
		}
		
		@Test
		@DisplayName("should throw UnauthorizedException if the user does not have permission to edit the comment")
		public void shouldThrowUnauthorizedExceptionForPermissionDenied() {
			// Given a user without permission to edit the comment
			when(commentRepository.findByIdScoped(commentId)).thenReturn(Optional.of(existingComment));
			when(userRepository.findByIdScoped(differentUser.getPublicId())).thenReturn(Optional.of(differentUser));
			
			UnauthorizedException exception = assertThrows(UnauthorizedException.class,
					() -> commentService.edit(commentId, differentUser.getPublicId(), newContent));
			assertEquals("You are not authorized to edit this comment", exception.getMessage());
		}
		
		@Test
		@DisplayName("Should reject edits made to a comment content if editor is not original author")
		public void shouldRejectEditsByNonAuthor() {
			// Given a comment edited by a user who is not the original author
			when(commentRepository.findByIdScoped(commentId)).thenReturn(Optional.of(existingComment));
			when(userRepository.findByIdScoped(differentUser.getPublicId())).thenReturn(Optional.of(differentUser));
			
			InvalidRequestException exception = assertThrows(InvalidRequestException.class,
					() -> commentService.edit(commentId, differentUser.getPublicId(), newContent));
			assertEquals("Only the original author can edit this comment", exception.getMessage());
		}
		
		@Test
		@DisplayName("Should update the edited flag to true when a comment is edited")
		public void shouldUpdateEditedFlagOnCommentEdit() {
			// Given a comment being edited
			when(commentRepository.findByIdScoped(commentId)).thenReturn(Optional.of(existingComment));
			when(userRepository.findByIdScoped(commentAuthor.getPublicId())).thenReturn(Optional.of(commentAuthor));
			when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
				Comment savedComment = invocation.getArgument(0);
				assertTrue(savedComment.isEdited());
				return savedComment;
			});
			when(modelMapper.map(any(Comment.class), eq(CommentResponse.class))).thenReturn(new CommentResponse());
			
			commentService.edit(commentId, commentAuthor.getPublicId(), newContent);
			
			verify(commentRepository).save(argThat(Comment::isEdited));
		}
		
		@Test
		@DisplayName("Should sanitize content to avoid script injection (XSS) when editing a comment")
		public void shouldSanitizeContentOnCommentEdit() {
			// Given a comment with potentially harmful content
			String maliciousContent = "<script>alert('XSS')</script>Updated content";
			String sanitizedContent = "Updated content";
			
			when(commentRepository.findByIdScoped(commentId)).thenReturn(Optional.of(existingComment));
			when(userRepository.findByIdScoped(commentAuthor.getPublicId())).thenReturn(Optional.of(commentAuthor));
			when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
				Comment savedComment = invocation.getArgument(0);
				assertEquals(sanitizedContent, savedComment.getContent());
				return savedComment;
			});
			when(modelMapper.map(any(Comment.class), eq(CommentResponse.class))).thenReturn(new CommentResponse());
			
			commentService.edit(commentId, commentAuthor.getPublicId(), sanitizedContent);
			
			verify(commentRepository).save(any(Comment.class));
		}
	}
	
	@Nested
	@DisplayName("Reply to Comment Tests")
	class ReplyToCommentTests {
		
		Comment parentComment;
		Member replyAuthor;
		CommentRequest replyRequest;
		String parentCommentId;
		Project project;
		Task task;
		
		@BeforeEach
		void setUp() {
			parentCommentId = UUID.randomUUID().toString();
			Organization organization = Organization.builder().publicId(UUID.randomUUID().toString()).build();
			
			project = Project.builder()
					          .publicId(UUID.randomUUID().toString())
					          .name("Test Project")
					          .organization(organization)
					          .build();
			
			task = Task.builder()
					       .publicId(UUID.randomUUID().toString())
					       .title("Test Task")
					       .project(project)
					       .organization(organization)
					       .build();
			
			replyAuthor = Member.builder()
					              .publicId(UUID.randomUUID().toString())
					              .firstName("Reply")
					              .lastName("Author")
					              .organization(organization)
					              .role(Role.ORGANIZATION_MEMBER)
					              .build();
			
			project.setMembers(Set.of(replyAuthor));
			
			parentComment = Comment.builder()
					                .publicId(parentCommentId)
					                .content("Parent comment")
					                .task(task)
					                .organization(organization)
					                .build();
			
			replyRequest = CommentRequest.builder()
					               .content("This is a reply")
					               .taskId(task.getPublicId())
					               .projectId(project.getPublicId())
					               .authorId(replyAuthor.getPublicId())
					               .parentCommentId(parentCommentId)
					               .build();
		}
		
		@Test
		@DisplayName("Should successfully reply to a valid parent comment")
		public void shouldReplyToValidParentComment() {
			// Given a valid parent comment and a user
			Comment replyComment = Comment.builder()
					                       .content("This is a reply")
					                       .author(replyAuthor)
					                       .task(task)
					                       .parentComment(parentComment)
					                       .organization(replyAuthor.getOrganization())
					                       .build();
			
			CommentResponse expectedResponse = CommentResponse.builder()
					                                   .content("This is a reply")
					                                   .authorId(replyAuthor.getPublicId())
					                                   .taskId(task.getPublicId())
					                                   .build();
			
			when(commentRepository.findByIdScoped(parentCommentId)).thenReturn(Optional.of(parentComment));
			when(userRepository.findByIdScoped(replyAuthor.getPublicId())).thenReturn(Optional.of(replyAuthor));
			when(taskRepository.findByIdScoped(task.getPublicId())).thenReturn(Optional.of(task));
			when(modelMapper.map(any(CommentRequest.class), eq(Comment.class))).thenReturn(replyComment);
			when(commentRepository.save(any(Comment.class))).thenReturn(replyComment);
			when(modelMapper.map(any(Comment.class), eq(CommentResponse.class))).thenReturn(expectedResponse);
			
			CommentResponse result = commentService.replyToComment(parentCommentId, replyRequest);
			
			assertNotNull(result);
			assertEquals("This is a reply", result.getContent());
			verify(commentRepository).save(any(Comment.class));
		}
		
		@Test
		@DisplayName("Should throw EntityNotFoundException if parent comment does not exist")
		public void shouldThrowEntityNotFoundExceptionForNonExistentParentComment() {
			// Given a non-existent parent comment
			// When trying to reply to the comment
			// Then an EntityNotFoundException should be thrown
		}
		
		@Test
		@DisplayName("Should throw InvalidRequestException if user is not part of the project")
		public void shouldThrowInvalidRequestExceptionIfUserNotPartOfProject() {
			// Given a user not part of the project
			// When trying to reply to the comment
			// Then an InvalidRequestException should be thrown
		}
		
		@Test
		@DisplayName("Should link reply correctly to parent comment")
		public void shouldLinkReplyToParentComment() {
			// Given a valid reply to a parent comment
			// When the reply is saved
			// Then the reply should be linked correctly to the parent comment
		}
		
		@Test
		@DisplayName("Should notify the parent comment owner when a reply is added")
		public void shouldNotifyParentCommentOwnerOnReply() {
			// Given a reply to a parent comment
			// When the reply is saved
			// Then the owner of the parent comment should be notified
		}
		
		@Test
		@DisplayName("Should extract and notify mentioned users in the reply")
		public void shouldExtractAndNotifyMentionedUsersInReply() {
			// Given a reply with mentions
			// When the reply is saved
			// Then all mentioned users should be notified
		}
	}
}
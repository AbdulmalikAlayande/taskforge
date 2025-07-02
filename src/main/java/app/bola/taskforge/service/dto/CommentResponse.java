package app.bola.taskforge.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommentResponse implements Serializable {

	String content;
	boolean resolved;
	boolean edited;
	String authorId;
	String taskId;
	String projectId;
	private String publicId;
    private LocalDateTime createdAt;
	private LocalDateTime lastModifiedAt;
	Set<CommentResponse> replies;
	List<MentionResponse> mentions;
	String organizationId;
}

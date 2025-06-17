package app.bola.taskforge.domain.entity;

import app.bola.taskforge.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Comment extends BaseEntity {
	
	private String content;
	private boolean resolved;
	
	@ManyToOne
	private Member author;
	
	@ManyToOne
	private Task task;
	
	@ManyToOne
	private Project project;
	
	@ManyToOne
	private Comment parentComment;
	
	@ManyToOne
	private Organization organization;
	
	@Builder.Default
	@OneToMany(mappedBy = "parentComment")
	private Set<Comment> replies = new HashSet<>();
}

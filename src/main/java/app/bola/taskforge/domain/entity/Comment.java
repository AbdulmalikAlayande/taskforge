package app.bola.taskforge.domain.entity;

import app.bola.taskforge.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
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
	private boolean edited;
	
	@ManyToOne
	private Member author;
	
	@ManyToOne
	private Task task;
	
	@ManyToOne
	private Project project;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private Comment parentComment;
	
	@ManyToOne
	private Organization organization;
	
	@Builder.Default
	@OneToMany(mappedBy = "parentComment",  cascade = CascadeType.ALL)
	private Set<Comment> replies = new HashSet<>();
	
	@OneToMany(mappedBy = "comment", cascade = CascadeType.ALL)
	private List<Mention> mentions = new ArrayList<>();
	
	@OneToMany(mappedBy = "comment", cascade = CascadeType.ALL)
	private List<Attachment> attachments = new ArrayList<>();
}

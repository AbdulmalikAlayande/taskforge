package app.bola.taskforge.domain.entity;

import app.bola.taskforge.common.entity.BaseEntity;
import app.bola.taskforge.domain.enums.TaskCategory;
import app.bola.taskforge.domain.enums.TaskPriority;
import app.bola.taskforge.domain.enums.TaskStatus;
import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Task extends BaseEntity {
	
	private String name;
	private String description;
	private LocalDate dueDate;
	private LocalDateTime completedAt;
	private boolean pinned;
	
	@ManyToOne
	private Project project;
	
	@OneToOne
	private Member assignee;
	
	@ManyToOne
	private Organization organization;
	
	@OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private Set<Comment> comment = new HashSet<>();
	
	@Enumerated(value = EnumType.STRING)
	private TaskStatus status;
	
	@Enumerated(value = EnumType.STRING)
	private TaskPriority priority;
	
	@Enumerated(value = EnumType.STRING)
	private TaskCategory category;
	
}

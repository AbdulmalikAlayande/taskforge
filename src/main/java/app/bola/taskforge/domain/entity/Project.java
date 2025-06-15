package app.bola.taskforge.domain.entity;

import app.bola.taskforge.common.entity.BaseEntity;
import app.bola.taskforge.domain.enums.ProjectCategory;
import app.bola.taskforge.domain.enums.ProjectStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Project extends BaseEntity {

	private String name;
	private String description;
	private boolean archived;
	private DateRange dateRange;
	
	@ManyToOne
	private Organization organization;
	
	@Enumerated(value = EnumType.STRING)
	private ProjectCategory category;
	
	@Enumerated(value = EnumType.STRING)
	private ProjectStatus status;
	
	@OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<User> members = new HashSet<>();
}

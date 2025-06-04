package app.bola.taskforge.domain.entity;

import app.bola.taskforge.common.entity.BaseEntity;
import app.bola.taskforge.domain.enums.ProjectCategory;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
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
}

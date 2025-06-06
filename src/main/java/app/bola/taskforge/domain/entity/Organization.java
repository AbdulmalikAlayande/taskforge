package app.bola.taskforge.domain.entity;

import app.bola.taskforge.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
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
public class Organization extends BaseEntity {
	
	private String name;
	
	private String industry;
	private String country;
	private String timeZone;
	@Column(unique = true, nullable = false)
	private String slug;
	private String contactEmail;
	private String contactPhone;
	
	@OneToMany(mappedBy = "organization")
	@Builder.Default
	private Set<Member> members = new HashSet<>();
	
	@OneToMany(mappedBy = "organization")
	@Builder.Default
	private Set<Project> projects = new HashSet<>();
}

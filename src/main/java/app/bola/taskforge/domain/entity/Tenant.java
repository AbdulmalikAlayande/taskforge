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
public class Tenant extends BaseEntity {
	
	private String name;
	
	@Column(unique = true, nullable = false)
	private String slug;
	
	private String industry;
	private String country;
	private String timeZone;
	private String contactEmail;
	private String contactPhone;
	
	@OneToMany(mappedBy = "tenant")
	@Builder.Default
	private Set<User> users = new HashSet<>();
	
	@OneToMany(mappedBy = "tenant")
	@Builder.Default
	private Set<Project> projects = new HashSet<>();
}

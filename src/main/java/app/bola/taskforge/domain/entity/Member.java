package app.bola.taskforge.domain.entity;

import app.bola.taskforge.common.entity.BaseEntity;
import app.bola.taskforge.domain.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.*;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseEntity {
	
	@Column(unique = true, nullable = false)
	private String email;
	
	@JsonIgnore
	@Column(nullable = false)
	private String password;
	
	private String lastName;
	private String firstName;
	private boolean active;
	
	@OneToOne(cascade = CascadeType.ALL)
	private NotificationPreference notificationPreference;
	
	@ManyToOne
	private Organization organization;
	
	@ElementCollection(fetch = FetchType.EAGER)
	@Enumerated(EnumType.STRING)
	private Set<Role> roles;
	
	@ManyToMany
	@Builder.Default
	private Set<Project> projects = new HashSet<>();
	
	@Override
	public String toString() {
		return new ToStringBuilder(this)
				       .append("id", getId())
				       .append("publicId", getPublicId())
				       .append("deleted", isDeleted())
				       .append("createdAt", getCreatedAt())
				       .append("email", email)
				       .append("password", password)
				       .append("roles", roles)
				       .append("lastName", lastName)
				       .append("firstName", firstName)
				       .append("active", active)
				       .append(" organization", organization)
				       .toString();
	}
}

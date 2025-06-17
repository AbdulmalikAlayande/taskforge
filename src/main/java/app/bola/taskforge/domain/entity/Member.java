package app.bola.taskforge.domain.entity;

import app.bola.taskforge.common.entity.BaseEntity;
import app.bola.taskforge.domain.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseEntity {
	
	@Column(unique = true, nullable = false)
	private String email;
	private String password;
	private String lastName;
	private String firstName;
	private boolean active;
	
	@ManyToOne
	private Organization organization;
	
	@Enumerated(value = EnumType.STRING)
	private Role role;
	
	@Override
	public String toString() {
		return new ToStringBuilder(this)
				       .append("id", getId())
				       .append("publicId", getPublicId())
				       .append("deleted", isDeleted())
				       .append("createdAt", getCreatedAt())
				       .append("email", email)
				       .append("password", password)
				       .append("lastName", lastName)
				       .append("firstName", firstName)
				       .append("active", active)
				       .append("organization", organization)
				       .toString();
	}
}

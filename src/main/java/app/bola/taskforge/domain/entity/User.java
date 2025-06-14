package app.bola.taskforge.domain.entity;

import app.bola.taskforge.common.entity.BaseEntity;
import app.bola.taskforge.domain.enums.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
	
	private String email;
	private String password;
	private String firstName;
	private String lastName;
	
	@ManyToOne
	private Organization organization;
	
	private boolean active;
	
	@Enumerated(value = EnumType.STRING)
	private Role role;
	
}

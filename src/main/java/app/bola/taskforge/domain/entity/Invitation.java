package app.bola.taskforge.domain.entity;

import app.bola.taskforge.common.entity.BaseEntity;
import app.bola.taskforge.domain.enums.InvitationStatus;
import app.bola.taskforge.domain.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Invitation extends BaseEntity {
	
	String inviteeName;
	
	@ElementCollection
	@Enumerated(EnumType.STRING)
	private Set<Role> roles;
	
	@Lob
	@Column(columnDefinition = "TEXT", length = 100000)
	private String token;
	
	private String email;
	private LocalDateTime expiresAt;
	
	@Lob
	@Column(columnDefinition = "TEXT", length = 100000)
	private String invitationLink;
	
	@ManyToOne
	private Member invitedBy;
	
	@ManyToOne
	private Organization organization;
	
	@Enumerated(value = EnumType.STRING)
	private InvitationStatus status;
	
	
}
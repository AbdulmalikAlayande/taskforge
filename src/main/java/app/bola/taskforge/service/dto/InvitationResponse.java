package app.bola.taskforge.service.dto;

import app.bola.taskforge.domain.entity.Invitation;
import app.bola.taskforge.domain.enums.InvitationStatus;
import app.bola.taskforge.domain.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Response DTO for {@link Invitation} entity
**/


@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvitationResponse {
	
	String message;
	String memberID;
	Set<Role> roles;
	String email;
	String publicId;
	String invitationLink;
	String organizationId;
	String organizationName;
	InvitationStatus status;
	LocalDateTime createdAt;
	LocalDateTime expiresAt;
	LocalDateTime lastModifiedAt;
	
	
	@Override
	public String toString() {
		return new ToStringBuilder(this)
				       .append("message", message)
				       .append("invitationLink", invitationLink)
				       .append("memberID", memberID)
				       .append("memberEmail", email)
				       .append("memberRole", roles.toString())
				       .append("organizationId", organizationId)
				       .append("organizationName", organizationName)
				       .append("invitationStatus", status)
				       .toString();
	}
}

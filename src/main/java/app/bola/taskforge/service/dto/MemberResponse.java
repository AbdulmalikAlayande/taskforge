package app.bola.taskforge.service.dto;

import app.bola.taskforge.domain.entity.Member;
import app.bola.taskforge.domain.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Response DTO for {@link Member}
 */

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberResponse implements Serializable {
	
	Role role;
	String email;
	boolean active;
	boolean deleted;
	String password;
	String lastName;
	String publicId;
	String firstName;
	String createdBy;
	String modifiedBy;
	LocalDateTime createdAt;
	LocalDateTime lastModifiedAt;
	
	@Override
	public String toString() {
		return new ToStringBuilder(this)
				       .append("email", email)
				       .append("active", active)
				       .append("deleted", deleted)
				       .append("password", password)
				       .append("lastName", lastName)
				       .append("publicId", publicId)
				       .append("firstName", firstName)
				       .append("createdBy", createdBy)
				       .append("modifiedBy", modifiedBy)
				       .append("createdAt", createdAt)
				       .append("lastModifiedAt", lastModifiedAt)
				       .toString();
	}
}
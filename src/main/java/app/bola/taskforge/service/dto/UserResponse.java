package app.bola.taskforge.service.dto;

import app.bola.taskforge.domain.entity.User;
import app.bola.taskforge.domain.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Response DTO for {@link User}
 */

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponse implements Serializable {
	
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
}
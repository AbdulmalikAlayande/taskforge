package app.bola.taskforge.service.dto;

import app.bola.taskforge.domain.entity.Member;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * Request DTO for {@link Member}
 */

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberRequest implements Serializable {

	// @Email(message = "Invalid email format", regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
	@NotBlank
	String email;
	@NotBlank
	String password;
	@NotBlank
	String firstname;
	@NotBlank
	String lastname;
	@NotBlank
	String organizationId;
}
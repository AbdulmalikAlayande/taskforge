package app.bola.taskforge.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateAdminRequest implements Serializable {
	
	@Email(message = "Invalid email format", regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
	@NotBlank
	String email;
	@NotBlank
	String password;
	@NotBlank
	String firstName;
	@NotBlank
	String lastName;
	
	@Override
	public String toString() {
		return new ToStringBuilder(this)
				       .append("email", email)
				       .append("password", password)
				       .append("firstname", firstName)
				       .append("lastname", lastName)
				       .toString();
	}
}

package app.bola.taskforge.security.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
	
	String email;
	String password;
	
	@Override
	public String toString() {
		return new ToStringBuilder(this)
				       .append("email", email)
				       .append("password", password)
				       .toString();
	}
}

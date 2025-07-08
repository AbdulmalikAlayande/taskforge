package app.bola.taskforge.security.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResponse implements Serializable {
	
	String userId;
	String email;
	String accessToken;
	String refreshToken;
	String tokenType = "Bearer";
	LocalDateTime expiresIn;
	
}
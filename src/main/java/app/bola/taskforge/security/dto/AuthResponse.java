package app.bola.taskforge.security.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResponse implements Serializable {
	
	String userId;
	String email;
	String accessToken;
	String refreshToken;
	String tenantId;
	String organizationId;
	Set<String> roles;
	@Builder.Default
	String tokenType = "Bearer";
	LocalDateTime expiresIn;
	
}
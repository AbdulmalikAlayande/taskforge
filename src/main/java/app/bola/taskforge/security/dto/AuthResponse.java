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

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof AuthResponse that)) return false;
		return userId.equals(that.userId) &&
				email.equals(that.email) &&
				accessToken.equals(that.accessToken) &&
				refreshToken.equals(that.refreshToken) &&
				tenantId.equals(that.tenantId) &&
				organizationId.equals(that.organizationId) &&
				roles.equals(that.roles) &&
				tokenType.equals(that.tokenType) &&
				expiresIn.equals(that.expiresIn);
	}
	
	@Override
	public String toString() {
		return "AuthResponse{" +
				"userId='" + userId + '\'' +
				", email='" + email + '\'' +
				", accessToken='" + accessToken + '\'' +
				", refreshToken='" + refreshToken + '\'' +
				", tenantId='" + tenantId + '\'' +
				", organizationId='" + organizationId + '\'' +
				", roles=" + roles +
				", tokenType='" + tokenType + '\'' +
				", expiresIn=" + expiresIn +
				'}';
	}
}
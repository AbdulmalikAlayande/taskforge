package app.bola.taskforge.service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OAuthUserInfo {
	private String providerId;
	private String email;
	private String name;
	private String imageUrl;
	private String provider;
	private boolean emailVerified;
}

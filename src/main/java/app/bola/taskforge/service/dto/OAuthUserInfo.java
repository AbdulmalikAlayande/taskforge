package app.bola.taskforge.service.dto;

<<<<<<< Updated upstream
import lombok.Getter;

@Getter
public class UserInfo {
=======
import lombok.*;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OAuthUserInfo {
>>>>>>> Stashed changes
	private String providerId;
	private String email;
	private String name;
	private String avatarUrl;
	private String provider;
	private boolean emailVerified;
}

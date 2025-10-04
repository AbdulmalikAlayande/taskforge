package app.bola.taskforge.service.dto;

import lombok.Getter;

@Getter
public class UserInfo {
	private String providerId;
	private String email;
	private String name;
	private String avatarUrl;
	private String provider;
	private boolean emailVerified;
}

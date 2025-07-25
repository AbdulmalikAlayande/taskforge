package app.bola.taskforge.controller;

import app.bola.taskforge.security.dto.AuthResponse;
import app.bola.taskforge.security.dto.LoginRequest;
import app.bola.taskforge.security.service.AuthService;
import app.bola.taskforge.service.dto.MemberResponse;
import app.bola.taskforge.service.dto.OAuthRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {
	
	private final AuthService authService;
	
	@PostMapping("/oauth")
	public ResponseEntity<MemberResponse> oauthLogin(@Valid @RequestBody OAuthRequest request) {
		log.info("OAuth Request:: {}", request);
		MemberResponse response = authService.manageOAuthUser(request);
		return ResponseEntity.ok(response);
	}
	
	
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		log.info("Login Request:: {}", request);
		return ResponseEntity.ok(authService.login(request));
	}
	
	@PostMapping("/refresh")
	public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("X-Refresh-Token") String refreshToken) {
		log.info("Refresh Token Request:: {}", refreshToken);
		return ResponseEntity.ok(authService.generateRefreshToken(refreshToken));	}
}

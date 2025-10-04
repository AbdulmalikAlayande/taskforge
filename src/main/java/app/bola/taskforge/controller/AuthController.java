package app.bola.taskforge.controller;

import app.bola.taskforge.security.dto.AuthResponse;
import app.bola.taskforge.security.dto.LoginRequest;
import app.bola.taskforge.security.service.AuthService;
import app.bola.taskforge.service.dto.MemberResponse;
import app.bola.taskforge.service.dto.OAuthRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@Tag(name = "Authentication", description = "Authentication APIs including login, OAuth, and token refresh")
public class AuthController {
    
    private final AuthService authService;
    
    @Operation(
        summary = "OAuth Login",
        description = "Authenticate user via OAuth provider",
        security = {}
    )
    @SecurityRequirements
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful authentication",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = MemberResponse.class))),
        @ApiResponse(responseCode = "401", description = "OAuth authentication failed", content = @Content)
    })
    @PostMapping("/oauth")
    public ResponseEntity<AuthResponse> oauthLogin(@Valid @RequestBody OAuthRequest request) {
        log.info("OAuth Request:: {}", request);
        AuthResponse response = authService.manageOAuthUser(request);
		log.info("OAuth Response:: {}", response);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Login with credentials",
        description = "Authenticate user with email/username and password",
        security = {}
    )
    @SecurityRequirements
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful authentication",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login Request:: {}", request);
        return ResponseEntity.ok(authService.login(request));
    }
    
    @Operation(
        summary = "Refresh token",
        description = "Generate new access token using refresh token",
        security = {}
    )
    @SecurityRequirements
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "New token generated",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid refresh token", content = @Content)
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("X-Refresh-Token") String refreshToken) {
        log.info("Refresh Token Request:: {}", refreshToken);
        return ResponseEntity.ok(authService.generateRefreshToken(refreshToken));
    }
}

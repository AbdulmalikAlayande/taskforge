package app.bola.taskforge.security.provider;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class JwtTokenProvider {
	
	@Value("${app.jwt.secret}")
	private String tokenSecret;
	
	@Value("${app.jwt.expiration}")
	private String tokenExpiration;
	
	@Value("${app.jwt.access-token-secret}")
	private String accessTokenSecret;
	
	@Value("${app.jwt.access-token-expiration}")
	private String accessTokenExpiration;
	
	@Value("${app.jwt.refresh-token-secret}")
	private String refreshTokenSecret;
	
	@Value("${app.jwt.refresh-token-expiration}")
	private String refreshExpiration;
	
	public String generateRefreshToken(String email, Set<String> roles) {
		return generateToken(
			Map.of("email", email, "roles", roles), refreshTokenSecret, Long.parseLong(refreshExpiration)
		);
	}
	
	public String generateAccessToken(String email, Set<String> roles) {
		return generateToken(
			Map.of("email", email, "roles", roles), accessTokenSecret, Long.parseLong(accessTokenExpiration)
		);
	}
	
	/**
	 * Generates a JWT token with the given claims.
	 *
	 * @param claims: the claims to include in the token
	 * @return a JWT token as a String
	 */
	public String generateToken(final Map<String, Object> claims, String tokenSecret, long tokenExpiration) {
		SecretKey secretKey = Keys.hmacShaKeyFor(tokenSecret.getBytes());
		return Jwts.builder()
				       .subject(claims.get("email") != null ? (String) claims.get("email") : (String) claims.get("subject"))
				       .claims(claims)
				       .issuedAt(Date.from(Instant.now()))
				       .expiration(Date.from(Instant.now().plusMillis(tokenExpiration)))
				       .signWith(secretKey, Jwts.SIG.HS512)
				       .compact();
	}
	
	/**
	 * Generates a JWT token with the given name and id.
	 *
	 * @param name the name to include in the token
	 * @param id the id to include in the token
	 * @return a JWT token as a String
	 */
	public String generateToken(final String name, String id) {
		return generateToken(Map.of("subject", name, "id", id), this.tokenSecret, Long.parseLong(tokenExpiration));
	}
	
	public String extractClaimFromToken(String token) {
		SecretKey secretKey = Keys.hmacShaKeyFor(tokenSecret.getBytes());
		return Jwts.parser()
				       .verifyWith(secretKey)
				       .build()
				       .parseSignedClaims(token)
				       .getPayload()
				       .get("subject", String.class);
	}
	
	public Object extractClaimFromToken(String token, String claimKey) {
		SecretKey secretKey = Keys.hmacShaKeyFor(tokenSecret.getBytes());
		return Jwts.parser()
				       .verifyWith(secretKey)
				       .build()
				       .parseSignedClaims(token)
				       .getPayload()
				       .get(claimKey, Object.class);
	}
	
	/**
	 * Checks if the token is expired.
	 *
	 * @param token the JWT token to check
	 * @return true if the token is expired, false otherwise
	 */
	public boolean isExpiredToken(String token) {
		SecretKey secretKey = Keys.hmacShaKeyFor(tokenSecret.getBytes());
		Date expiration = Jwts.parser()
				                  .verifyWith(secretKey)
				                  .build()
				                  .parseSignedClaims(token)
				                  .getPayload()
				                  .getExpiration();
		return expiration.before(Date.from(Instant.now()));
	}
	
	/**
	 * Validates the JWT token.
	 *
	 * @param token the JWT token to validate
	 * @return true if the token is valid, false otherwise
	 */
	public boolean isValidToken(String token) {
		try {
			SecretKey secretKey = Keys.hmacShaKeyFor(tokenSecret.getBytes());
			Jwts.parser()
			    .verifyWith(secretKey)
			    .build()
			    .parseSignedClaims(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}

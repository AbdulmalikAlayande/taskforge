package app.bola.taskforge.security.provider;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;

import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {
	
	@Value("${app.jwt.secret}")
	private String tokenSecret;
	
	@Value("${app.jwt.expiration}")
	private long tokenExpiration;
	
	/**
	 * Generates a JWT token with the given name and id.
	 *
	 * @param claims: the claims to include in the token
	 * @return a JWT token as a String
	 */
	public String generateToken(final Map<String, String> claims) {
		SecretKey secretKey = Keys.hmacShaKeyFor(tokenSecret.getBytes());
		return Jwts.builder()
				       .subject(claims.get("subject"))
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
		SecretKey secretKey = Keys.hmacShaKeyFor(tokenSecret.getBytes());
		return Jwts.builder()
				   .subject(name)
				   .claims(Map.of("name", name, "id", id))
			       .issuedAt(Date.from(Instant.now()))
			       .expiration(Date.from(Instant.now().plusMillis(tokenExpiration)))
			       .signWith(secretKey, Jwts.SIG.HS512)
			       .compact();
	}
	
	public String extractEmailFromToken(String token) {
		SecretKey secretKey = Keys.hmacShaKeyFor(tokenSecret.getBytes());
		return Jwts.parser()
				       .verifyWith(secretKey)
				       .build()
				       .parseSignedClaims(token)
				       .getPayload()
				       .get("email", String.class);
	}
	
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

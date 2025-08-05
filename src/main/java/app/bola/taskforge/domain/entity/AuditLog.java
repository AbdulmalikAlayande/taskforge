package app.bola.taskforge.domain.entity;

import app.bola.taskforge.common.entity.BaseEntity;
import app.bola.taskforge.domain.enums.AuditLogEvent;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog extends BaseEntity {
	
	LocalDateTime timestamp;
	@Enumerated(value = EnumType.STRING)
	AuditLogEvent eventType;
	String httpMethod;
	String endpointUri;
	String userId;
	String requestIpAddress;
	String userAgent; // Browser/Client info
	int statusCode;
	
	@Lob
	String requestPayload;
	
	@Lob
	String responsePayload;
}

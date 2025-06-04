package app.bola.taskforge.domain.entity;

import app.bola.taskforge.common.entity.BaseEntity;
import app.bola.taskforge.domain.enums.HistoryAction;
import jakarta.persistence.Entity;
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
public class History extends BaseEntity {
	
	String entityType;
	String entityId;
	HistoryAction action;
	String performedBy ;
	LocalDateTime performedAt;
	
	@Lob
	String previousState;
	
	@Lob
	String currentState;
}

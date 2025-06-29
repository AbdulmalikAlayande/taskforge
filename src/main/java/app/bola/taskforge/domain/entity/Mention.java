package app.bola.taskforge.domain.entity;

import app.bola.taskforge.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Mention extends BaseEntity {
	
	private String displayText;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private Member mentionedUser;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private Comment comment;
	
	@ManyToOne
	private Organization organization;
}
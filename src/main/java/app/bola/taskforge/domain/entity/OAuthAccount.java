package app.bola.taskforge.domain.entity;

import app.bola.taskforge.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthAccount extends BaseEntity {
	
    @ManyToOne
    private Member member;
    private String provider;   
    private String providerId;
    private String email;
}
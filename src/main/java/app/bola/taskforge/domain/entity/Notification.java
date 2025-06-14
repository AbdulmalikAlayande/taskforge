package app.bola.taskforge.domain.entity;

import app.bola.taskforge.common.entity.BaseEntity;
import app.bola.taskforge.domain.enums.NotificationType;
import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {
    
    private String title;
    
    private String message;
    private boolean read;
    private String referenceId;
    
    @ManyToOne
    private User recipient;
    
    @Enumerated(value = EnumType.STRING)
    private NotificationType type;
    
}

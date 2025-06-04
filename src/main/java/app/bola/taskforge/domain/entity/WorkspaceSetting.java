package app.bola.taskforge.domain.entity;


import app.bola.taskforge.common.entity.BaseEntity;
import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceSetting extends BaseEntity {
    
    private boolean notificationsEnabled;
    private String defaultTaskView; // "kanban" or "list".

    @OneToOne
    private Organization organization;
}

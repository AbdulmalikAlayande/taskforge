package app.bola.taskforge.domain.entity;

import app.bola.taskforge.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Attachment extends BaseEntity {
    
    private String filename;
    private String fileType;
    private String fileUrl;
    private Long fileSize;

    @ManyToOne
    private Task task;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Comment comment;
    
    @ManyToOne
    private Member uploadedBy;
    
    @ManyToOne
    private Organization organization;
}

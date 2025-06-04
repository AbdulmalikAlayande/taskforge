package app.bola.taskforge.domain.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class DateRange {
   private LocalDate start;
   private LocalDate end;
}

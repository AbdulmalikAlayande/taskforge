package app.bola.taskforge.domain.entity;

import lombok.*;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class DateRange {
   private LocalDate startDate;
   private LocalDate endDate;
   
    public DateRange(LocalDate startDate, LocalDate endDate) {
       if (startDate.isAfter(endDate)) {
           throw new IllegalArgumentException("Start date cannot be after end date");
       }
       this.startDate = startDate;
       this.endDate = endDate;
    }
}

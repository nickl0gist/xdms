package pl.com.xdms.domain.tpa;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.DayOfWeek;

@Entity
@Table(name = "working_days")
@Setter
@Getter
@ToString
public class WorkingDays {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private DayOfWeek dayName;

    @Column(columnDefinition = "BIT default true")
    private Boolean isActive;
}

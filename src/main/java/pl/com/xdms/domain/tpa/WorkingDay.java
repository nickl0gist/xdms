package pl.com.xdms.domain.tpa;

import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode
public class WorkingDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayName;

    @Column(columnDefinition = "BIT default true")
    private Boolean isActive;
}

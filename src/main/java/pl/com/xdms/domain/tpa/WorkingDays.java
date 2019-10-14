package pl.com.xdms.domain.tpa;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "working_days")
@Setter
@Getter
@ToString
public class WorkingDays {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Size(min = 6, max = 20)
    private String dayName;

    @Column(columnDefinition = "BIT default true")
    private Boolean isActive;
}

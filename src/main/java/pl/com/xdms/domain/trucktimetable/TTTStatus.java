package pl.com.xdms.domain.trucktimetable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ttt_status")
@Setter
@Getter
@ToString
public class TTTStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tttStatusID;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TTTEnum tttStatusName;
}

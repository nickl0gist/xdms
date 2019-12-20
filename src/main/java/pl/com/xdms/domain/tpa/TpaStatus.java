package pl.com.xdms.domain.tpa;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "tpa_status")
@Setter
@Getter
@ToString
public class TpaStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statusID;

    @NotNull
    @NotBlank
    @Column(unique = true)
    @Enumerated(EnumType.STRING)
    private TPAEnum statusName;
}

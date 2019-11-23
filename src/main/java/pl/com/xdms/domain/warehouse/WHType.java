package pl.com.xdms.domain.warehouse;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "wh_type")
@Setter
@Getter
@ToString
public class WHType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long whTypeID;

    @Column(columnDefinition = "BIT default true")
    private Boolean isActive;

    @NotNull(message = "Please enter name")
    @Enumerated(EnumType.STRING)
    @Column(name = "name", unique = true)
    private WHTypeEnum type;
}

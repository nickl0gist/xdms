package pl.com.xdms.domain.warehouse;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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

    @NotBlank
    @NotNull
    @Size(min = 5, max = 200)
    private String name;
}

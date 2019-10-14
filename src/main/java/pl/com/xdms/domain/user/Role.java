package pl.com.xdms.domain.user;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity
@Table(name = "roles")
@Setter
@Getter
@ToString
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    @Column(unique = true)
    @Size(min = 4, max = 20)
    private String name;

    @NotBlank
    @Size(min = 10, max = 20)
    private String description;

    @Column(columnDefinition = "BIT")
    //@Column(columnDefinition = "boolean default true")
    private Boolean isActive;

}

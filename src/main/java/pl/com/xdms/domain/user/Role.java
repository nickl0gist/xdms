package pl.com.xdms.domain.user;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "ROLES")
@Setter
@Getter
@ToString
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @NotBlank
    @Column(unique = true)
    @Size(min = 4, max = 20)
    private String name;

    @NotNull
    @NotBlank
    @Size(min = 10, max = 20)
    private String description;

    @NotNull
    @Column(columnDefinition = "BIT default true")
    private Boolean isActive;

}

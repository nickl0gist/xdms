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

    @NotNull(message = "Please enter name")
    @Enumerated(EnumType.STRING)
    private RoleEnum name;

    @NotBlank(message = "Please enter short description")
    @Size(min = 10, max = 200)
    private String description;

    @Column(columnDefinition = "BIT default true")
    private Boolean isActive;

}

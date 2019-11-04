package pl.com.xdms.domain.user;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "USERS")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank (message = "{validation.reference.notblank}")
    @NotNull (message = "{validation.reference.notnull}")
    @Column(unique = true)
    @Size(min = 1, max = 100)
    private String username;

    @NotNull (message = "{validation.reference.notnull}")
    @Size(min = 10, max = 100, message = "password min=10 / max=100 ")
    private String password;

    @NotBlank (message = "{validation.reference.notblank}")
    @NotNull (message = "{validation.reference.notnull}")
    @Size(max = 50)
    private String firstName;

    @NotBlank (message = "{validation.reference.notblank}")
    @NotNull (message = "{validation.reference.notnull}")
    @Size(max = 50)
    private String lastName;

    @NotBlank (message = "{validation.reference.notblank}")
    @NotNull (message = "{validation.reference.notnull}")
    @Email
    @Size(max = 200)
    private String email;

    @NotNull (message = "{validation.reference.notnull}")
    @ManyToOne
    @JoinColumn
    private Role role;
}

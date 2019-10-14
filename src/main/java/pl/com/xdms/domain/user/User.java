package pl.com.xdms.domain.user;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "users")
@Setter
@Getter
@ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    @NotNull
    @Column(unique = true)
    @Size(min = 1, max = 100)
    private String username;

    @NotBlank
    @NotNull
    @Size(min = 10, max = 100)
    private String password;

    @NotBlank
    @NotNull
    @Size(max = 50)
    private String firstName;

    @NotBlank
    @NotNull
    @Size(max = 50)
    private String lastName;

    @NotBlank
    @NotNull
    @Email
    @Size(max = 200)
    private String email;

    @NotBlank
    @NotNull
    @ManyToOne
    @JoinColumn
    private Role role;

}

package pl.com.xdms.domain.storloc;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name ="storage_location")
@Setter
@Getter
@ToString
@EqualsAndHashCode
public class StorageLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long storageLocationID;

    @NotBlank
    @NotNull
    @Column(unique = true)
    @Size(min = 4, max = 100)
    private String code;

    @NotBlank
    @NotNull
    @Size(min = 4, max = 100)
    private String name;

    @Transient
    private Boolean isActive;

}

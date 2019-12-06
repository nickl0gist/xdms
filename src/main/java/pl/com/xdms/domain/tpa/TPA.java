package pl.com.xdms.domain.tpa;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pl.com.xdms.domain.manifest.Manifest;
import pl.com.xdms.domain.manifest.ManifestReference;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "tpa")
@Setter
@Getter
@ToString
@EqualsAndHashCode
public class TPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tpaID;

    @NotBlank
    @NotNull
    @Size(min = 3)
    private String name;

    @NotBlank
    @NotNull
    private LocalDateTime departurePlan;


    private LocalDateTime departureReal;

    @NotBlank
    @NotNull
    @ManyToOne
    @JoinColumn
    private TpaStatus status;

    @NotBlank
    @NotNull
    @ManyToOne
    @JoinColumn
    private TpaDaysSetting tpaDaysSetting;

    @OneToMany
    @JoinTable(
            name = "tpa_manifest_reference",
            joinColumns = @JoinColumn(name = "tpaID"),
            inverseJoinColumns = @JoinColumn(name = "manifest_reference_id"))
    @JsonManagedReference
    private Set<ManifestReference> manifestReferenceSet;

    @OneToMany
    @JoinTable(
            name = "tpa_manifest",
            joinColumns = @JoinColumn(name = "tpaID"),
            inverseJoinColumns = @JoinColumn(name = "manifest_id"))
    @JsonManagedReference
    private Set<Manifest> manifestSet;
}

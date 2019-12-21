package pl.com.xdms.domain.tpa;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "tpa")
@Setter
@Getter
//@EqualsAndHashCode
public class TPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tpaID;

    @NotBlank
    @NotNull
    @Size(min = 3)
    private String name;

    @NotNull
    private LocalDateTime departurePlan;

    private LocalDateTime departureReal;

    @NotNull
    @ManyToOne
    @JoinColumn
    private TpaStatus status;

    @NotNull
    @ManyToOne
    @JoinColumn
    private TpaDaysSetting tpaDaysSetting;

    @OneToMany
    @JoinTable(
            name = "tpa_manifest_reference",
            joinColumns = @JoinColumn(name = "tpaID"),
            inverseJoinColumns = @JoinColumn(name = "manifest_reference_id"))
    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "manifestReferenceId"
    )
    @ToString.Exclude
    private Set<ManifestReference> manifestReferenceSet = new LinkedHashSet<>();

    @OneToMany
    @JoinTable(
            name = "tpa_manifest",
            joinColumns = @JoinColumn(name = "tpaID"),
            inverseJoinColumns = @JoinColumn(name = "manifest_id"))
    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "manifestCode"
    )
    @ToString.Exclude
    private Set<Manifest> manifestSet = new LinkedHashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TPA)) return false;
        TPA tpa = (TPA) o;
        return Objects.equals(name, tpa.name) &&
                Objects.equals(departurePlan, tpa.departurePlan) &&
                Objects.equals(status, tpa.status) &&
                Objects.equals(tpaDaysSetting, tpa.tpaDaysSetting);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, departurePlan, departureReal, status, tpaDaysSetting);
    }

    @Override
    public String toString() {
        StringBuilder manifestSetString = new StringBuilder();
        manifestSet.forEach(x -> manifestSetString.append(x.getManifestCode()).append( ", "));
        return "TPA{" +
                "tpaID=" + tpaID +
                ", name='" + name + '\'' +
                ", departurePlan=" + departurePlan +
                ", departureReal=" + departureReal +
                ", status=" + status +
                ", tpaDaysSetting=" + tpaDaysSetting +
                ", manifestReferenceSet=" + manifestReferenceSet +
                ", manifestSet=[" + manifestSetString + "\b\b"
                +"]}";
    }
}

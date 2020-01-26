package pl.com.xdms.domain.dto;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import pl.com.xdms.domain.manifest.Manifest;
import pl.com.xdms.domain.manifest.ManifestReference;
import pl.com.xdms.domain.tpa.TPA;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created on 22.12.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Data
@Slf4j
@ToString
public class ManifestTpaTttDTO {
    Map<Long,Manifest> manifestMapDTO = new HashMap<>();
    Set<TPA> tpaSetDTO = new LinkedHashSet<>();
    Set<TruckTimeTable> tttSetDTO = new LinkedHashSet<>();
    Set<ManifestReference> manifestReferenceSetDTO = new LinkedHashSet<>();

}

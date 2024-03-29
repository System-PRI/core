package pl.edu.amu.wmi.model.project;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.amu.wmi.model.externallink.ExternalLinkDTO;

import java.util.List;
import java.util.Set;


@Data
@NoArgsConstructor
public class ProjectDTO {

    private String id;

    private String name;

    private SupervisorDTO supervisor;

    private boolean accepted;

    @JsonProperty("firstSemesterGrade")
    private String pointsFirstSemester;

    @JsonProperty("secondSemesterGrade")
    private String pointsSecondSemester;

    private Boolean criteriaMet;

    private Set<ExternalLinkDTO> externalLinks;

    private String defenseDay;

    private String evaluationPhase;

    private String classroom;

    private List<String> committee;

    private String students;

}

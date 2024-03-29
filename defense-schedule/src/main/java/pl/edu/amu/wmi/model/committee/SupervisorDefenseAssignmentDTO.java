package pl.edu.amu.wmi.model.committee;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.amu.wmi.enumerations.CommitteeIdentifier;

@Data
@NoArgsConstructor
public class SupervisorDefenseAssignmentDTO {

    private String supervisorId;

    @NotNull
    private Long defenseSlotId;

    private String time;

    private boolean available;

    private boolean chairperson;

    private CommitteeIdentifier committeeIdentifier;

    private String classroom;

    private String projectId;

}

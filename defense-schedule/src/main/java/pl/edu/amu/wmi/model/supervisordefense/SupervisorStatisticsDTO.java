package pl.edu.amu.wmi.model.supervisordefense;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class SupervisorStatisticsDTO {

    private String supervisor;

    private Integer numberOfGroups;

    private Integer totalNumberOfCommittees;

    /**
     * result of totalNumberOfCommittees / numberOfGroups
     */
    private Double load;

    private Map<String, Integer> committeesPerDay;

}

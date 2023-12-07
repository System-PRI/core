package pl.edu.amu.wmi.service.supervisoravailability;

import pl.edu.amu.wmi.model.supervisordefense.SupervisorDefenseAssignmentDTO;

import java.util.List;


public interface SupervisorAvailabilityService {

    void putSupervisorAvailability(String studyYear, Long supervisorId, List<SupervisorDefenseAssignmentDTO> supervisorDefenseAssignments);

}

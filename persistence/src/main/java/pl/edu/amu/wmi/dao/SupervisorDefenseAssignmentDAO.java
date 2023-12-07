package pl.edu.amu.wmi.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.amu.wmi.entity.SupervisorDefenseAssignment;

import java.util.List;

public interface SupervisorDefenseAssignmentDAO extends JpaRepository<SupervisorDefenseAssignment, Long> {

    List<SupervisorDefenseAssignment> findAllBySupervisor_Id(Long supervisorId);

    SupervisorDefenseAssignment findBySupervisor_IdAndDefenseTimeSlot_Id(Long supervisorId, Long defenseTimeSlotId);

}

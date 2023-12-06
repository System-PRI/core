package pl.edu.amu.wmi.service.supervisordefense.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.DefenseTimeSlotDAO;
import pl.edu.amu.wmi.dao.SupervisorDAO;
import pl.edu.amu.wmi.dao.SupervisorDefenseAssignmentDAO;
import pl.edu.amu.wmi.entity.DefenseTimeSlot;
import pl.edu.amu.wmi.entity.Supervisor;
import pl.edu.amu.wmi.entity.SupervisorDefenseAssignment;
import pl.edu.amu.wmi.exception.BusinessException;
import pl.edu.amu.wmi.service.supervisordefense.SupervisorDefenseAssignmentService;

import java.text.MessageFormat;
import java.util.List;

@Service
@Slf4j
public class SupervisorDefenseAssignmentServiceImpl implements SupervisorDefenseAssignmentService {

    private final SupervisorDefenseAssignmentDAO supervisorDefenseAssignmentDAO;
    private final SupervisorDAO supervisorDAO;
    private final DefenseTimeSlotDAO defenseTimeSlotDAO;

    @Autowired
    public SupervisorDefenseAssignmentServiceImpl(SupervisorDefenseAssignmentDAO supervisorDefenseAssignmentDAO,
                                                  SupervisorDAO supervisorDAO,
                                                  DefenseTimeSlotDAO defenseTimeSlotDAO) {
        this.supervisorDefenseAssignmentDAO = supervisorDefenseAssignmentDAO;
        this.supervisorDAO = supervisorDAO;
        this.defenseTimeSlotDAO = defenseTimeSlotDAO;
    }

    @Override
    @Transactional
    public void createSupervisorDefenseAssignments(String studyYear, Long defenseScheduleConfigId) {
        List<Supervisor> supervisors = supervisorDAO.findAllByStudyYear(studyYear);

        if (supervisors.isEmpty())
            throw new BusinessException(MessageFormat.format("Supervisors for study year: {0} were not found", studyYear));

        List<DefenseTimeSlot> defenseTimeSlots = defenseTimeSlotDAO.findAllByDefenseScheduleConfig_Id(defenseScheduleConfigId);

        if (defenseTimeSlots.isEmpty())
            throw new BusinessException(MessageFormat.format("Time slots for defense schedule config with id: {0} were not found", defenseScheduleConfigId));

        supervisors.forEach(supervisor -> {
            defenseTimeSlots.forEach(timeslot -> {
                SupervisorDefenseAssignment supervisorDefenseAssignment = createSingleSupervisorDefenseAssignment(supervisor, timeslot);
                supervisorDefenseAssignment = supervisorDefenseAssignmentDAO.save(supervisorDefenseAssignment);
                log.info("Supervisor defense assignment was created with id: {}", supervisorDefenseAssignment.getId());
            });
        });
    }

    private SupervisorDefenseAssignment createSingleSupervisorDefenseAssignment(Supervisor supervisor, DefenseTimeSlot defenseTimeSlot) {
        SupervisorDefenseAssignment supervisorDefenseAssignment = new SupervisorDefenseAssignment();
        supervisorDefenseAssignment.setSupervisor(supervisor);
        supervisorDefenseAssignment.setDefenseTimeSlot(defenseTimeSlot);
        return supervisorDefenseAssignment;
    }
}

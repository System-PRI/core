package pl.edu.amu.wmi.service.supervisoravailability.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.SupervisorDAO;
import pl.edu.amu.wmi.dao.SupervisorDefenseAssignmentDAO;
import pl.edu.amu.wmi.entity.Supervisor;
import pl.edu.amu.wmi.entity.SupervisorDefenseAssignment;
import pl.edu.amu.wmi.exception.BusinessException;
import pl.edu.amu.wmi.model.supervisordefense.SupervisorDefenseAssignmentDTO;
import pl.edu.amu.wmi.service.supervisoravailability.SupervisorAvailabilityService;

import java.text.MessageFormat;
import java.util.List;

@Service
@Slf4j
public class SupervisorAvailabilityServiceImpl implements SupervisorAvailabilityService {

    private final SupervisorDAO supervisorDAO;
    private final SupervisorDefenseAssignmentDAO supervisorDefenseAssignmentDAO;

    @Autowired
    public SupervisorAvailabilityServiceImpl(SupervisorDAO supervisorDAO,
                                             SupervisorDefenseAssignmentDAO supervisorDefenseAssignmentDAO) {
        this.supervisorDAO = supervisorDAO;
        this.supervisorDefenseAssignmentDAO = supervisorDefenseAssignmentDAO;
    }

    @Override
    @Transactional
    public void putSupervisorAvailability(String studyYear, Long supervisorId, List<SupervisorDefenseAssignmentDTO> supervisorDefenseAssignments) {
        Supervisor supervisor = supervisorDAO.findById(supervisorId)
                .orElseThrow(() -> new BusinessException(MessageFormat.format("Supervisors with id: {0} for study year: {1} was not found", supervisorId, studyYear)));

        List<Long> selectedTimeSlotsIds = supervisorDefenseAssignments.stream().map(SupervisorDefenseAssignmentDTO::getDefenseSlotId).toList();

        clearNotSelectedTimeSlots(supervisor, selectedTimeSlotsIds);
        pickSelectedTimeSlots(supervisor, selectedTimeSlotsIds);
    }

    /**
     * Unselect timeslots that are not selected by supervisor in current request. This method handle the case when
     * supervisor changes previously selected timeslots.
     */
    private void clearNotSelectedTimeSlots(Supervisor supervisor, List<Long> selectedTimeSlotsIds) {
        Long supervisorId = supervisor.getId();
        List<SupervisorDefenseAssignment> allSupervisorDefenseAssignments = supervisorDefenseAssignmentDAO.findAllBySupervisor_Id(supervisorId);

        allSupervisorDefenseAssignments.stream()
                .filter(defenseAssignment -> !hasDefenseTimeSlotAmongSelected(defenseAssignment, selectedTimeSlotsIds))
                .filter(this::isDefenseAssignmentAvailable)
                .forEach(defenseAssignment -> {
                    defenseAssignment.setAvailable(false);
                    supervisorDefenseAssignmentDAO.save(defenseAssignment);
                });
        log.info("Cleared timeslots for supervisor with id: {}", supervisorId);
    }

    /**
     * Check if Supervisor defense assignment has timeslot which can be found among new selected timeslots.
     */
    private boolean hasDefenseTimeSlotAmongSelected(SupervisorDefenseAssignment supervisorDefenseAssignment, List<Long> selectedTimeSlotsIds) {
        Long timeSlotId = supervisorDefenseAssignment.getDefenseTimeSlot().getId();
        return selectedTimeSlotsIds.contains(timeSlotId);
    }

    /**
     * Check if Supervisor defense assignment is set to available.
     */
    private boolean isDefenseAssignmentAvailable(SupervisorDefenseAssignment supervisorDefenseAssignment) {
        return supervisorDefenseAssignment.isAvailable();
    }

    /**
     * Set supervisor defense assignment availability to true if this object is associated with a time slot that can be
     * found among the selected time slots in the current request.
     */
    private void pickSelectedTimeSlots(Supervisor supervisor, List<Long> selectedTimeSlotsIds) {
        Long supervisorId = supervisor.getId();
        selectedTimeSlotsIds.forEach(timeSlotId -> {
            SupervisorDefenseAssignment supervisorDefenseAssignment =
                    supervisorDefenseAssignmentDAO.findBySupervisor_IdAndDefenseTimeSlot_Id(supervisorId, timeSlotId);
            supervisorDefenseAssignment.setAvailable(true);
            supervisorDefenseAssignmentDAO.save(supervisorDefenseAssignment);
        });
        log.info("Picked time slots for supervisor with id: {}", supervisorId);
    }

}

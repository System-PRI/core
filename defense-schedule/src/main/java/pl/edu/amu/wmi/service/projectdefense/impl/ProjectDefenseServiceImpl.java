package pl.edu.amu.wmi.service.projectdefense.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.DefenseScheduleConfigDAO;
import pl.edu.amu.wmi.dao.ProjectDAO;
import pl.edu.amu.wmi.dao.ProjectDefenseDAO;
import pl.edu.amu.wmi.entity.DefenseTimeSlot;
import pl.edu.amu.wmi.entity.Project;
import pl.edu.amu.wmi.entity.ProjectDefense;
import pl.edu.amu.wmi.entity.SupervisorDefenseAssignment;
import pl.edu.amu.wmi.enumerations.CommitteeIdentifier;
import pl.edu.amu.wmi.enumerations.DefensePhase;
import pl.edu.amu.wmi.enumerations.UserRole;
import pl.edu.amu.wmi.exception.BusinessException;
import pl.edu.amu.wmi.mapper.projectdefense.ProjectDefenseMapper;
import pl.edu.amu.wmi.model.UserRoleType;
import pl.edu.amu.wmi.model.projectdefense.ProjectDefenseDTO;
import pl.edu.amu.wmi.service.PermissionService;
import pl.edu.amu.wmi.service.ProjectMemberService;
import pl.edu.amu.wmi.service.defensetimeslot.DefenseTimeSlotService;
import pl.edu.amu.wmi.service.projectdefense.ProjectDefenseService;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProjectDefenseServiceImpl implements ProjectDefenseService {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private final DefenseTimeSlotService defenseTimeSlotService;
    private final ProjectMemberService projectMemberService;
    private final PermissionService permissionService;
    private final DefenseScheduleConfigDAO defenseScheduleConfigDAO;
    private final ProjectDefenseMapper projectDefenseMapper;
    private final ProjectDefenseDAO projectDefenseDAO;
    private final ProjectDAO projectDAO;

    public ProjectDefenseServiceImpl(DefenseTimeSlotService defenseTimeSlotService,
                                     ProjectMemberService projectMemberService,
                                     PermissionService permissionService,
                                     DefenseScheduleConfigDAO defenseScheduleConfigDAO,
                                     ProjectDefenseMapper projectDefenseMapper,
                                     ProjectDefenseDAO projectDefenseDAO,
                                     ProjectDAO projectDAO) {
        this.defenseTimeSlotService = defenseTimeSlotService;
        this.projectMemberService = projectMemberService;
        this.permissionService = permissionService;
        this.defenseScheduleConfigDAO = defenseScheduleConfigDAO;
        this.projectDefenseMapper = projectDefenseMapper;
        this.projectDefenseDAO = projectDefenseDAO;
        this.projectDAO = projectDAO;
    }

    @Override
    @Transactional
    public void createProjectDefenses(Long defenseScheduleConfigId, String studyYear) {
        List<DefenseTimeSlot> timeSlots = defenseTimeSlotService.getAllTimeSlotsForDefenseConfig(defenseScheduleConfigId);

        timeSlots.forEach(defenseTimeSlot ->
                createProjectDefensesForTimeSlot(studyYear, defenseTimeSlot)
        );
        log.info("Project defense slots have been created for study year {}", studyYear);
    }

    @Override
    public Map<String, List<ProjectDefenseDTO>> getProjectDefenses(String studyYear, String indexNumber) {
        List<ProjectDefense> projectDefenses = projectDefenseDAO.findAllByStudyYear(studyYear);
        Map<LocalDate, List<ProjectDefense>> projectDefenseMap = projectDefenses.stream().collect(Collectors.groupingBy(projectDefense -> projectDefense.getDefenseTimeslot().getDate()));

        UserRole userRole = projectMemberService.getUserRoleByUserIndex(indexNumber, UserRoleType.SPECIAL);
        Project projectAdminProject = Objects.equals(UserRole.PROJECT_ADMIN, userRole) ? projectDAO.findByProjectAdmin(indexNumber, studyYear) : null;
        DefensePhase defensePhase = defenseScheduleConfigDAO.findByStudyYearAndIsActiveIsTrue(studyYear).getDefensePhase();
        Map<String, List<ProjectDefenseDTO>> projectDefenseDTOMap = new TreeMap<>();
        projectDefenseMap.forEach((date, defenses) -> {
            List<ProjectDefenseDTO> projectDefenseDTOs = mapProjectDefensesToDTOs(defenses, indexNumber, projectAdminProject, userRole, defensePhase);
            projectDefenseDTOMap.put(date.format(dateTimeFormatter), projectDefenseDTOs);
        });
        return projectDefenseDTOMap;
    }

    /**
     * Map project defense entities to dtos and set the value of a field isEditable for every {@link ProjectDefenseDTO} object based on user role and project attributes
     * - for user with role COORDINATOR all project defenses are editable
     * - for users with role SUPERVISOR or STUDENT - none project defenses are editable (no mapping needed as isEditable is set to false by default)
     * - for user with role PROJECT_ADMIN project is editable if the supervisor of the project is in the committee and time slot is free
     *
     * @param defenses
     * @param indexNumber
     * @param project
     * @param userRole
     * @param defensePhase
     * @return
     */
    private List<ProjectDefenseDTO> mapProjectDefensesToDTOs(List<ProjectDefense> defenses, String indexNumber,
                                                             Project project, UserRole userRole, DefensePhase defensePhase) {
        switch (userRole) {
            case COORDINATOR -> {
                List<ProjectDefenseDTO> projectDefenseDTOs = projectDefenseMapper.mapToDTOs(defenses).stream()
                        .sorted(projectDefenseByTimeComparator())
                        .toList();
                projectDefenseDTOs.forEach(projectDefense -> projectDefense.setEditable(true));
                return projectDefenseDTOs;
            }
            case PROJECT_ADMIN -> {
                if (Objects.equals(DefensePhase.DEFENSE_PROJECT_REGISTRATION, defensePhase)) {
                    List<ProjectDefenseDTO> projectDefenseDTOs = new ArrayList<>();
                    defenses.forEach(projectDefense -> {
                        boolean isEditable = permissionService.isProjectDefenseEditableForProjectAdmin(projectDefense, indexNumber, project);
                        ProjectDefenseDTO projectDefenseDTO = projectDefenseMapper.mapToDto(projectDefense);
                        projectDefenseDTO.setEditable(isEditable);
                        projectDefenseDTOs.add(projectDefenseDTO);
                    });
                    return projectDefenseDTOs.stream()
                            .sorted(projectDefenseByTimeComparator())
                            .toList();
                } else {
                    return projectDefenseMapper.mapToDTOs(defenses).stream()
                            .sorted(projectDefenseByTimeComparator())
                            .toList();
                }
            }
            case STUDENT, SUPERVISOR -> {
                return projectDefenseMapper.mapToDTOs(defenses).stream()
                        .sorted(projectDefenseByTimeComparator())
                        .toList();
            }
        }


        return projectDefenseMapper.mapToDTOs(defenses).stream()
                .sorted(projectDefenseByTimeComparator())
                .toList();
    }

    private Comparator<ProjectDefenseDTO> projectDefenseByTimeComparator() {
        return Comparator.comparing(ProjectDefenseDTO::getTime);
    }

    private void createProjectDefensesForTimeSlot(String studyYear, DefenseTimeSlot defenseTimeSlot) {
        Map<CommitteeIdentifier, List<SupervisorDefenseAssignment>> committeeMap = mapCommitteesByCommitteeIdentifiers(defenseTimeSlot);
        committeeMap.forEach((committeeIdentifier, supervisorDefenseAssignments) -> {
            if (!supervisorDefenseAssignments.isEmpty()) {
                if (!isChairpersonSetCorrectlyForCommittee(supervisorDefenseAssignments, defenseTimeSlot, committeeIdentifier)) {
                    throw new BusinessException(MessageFormat.format("Project defense for committee with identifier: " +
                                    "{0} for time slot {1} {2} cannot be created. Committee has to have exactly one chairperson selected",
                            committeeIdentifier, defenseTimeSlot.getDate(), defenseTimeSlot.getStartTime()));
                }
                createNewProjectDefense(studyYear, supervisorDefenseAssignments);
            }
        });
    }

    private void createNewProjectDefense(String studyYear, List<SupervisorDefenseAssignment> supervisorDefenseAssignments) {
        ProjectDefense projectDefense = new ProjectDefense();
        projectDefense.addSupervisorDefenseAssignments(supervisorDefenseAssignments);
        projectDefense.setStudyYear(studyYear);
        projectDefenseDAO.save(projectDefense);
    }

    private Map<CommitteeIdentifier, List<SupervisorDefenseAssignment>> mapCommitteesByCommitteeIdentifiers(DefenseTimeSlot defenseTimeSlot) {
        return defenseTimeSlot.getSupervisorDefenseAssignments().stream()
                .filter(supervisorDefenseAssignment -> Objects.nonNull(supervisorDefenseAssignment.getCommitteeIdentifier()))
                .collect(Collectors.groupingBy(SupervisorDefenseAssignment::getCommitteeIdentifier));
    }

    private boolean isChairpersonSetCorrectlyForCommittee(List<SupervisorDefenseAssignment> supervisorDefenseAssignments,
                                                          DefenseTimeSlot defenseTimeSlot, CommitteeIdentifier committeeIdentifier) {
        long numberOfChairpersonsInCommittee = supervisorDefenseAssignments.stream()
                .filter(SupervisorDefenseAssignment::isChairperson)
                .count();
        if (numberOfChairpersonsInCommittee == 0) {
            log.error("Project defense for committee with identifier: {} for time slot {} {} cannot be created because none chairperson was selected.",
                    committeeIdentifier, defenseTimeSlot.getDate(), defenseTimeSlot.getStartTime());
            return false;
        } else if (numberOfChairpersonsInCommittee > 1) {
            log.error("Project defense for committee with identifier: {} for time slot {} {} cannot be created because more than one chairperson was selected.",
                    committeeIdentifier, defenseTimeSlot.getDate(), defenseTimeSlot.getStartTime());
            return false;
        } else {
            return true;
        }
    }
}

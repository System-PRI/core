package pl.edu.amu.wmi.service.supervisoravailability.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.dao.ProjectDefenseDAO;
import pl.edu.amu.wmi.dao.SupervisorDAO;
import pl.edu.amu.wmi.entity.ProjectDefense;
import pl.edu.amu.wmi.entity.Supervisor;
import pl.edu.amu.wmi.entity.SupervisorDefenseAssignment;
import pl.edu.amu.wmi.model.supervisordefense.SupervisorStatisticsDTO;
import pl.edu.amu.wmi.service.supervisoravailability.SupervisorStatisticsService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SupervisorStatisticsServiceImpl implements SupervisorStatisticsService {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private final SupervisorDAO supervisorDAO;
    private final ProjectDefenseDAO projectDefenseDAO;

    public SupervisorStatisticsServiceImpl(SupervisorDAO supervisorDAO, ProjectDefenseDAO projectDefenseDAO) {
        this.supervisorDAO = supervisorDAO;
        this.projectDefenseDAO = projectDefenseDAO;
    }

    @Override
    public List<SupervisorStatisticsDTO> getSupervisorStatistics(String studyYear) {
        List<Supervisor> supervisors = supervisorDAO.findAllByStudyYear(studyYear);
        List<ProjectDefense> projectDefenses = projectDefenseDAO.findAllByStudyYear(studyYear);
        Map<LocalDate, List<ProjectDefense>> projectDefenseMap = projectDefenses.stream().collect(Collectors.groupingBy(projectDefense -> projectDefense.getDefenseTimeslot().getDate()));
        List<SupervisorStatisticsDTO> supervisorStatisticsDTOs = new ArrayList<>();

        supervisors.forEach(supervisor -> {
            SupervisorStatisticsDTO supervisorStatisticsDTO = new SupervisorStatisticsDTO();
            supervisorStatisticsDTO.setSupervisor(supervisor.getFullName());
            int numberOfGroups = supervisor.getProjects().size();
            supervisorStatisticsDTO.setNumberOfGroups(supervisor.getProjects().size());
            int numberOfAssignedProjectDefenses = (int) projectDefenses.stream()
                    .filter(defense -> isProjectSupervisorCommitteeMember(defense, supervisor))
                    .filter(defense -> Objects.nonNull(defense.getProject()))
                    .count();
            supervisorStatisticsDTO.setTotalNumberOfCommittees(numberOfAssignedProjectDefenses);
            if (numberOfGroups == 0) {
                supervisorStatisticsDTO.setLoad(0.0);
            } else {
                double load = (double) numberOfAssignedProjectDefenses / (double) numberOfGroups;
                supervisorStatisticsDTO.setLoad(load);
            }

            Map<String, Integer> supervisorDefensesMap = new TreeMap<>();
            projectDefenseMap.forEach((date, defenses) -> {
                int numberOfCommittees = (int) defenses.stream()
                        .filter(defense -> isProjectSupervisorCommitteeMember(defense, supervisor))
                        .filter(defense -> Objects.nonNull(defense.getProject()))
                        .count();
                supervisorDefensesMap.put(date.format(dateTimeFormatter), numberOfCommittees);
            });
            supervisorStatisticsDTO.setCommitteesPerDay(supervisorDefensesMap);
            supervisorStatisticsDTOs.add(supervisorStatisticsDTO);
        });

        return supervisorStatisticsDTOs;
    }

    // TODO: 12/10/2023 refactor needed - method should be taken from projectMember service
    private boolean isProjectSupervisorCommitteeMember(ProjectDefense projectDefense, Supervisor supervisor) {
        List<Supervisor> committeeMembers = projectDefense.getSupervisorDefenseAssignments().stream()
                .map(SupervisorDefenseAssignment::getSupervisor)
                .toList();
        return committeeMembers.contains(supervisor);
    }
}

package pl.edu.amu.wmi.service.permission.impl;

import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.dao.ProjectDAO;
import pl.edu.amu.wmi.entity.EvaluationCard;
import pl.edu.amu.wmi.entity.Project;
import pl.edu.amu.wmi.entity.Student;
import pl.edu.amu.wmi.entity.Supervisor;
import pl.edu.amu.wmi.enumerations.AcceptanceStatus;
import pl.edu.amu.wmi.enumerations.EvaluationPhase;
import pl.edu.amu.wmi.enumerations.EvaluationStatus;
import pl.edu.amu.wmi.enumerations.UserRole;
import pl.edu.amu.wmi.exception.project.ProjectManagementException;
import pl.edu.amu.wmi.service.permission.PermissionService;
import pl.edu.amu.wmi.service.project.ProjectMemberService;

import java.text.MessageFormat;
import java.util.Objects;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final ProjectMemberService projectMemberService;

    private final ProjectDAO projectDAO;

    public PermissionServiceImpl(ProjectMemberService projectMemberService, ProjectDAO projectDAO) {
        this.projectMemberService = projectMemberService;
        this.projectDAO = projectDAO;
    }

    @Override
    public boolean isUserAllowedToSeeProjectDetails(String studyYear, String indexNumber, Long projectId) {
        Project project = projectDAO.findById(projectId).orElseThrow(() ->
                new ProjectManagementException(MessageFormat.format("Project with id: {0} not found", projectId)));
        boolean isCoordinator = projectMemberService.isUserRoleCoordinator(indexNumber);
        boolean isStudentAMemberOfProject = isStudentAMemberOfProject(indexNumber, project);
        boolean isSupervisorAllowedToSeeGrades = project.getEvaluationCards().stream()
                .anyMatch(evaluationCard -> isSupervisorAllowedToSeeGrades(project, evaluationCard, indexNumber));

        return isCoordinator || isStudentAMemberOfProject || isSupervisorAllowedToSeeGrades;
    }

    @Override
    public boolean isEvaluationCardEditableForUser(EvaluationCard evaluationCardEntity, Project project, String indexNumber) {
        if (!Objects.equals(AcceptanceStatus.ACCEPTED, project.getAcceptanceStatus())) {
            return false;
        }
        if (isUserAProjectSupervisor(project.getSupervisor(), indexNumber) && Objects.equals(EvaluationStatus.ACTIVE, evaluationCardEntity.getEvaluationStatus())) {
            return true;
        }
        if (isSupervisorAllowedToEditGrades(evaluationCardEntity, indexNumber)) {
            return true;
        }
        if (projectMemberService.isUserRoleCoordinator(indexNumber)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isEvaluationCardVisibleForUser(EvaluationCard evaluationCardEntity, Project project, String indexNumber) {
        return projectMemberService.isUserRoleCoordinator(indexNumber)
                || isStudentAMemberOfProject(indexNumber, project)
                || isSupervisorAllowedToSeeGrades(project, evaluationCardEntity, indexNumber);
    }

    private boolean isUserAProjectSupervisor(Supervisor supervisor, String indexNumber) {
        return Objects.equals(supervisor.getIndexNumber(), indexNumber);
    }

    private boolean isSupervisorAllowedToEditGrades(EvaluationCard evaluationCardEntity, String indexNumber) {
        return Objects.equals(UserRole.SUPERVISOR, projectMemberService.getUserRoleByUserIndex(indexNumber))
                && !Objects.equals(EvaluationPhase.SEMESTER_PHASE, evaluationCardEntity.getEvaluationPhase())
                && Objects.equals(EvaluationStatus.ACTIVE, evaluationCardEntity.getEvaluationStatus());
    }

    private boolean isStudentAMemberOfProject(String indexNumber, Project project) {
        return project.getStudents().stream()
                .map(Student::getIndexNumber)
                .anyMatch(studentIndexId -> Objects.equals(indexNumber, studentIndexId));
    }

    private boolean isSupervisorAllowedToSeeGrades(Project project, EvaluationCard evaluationCardEntity, String indexNumber) {
        boolean isUserAProjectSupervisor = isUserAProjectSupervisor(project.getSupervisor(), indexNumber);
        boolean isUserASupervisorAndProjectPhaseIsDifferentThanSemester = Objects.equals(UserRole.SUPERVISOR, projectMemberService.getUserRoleByUserIndex(indexNumber))
                && !Objects.equals(EvaluationPhase.SEMESTER_PHASE, evaluationCardEntity.getEvaluationPhase());

        return isUserAProjectSupervisor || isUserASupervisorAndProjectPhaseIsDifferentThanSemester;
    }
}

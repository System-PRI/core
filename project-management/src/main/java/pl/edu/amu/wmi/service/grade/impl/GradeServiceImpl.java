package pl.edu.amu.wmi.service.grade.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.ProjectDAO;
import pl.edu.amu.wmi.entity.*;
import pl.edu.amu.wmi.enumerations.CriterionCategory;
import pl.edu.amu.wmi.enumerations.Semester;
import pl.edu.amu.wmi.exception.project.ProjectManagementException;
import pl.edu.amu.wmi.mapper.grade.ProjectCriteriaSectionMapper;
import pl.edu.amu.wmi.model.grade.CriteriaGroupDTO;
import pl.edu.amu.wmi.model.grade.CriteriaSectionDTO;
import pl.edu.amu.wmi.model.grade.GradeDetailsDTO;
import pl.edu.amu.wmi.service.grade.GradeService;

import java.text.MessageFormat;
import java.util.*;

@Slf4j
@Service
public class GradeServiceImpl implements GradeService {

    private final ProjectDAO projectDAO;
    private final ProjectCriteriaSectionMapper projectCriteriaSectionMapper;
    private final EvaluationCardServiceImpl evaluationCardService;


    @Autowired
    public GradeServiceImpl(ProjectDAO projectDAO, ProjectCriteriaSectionMapper projectCriteriaSectionMapper, EvaluationCardServiceImpl evaluationCardService) {
        this.projectDAO = projectDAO;
        this.projectCriteriaSectionMapper = projectCriteriaSectionMapper;
        this.evaluationCardService = evaluationCardService;
    }

    // TODO: Add and update Javadoc
    // TODO: Do refactor of everything here
    // TODO: Add new folder structure

    /**
     * Creates a ProjectGradeDetailsDTO object that contains grade information for a specific project.
     * In addition to information about the criteria related to the project, there is also information about the
     * selected criteria for each Criteria Group. The selected criterion is calculated based on the points obtained by
     * the project in a specific Criteria Group.
     *
     * @param semester  - semester that the criteria are fetched for
     * @param projectId - project that the evaluation card is fetched for
     * @return Project grade details object that is distinguishable by semesters
     */
    @Override
    public GradeDetailsDTO findByProjectIdAndSemester(Semester semester, Long projectId) {
        GradeDetailsDTO projectGradeDetails = new GradeDetailsDTO();

        Project project = projectDAO.findById(projectId)
                .orElseThrow(() -> new ProjectManagementException(MessageFormat.format("Project with id: {0} not found", projectId)));

        projectGradeDetails.setId(projectId);
        projectGradeDetails.setProjectName(project.getName());
        projectGradeDetails.setSemester(semester);

        Double points = getPointsForSemester(project, semester);
        projectGradeDetails.setGrade(pointsToOverallPercent(points));

        List<CriteriaSection> sections = getCriteriaSectionsForSemester(project, semester);
        List<CriteriaSectionDTO> sectionDTOs = projectCriteriaSectionMapper.mapToDtoList(sections);


        List<Grade> projectGrades = project.getEvaluationCard().getGrades();
        Map<Long, Integer> projectPointsByGroupId = new HashMap<>();
        for (Grade projectGrade : projectGrades) {
            projectPointsByGroupId.put(projectGrade.getCriteriaGroup().getId(), projectGrade.getPoints());
        }

        sectionDTOs.forEach(section -> section.getCriteriaGroups().forEach(group ->
                group.setSelectedCriterion(CriterionCategory.getByPointsReceived(projectPointsByGroupId.get(group.getId())))
        ));

        projectGradeDetails.setSections(sectionDTOs);

        return projectGradeDetails;
    }

    /**
     * @return Points value which was received by the project in the chosen semester
     */
    private Double getPointsForSemester(Project project, Semester semester) {
        return switch (semester) {
            case SEMESTER_I -> project.getEvaluationCard().getTotalPointsFirstSemester();
            case SEMESTER_II -> project.getEvaluationCard().getTotalPointsSecondSemester();
        };
    }

    private String pointsToOverallPercent(Double points) {
        Double pointsOverall = points * 100 / 4;
        return String.format("%.2f", pointsOverall) + "%";
    }

    /**
     * @return List of criteria sections which are related to the project in chosen semester
     */
    private List<CriteriaSection> getCriteriaSectionsForSemester(Project project, Semester semester) {
        return switch (semester) {
            case SEMESTER_I ->
                    project.getEvaluationCard().getEvaluationCardTemplate().getCriteriaSectionsFirstSemester();
            case SEMESTER_II ->
                    project.getEvaluationCard().getEvaluationCardTemplate().getCriteriaSectionsSecondSemester();
        };
    }

    @Transactional
    public GradeDetailsDTO updateProjectGradesForSemester(Semester semester, Long projectId, GradeDetailsDTO projectGradeDetails) {
        Project project = projectDAO.findById(projectId)
                .orElseThrow(() -> new ProjectManagementException(MessageFormat.format("Project with id: {0} not found", projectId)));

        List<CriteriaGroupDTO> groups = getCriteriaGroupsToUpdate(projectGradeDetails);
        Map<Long, CriterionCategory> newSelectedCriteriaByGroupId = getSelectedCriteriaByGroupId(groups);

        List<Grade> projectGradesForSemester = getGradesForSemester(semester, project);
        projectGradesForSemester.forEach(grade -> {
            Long gradeCriteriaGroupId = grade.getCriteriaGroup().getId();
            CriterionCategory newSelectedCriterion = newSelectedCriteriaByGroupId.get(gradeCriteriaGroupId);
            updateGrade(grade, newSelectedCriterion);
        });

        evaluationCardService.updateEvaluationCard(projectGradesForSemester, semester, project);
        projectDAO.save(project);

        return projectGradeDetails;
    }

    private List<CriteriaGroupDTO> getCriteriaGroupsToUpdate(GradeDetailsDTO projectGradeDetails) {
        List<CriteriaSectionDTO> sections = projectGradeDetails.getSections();
        return sections.stream()
                .map(CriteriaSectionDTO::getCriteriaGroups)
                .flatMap(List::stream)
                .toList();
    }

    private Map<Long, CriterionCategory> getSelectedCriteriaByGroupId(List<CriteriaGroupDTO> criteriaGroups) {
        Map<Long, CriterionCategory> selectedCriteriaByGroupId = new HashMap<>();
        for (CriteriaGroupDTO group : criteriaGroups) {
            selectedCriteriaByGroupId.put(group.getId(), group.getSelectedCriterion());
        }
        return selectedCriteriaByGroupId;
    }

    private List<Grade> getGradesForSemester(Semester semester, Project project) {
        List<Grade> projectGrades = project.getEvaluationCard().getGrades();
        return projectGrades.stream()
                .filter(grade -> isGradeForSemester(grade, semester))
                .toList();
    }

    private boolean isGradeForSemester(Grade grade, Semester semester) {
        return grade.getCriteriaGroup().getCriteriaSection().getSemester().equals(semester);
    }

    private void updateGrade(Grade grade, CriterionCategory criterionCategory) {
        CriteriaGroup gradeCriteriaGroup = grade.getCriteriaGroup();
        Integer criterionPoints = CriterionCategory.getPoints(criterionCategory);
        Optional<Criterion> criterion = gradeCriteriaGroup.getCriteria().stream()
                .filter(c -> c.getCriterionCategory().equals(criterionCategory))
                .findFirst();
        Double groupWeight = grade.getCriteriaGroup().getGradeWeight();

        if (criterion.isPresent()) {
            grade.setPoints(criterionPoints);
            grade.setPointsWithWeight(criterionPoints * groupWeight);
            grade.setDisqualifying(criterion.get().isDisqualifying());
        } else {
            grade.setPointsWithWeight(null);
            grade.setPoints(null);
            grade.setDisqualifying(false);
        }
    }

}

package pl.edu.amu.wmi.service.grade.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.EvaluationCardDAO;
import pl.edu.amu.wmi.dao.EvaluationCardTemplateDAO;
import pl.edu.amu.wmi.entity.*;
import pl.edu.amu.wmi.enumerations.Semester;
import pl.edu.amu.wmi.exception.project.ProjectManagementException;
import pl.edu.amu.wmi.service.grade.EvaluationCardService;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class EvaluationCardServiceImpl implements EvaluationCardService {

    private final EvaluationCardDAO evaluationCardDAO;
    private final EvaluationCardTemplateDAO evaluationCardTemplateDAO;

    public EvaluationCardServiceImpl(EvaluationCardDAO evaluationCardDAO,
                                     EvaluationCardTemplateDAO evaluationCardTemplateDAO) {
        this.evaluationCardDAO = evaluationCardDAO;
        this.evaluationCardTemplateDAO = evaluationCardTemplateDAO;
    }

    @Override
    @Transactional
    public void addEmptyGradesToEvaluationCard(Project project, String studyYear) {
        Optional<EvaluationCardTemplate> evaluationCardTemplate = evaluationCardTemplateDAO.findByStudyYear(studyYear);
        if (evaluationCardTemplate.isEmpty()) {
            log.info("Evaluation criteria have been not yet uploaded to the system - EvaluationCard will be updated later");
            return;
        }
        EvaluationCardTemplate template = evaluationCardTemplate.get();

        List<Grade> grades = createEmptyGrades(template);

        EvaluationCard evaluationCard = evaluationCardDAO.findById(project.getId()).orElseThrow(()
                -> new ProjectManagementException(MessageFormat.format("Evaluation card for project: {0} not found", project.getId())));
        evaluationCard.setEvaluationCardTemplate(template);
        evaluationCard.setGrades(grades);
        evaluationCardDAO.save(evaluationCard);
    }

    private List<Grade> createEmptyGrades(EvaluationCardTemplate template) {
        List<Grade> grades = new ArrayList<>();
        grades.addAll(createEmptyGradesForSemester(template.getCriteriaSectionsFirstSemester()));
        grades.addAll(createEmptyGradesForSemester(template.getCriteriaSectionsSecondSemester()));
        return grades;
    }

    private List<Grade> createEmptyGradesForSemester(List<CriteriaSection> criteriaSections) {
        List<Grade> grades = new ArrayList<>();
        criteriaSections.forEach(criteriaSection -> {
                    List<Grade> gradesForSection = createEmptyGradesForCriteriaSection(criteriaSection);
                    grades.addAll(gradesForSection);
                });
        return grades;
    }

    private List<Grade> createEmptyGradesForCriteriaSection(CriteriaSection criteriaSection) {
        return criteriaSection.getCriteriaGroups().stream()
                .map(Grade::new)
                .toList();
    }


    // TODO: 11/18/2023 - Once SYSPRI-223 is ready, update the disqualification and approval conditions
    @Override
    public void updateEvaluationCard(List<Grade> gradesForSemester, Semester semester, Project project) {
        EvaluationCard evaluationCard = project.getEvaluationCard();

        List<Double> gradesWeightsForSemester = gradesForSemester.stream()
                .map(grade -> grade.getCriteriaGroup().getGradeWeight()).toList();

        gradesForSemester.forEach(grade -> grade.getCriteriaGroup()
                .setModificationDate(grade.getModificationDate()));

        Double totalPointsWithWeight = gradesForSemester.stream()
                .map(Grade::getPointsWithWeight)
                .filter(Objects::nonNull)
                .reduce(0.0, Double::sum);
        Double totalWeight = gradesWeightsForSemester.stream()
                .filter(Objects::nonNull)
                .reduce(0.0, Double::sum);

        Double totalPointsSemester = totalPointsWithWeight / totalWeight;
        setTotalPointsForSemester(semester, evaluationCard, totalPointsSemester);

        boolean isDisqualified = checkDisqualification(gradesForSemester);
        evaluationCard.setDisqualified(isDisqualified);
        evaluationCard.setApprovedForDefense(!isDisqualified);
    }

    private void setTotalPointsForSemester(Semester semester, EvaluationCard evaluationCard, Double totalPoints) {
        switch (semester) {
            case SEMESTER_I ->
                    evaluationCard.setTotalPointsFirstSemester(totalPoints);
            case SEMESTER_II ->
                    evaluationCard.setTotalPointsSecondSemester(totalPoints);
        }
    }

    private boolean checkDisqualification(List<Grade> gradesForSemester) {
        return gradesForSemester.stream().anyMatch(Grade::isDisqualifying) ||
                gradesForSemester.stream().anyMatch(g -> g.getPointsWithWeight() == null);
    }
}

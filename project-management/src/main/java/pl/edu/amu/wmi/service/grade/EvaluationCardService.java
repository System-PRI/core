package pl.edu.amu.wmi.service.grade;

import pl.edu.amu.wmi.entity.Project;
import pl.edu.amu.wmi.model.grade.EvaluationCardDTO;
import pl.edu.amu.wmi.model.grade.SingleGroupGradeUpdateDTO;


public interface EvaluationCardService {

    void createEvaluationCard(Project project, String studyYear);

    SingleGroupGradeUpdateDTO updateEvaluationCard(Long evaluationCardId, SingleGroupGradeUpdateDTO singleGroupGradeUpdate);

    EvaluationCardDTO findEvaluationCards(Long projectId, String studyYear);
}

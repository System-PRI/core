package pl.edu.amu.wmi.service.grade;

import pl.edu.amu.wmi.entity.Grade;
import pl.edu.amu.wmi.entity.Project;
import pl.edu.amu.wmi.enumerations.Semester;

import java.util.List;

public interface EvaluationCardService {
    void addEmptyGradesToEvaluationCard(Project project, String studyYear);
    void updateEvaluationCard(List<Grade> gradesForSemester, Semester semester, Project project);
}

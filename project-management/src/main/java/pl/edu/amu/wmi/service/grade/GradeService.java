package pl.edu.amu.wmi.service.grade;

import pl.edu.amu.wmi.enumerations.Semester;
import pl.edu.amu.wmi.model.grade.GradeDetailsDTO;


public interface GradeService {

    GradeDetailsDTO findByProjectIdAndSemester(Semester semester, Long id);

    GradeDetailsDTO updateProjectGradesForSemester(Semester semester, Long projectId, GradeDetailsDTO projectGradeDetails);

}

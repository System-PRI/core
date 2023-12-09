package pl.edu.amu.wmi.service.notification;

import pl.edu.amu.wmi.entity.Student;
import pl.edu.amu.wmi.enumerations.DefensePhase;

import java.util.List;

public interface DefenseNotificationService {

    /**
     * Sends email notification to all students from a study year.
     * The message content depends on the defense phase
     *
     * @param studyYear    - study year used to fetch students data
     * @param defensePhase - the phase of project defense process
     */
    void notifyStudents(String studyYear, DefensePhase defensePhase);

    void notifyStudentsAboutProjectDefenseAssignment(List<Student> students);
}

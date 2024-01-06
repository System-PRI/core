package pl.edu.amu.wmi.exception.grade;

import pl.edu.amu.wmi.exception.BusinessException;

public class UserNotAllowedToSeeGradesInDefensePhaseException extends BusinessException {

    public UserNotAllowedToSeeGradesInDefensePhaseException(String message) {
        super(message);
    }
}

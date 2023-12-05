package pl.edu.amu.wmi.service;

import pl.edu.amu.wmi.model.DefenseScheduleConfigDTO;

public interface DefenseScheduleConfigService {

    void createDefenseScheduleConfig(String studyYear, DefenseScheduleConfigDTO defenseScheduleConfig);

}

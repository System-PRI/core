package pl.edu.amu.wmi.model;

import java.util.List;

public record CriteriaGroupDTO (
        String name,
        Double criteriaGroupGradeWeightFirstSemester,
        Double criteriaGroupGradeWeightSecondSemester,
        List<CriterionDTO> criteria
) {}

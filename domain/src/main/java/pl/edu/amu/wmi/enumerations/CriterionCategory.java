package pl.edu.amu.wmi.enumerations;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum CriterionCategory {
    CRITERION_NOT_MET(0, 0.0),
    UNSUCCESSFUL_ATTEMPT_TO_MEET_THE_CRITERION(1, 0.1),
    CRITERION_MET_WITH_RESERVATIONS(2, 0.3),
    CRITERION_MET(3, 0.4);

    final Double points;

    @Getter
    final Integer value;

    CriterionCategory(Integer value, Double points) {
        this.value = value;
        this.points = points;
    }

    private static final Map<Double, CriterionCategory> criterionCategories;
    static {
        criterionCategories = new HashMap<Double, CriterionCategory>();
        for (CriterionCategory category : CriterionCategory.values()) {
            criterionCategories.put(category.points, category);
        }
    }

    public static CriterionCategory findByPointsReceived(Double points) {
        return criterionCategories.get(points);
    }

}

package pl.edu.amu.wmi.model;

public record ScoringCriteriaDTO(
        String category,
        Integer points,
        String description,
        boolean isDisqualifying
) {
}

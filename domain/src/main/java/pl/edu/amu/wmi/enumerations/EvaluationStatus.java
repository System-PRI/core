package pl.edu.amu.wmi.enumerations;

public enum EvaluationStatus {

    ACTIVE("Active"), FROZEN("Frozen"), PUBLISHED("Published"), RETAKE("Retake");

    public final String label;

    EvaluationStatus(String label) {
        this.label = label;
    }

}

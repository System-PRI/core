package pl.edu.amu.wmi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "SCORING_CRITERIA")
public class ScoringCriteria extends AbstractEntity {

    private Integer points;

    private String description;

    private Boolean isDisqualifying;

}

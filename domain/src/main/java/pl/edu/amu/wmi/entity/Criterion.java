package pl.edu.amu.wmi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "CRITERION")
public class Criterion extends AbstractEntity {

    private String name;

    @ManyToOne
    @JoinColumn(name = "CRITERIA_GROUP_ID")
    private CriteriaGroup criteriaGroup;

    private Double gradeWeight;

    @ManyToMany
    private Set<ScoringCriteria> scoringCriteria;

}

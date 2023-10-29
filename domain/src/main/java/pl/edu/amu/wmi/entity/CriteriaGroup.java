package pl.edu.amu.wmi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import pl.edu.amu.wmi.enumerations.Semester;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "CRITERIA_GROUP")
public class CriteriaGroup extends AbstractEntity {

    private String name;

    @OneToMany
    @JoinColumn(name = "CRITERIA_GROUP_ID")
    private List<Criterion> criteria = new ArrayList<>();

    private Double criteriaGroupGradeWeight;

    private Semester semester;

}

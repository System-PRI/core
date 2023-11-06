package pl.edu.amu.wmi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "EVALUATION_CARD")
public class EvaluationCard extends AbstractEntity {


    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "PROJECT_ID", unique = true)
    private Project project;;

    @Column(name = "TOTAL_POINTS_SEMESTER_I")
    private Double totalPointsFirstSemester;

    @Column(name = "TOTAL_POINTS_SEMESTER_II")
    private Double totalPointsSecondSemester;

    private boolean isDisqualified;

    private boolean isApprovedForDefense;

    @ManyToOne
    @JoinColumn(name = "EVALUATION_CARD_TEMPLATE_ID")
    private EvaluationCardTemplate evaluationCardTemplate;

    @OneToMany(
            cascade = CascadeType.REMOVE
    )
    @JoinColumn(name = "EVALUATION_CARD_ID")
    private List<Grade> grades = new ArrayList<>();

    @Column(name = "FINAL_GRADE_SEMESTER_I")
    private Double finalGradeFirstSemester;

    @Column(name = "FINAL_GRADE_SEMESTER_II")
    private Double finalGradeSecondSemester;

}

package pl.edu.amu.wmi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "EVALUATION_CARD")
public class EvaluationCard extends AbstractEntity {

    @OneToOne
    @JoinColumn(name = "PROJECT_ID")
    private Project project;

    @Column(name = "TOTAL_POINTS_SEMESTER_I")
    private Double totalPointsFirstSemester;

    @Column(name = "TOTAL_POINTS_SEMESTER_II")
    private Double totalPointsSecondSemester;

    private boolean isDisqualified;

    private boolean isApprovedForDefense;

    @ManyToOne
    @JoinColumn(name = "EVALUATION_CARD_TEMPLATE_ID")
    private EvaluationCardTemplate evaluationCardTemplate;

    @Column(name = "FINAL_GRADE_SEMESTER_I")
    private Double finalGradeFirstSemester;

    @Column(name = "FINAL_GRADE_SEMESTER_II")
    private Double finalGradeSecondSemester;

}

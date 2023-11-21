package pl.edu.amu.wmi.model.grade;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.amu.wmi.enumerations.CriterionCategory;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SingleGroupGradeUpdateDTO {

    @NotNull
    private Long id;

    @Enumerated(EnumType.STRING)
    private CriterionCategory selectedCriterion;

}

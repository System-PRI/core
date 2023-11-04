package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.edu.amu.wmi.entity.Criterion;
import pl.edu.amu.wmi.model.CriterionDTO;

@Mapper(componentModel = "spring")
public interface CriterionMapper {

    @Mapping(target = "gradeWeight", source = "gradeWeightFirstSemester")
    @Mapping(target = "scoringCriteria", ignore = true)
    Criterion mapToEntityForFirstSemester(CriterionDTO dto);

    @Mapping(target = "gradeWeight", source = "gradeWeightSecondSemester")
    @Mapping(target = "scoringCriteria", ignore = true)
    Criterion mapToEntityForSecondSemester(CriterionDTO dto);

}

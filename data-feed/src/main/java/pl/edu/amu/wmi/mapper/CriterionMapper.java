package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pl.edu.amu.wmi.entity.Criterion;
import pl.edu.amu.wmi.model.CriterionDTO;

@Mapper(componentModel = "spring", uses = {ScoringCriteriaMapper.class})
public interface CriterionMapper {

    @Named("mapToEntityForFirstSemester")
    @Mapping(target = "gradeWeight", source = "gradeWeightFirstSemester")
    Criterion mapToEntityForFirstSemester(CriterionDTO dto);

    @Named("mapToEntityForSecondSemester")
    @Mapping(target = "gradeWeight", source = "gradeWeightSecondSemester")
    Criterion mapToEntityForSecondSemester(CriterionDTO dto);

}

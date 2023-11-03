package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.edu.amu.wmi.entity.ScoringCriteria;
import pl.edu.amu.wmi.model.ScoringCriteriaDTO;

@Mapper(componentModel = "spring")
public interface ScoringCriteriaMapper {

    @Mapping(target = "criterionCategory", source = "category")
    @Mapping(target = "disqualifying", source = "isDisqualifying")
    ScoringCriteria mapToEntity(ScoringCriteriaDTO dto);

}

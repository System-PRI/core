package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.edu.amu.wmi.entity.CriteriaGroup;
import pl.edu.amu.wmi.model.CriteriaGroupDTO;

@Mapper(componentModel = "spring")
public interface CriteriaGroupMapper {

    @Mapping(target = "criteriaGroupGradeWeight", source = "criteriaGroupGradeWeightFirstSemester")
    @Mapping(target = "criteria", ignore = true)
    @Mapping(target = "semester", expression = "java(pl.edu.amu.wmi.enumerations.Semester.SEMESTER_I)")
    CriteriaGroup mapToEntityForFirstSemester(CriteriaGroupDTO dto);

    @Mapping(target = "criteriaGroupGradeWeight", source = "criteriaGroupGradeWeightSecondSemester")
    @Mapping(target = "criteria", ignore = true)
    @Mapping(target = "semester", expression = "java(pl.edu.amu.wmi.enumerations.Semester.SEMESTER_II)")
    CriteriaGroup mapToEntityForSecondSemester(CriteriaGroupDTO criteriaGroupDTO);
}

package pl.edu.amu.wmi.mapper.grade;

import org.mapstruct.Mapper;

import pl.edu.amu.wmi.entity.EvaluationCard;
import pl.edu.amu.wmi.model.grade.EvaluationCardStatusDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EvaluationCardMapper {

    EvaluationCardStatusDTO mapToDto(EvaluationCard evaluationCard);

    List<EvaluationCardStatusDTO> mapToDtoList(List<EvaluationCard> evaluationCardList);

}

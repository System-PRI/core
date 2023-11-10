package pl.edu.amu.wmi.service.exportdata.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.edu.amu.wmi.dao.EvaluationCardTemplateRepositoryDAO;
import pl.edu.amu.wmi.entity.CriteriaGroup;
import pl.edu.amu.wmi.entity.CriteriaSection;
import pl.edu.amu.wmi.entity.Criterion;
import pl.edu.amu.wmi.entity.EvaluationCardTemplate;
import pl.edu.amu.wmi.enumerations.Semester;
import pl.edu.amu.wmi.mapper.CriteriaGroupMapper;
import pl.edu.amu.wmi.mapper.EvaluationCriteriaMapper;
import pl.edu.amu.wmi.model.CriteriaGroupDTO;
import pl.edu.amu.wmi.model.CriteriaSectionDTO;
import pl.edu.amu.wmi.model.EvaluationCriteriaDTO;
import pl.edu.amu.wmi.model.enumeration.DataFeedType;
import pl.edu.amu.wmi.service.exportdata.DataFeedExportService;

import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class DataFeedCriteriaExportServiceImpl implements DataFeedExportService {

    private final EvaluationCardTemplateRepositoryDAO evaluationCardTemplateRepositoryDAO;
    private final EvaluationCriteriaMapper evaluationCriteriaMapper;
    private final CriteriaGroupMapper criteriaGroupMapper;

    public DataFeedCriteriaExportServiceImpl(EvaluationCardTemplateRepositoryDAO evaluationCardTemplateRepositoryDAO, EvaluationCriteriaMapper evaluationCriteriaMapper, CriteriaGroupMapper criteriaGroupMapper) {
        this.evaluationCardTemplateRepositoryDAO = evaluationCardTemplateRepositoryDAO;
        this.evaluationCriteriaMapper = evaluationCriteriaMapper;
        this.criteriaGroupMapper = criteriaGroupMapper;
    }

    @Override
    public void exportData(Writer writer, String studyYear) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        EvaluationCardTemplate evaluationCardTemplate = evaluationCardTemplateRepositoryDAO.findByStudyYear(studyYear).orElseGet(EvaluationCardTemplate::new);



        List<CriteriaSection> criteriaSectionsFirstSemester = evaluationCardTemplate.getCriteriaSectionsFirstSemester();
        List<CriteriaSection> criteriaSectionsSecondSemester = evaluationCardTemplate.getCriteriaSectionsSecondSemester();

        Map<String, List<CriteriaSection>> criteriaSectionMap = new HashMap<>();

        List<String> criteriaSectionFistSemesterNames = criteriaSectionsFirstSemester.stream()
                        .map(criteriaSection -> criteriaSection.getName())
                        .toList();

        List<String> criteriaSectionSecondSemesterNames = criteriaSectionsSecondSemester.stream()
                        .map(criteriaSection -> criteriaSection.getName())
                        .toList();

        Set<String> criteriaSectionNames = Stream.concat(criteriaSectionFistSemesterNames.stream(), criteriaSectionSecondSemesterNames.stream()).collect(Collectors.toSet());

        List<CriteriaSectionDTO> criteriaSectionDTOS = new ArrayList<>();

        // TODO: 11/10/2023 add id mapping

        for (String criteriaSectionName : criteriaSectionNames) {

            CriteriaSection criteriaSectionFirstSemester = criteriaSectionsFirstSemester.stream()
                    .filter(criteriaSection -> Objects.equals(criteriaSectionName, criteriaSection.getName()))
                    .findFirst()
                    .orElse(null);

            Map<String, List<CriteriaGroup>> criteriaGroups = new HashMap<>();
            Double criteriaSectionGradeWeightFirstSemester = null;

            if (Objects.nonNull(criteriaSectionFirstSemester)) {
                criteriaSectionGradeWeightFirstSemester = criteriaSectionFirstSemester.getCriteriaSectionGradeWeight();

                for (CriteriaGroup criteriaGroup : criteriaSectionFirstSemester.getCriteriaGroups()) {
                    criteriaGroup.setSemester(Semester.SEMESTER_I);
                    criteriaGroups.computeIfAbsent(criteriaGroup.getName(), k -> new ArrayList<>()).add(criteriaGroup);
                }

            }

            CriteriaSection criteriaSectionSecondSemester = criteriaSectionsSecondSemester.stream()
                    .filter(criteriaSection -> Objects.equals(criteriaSectionName, criteriaSection.getName()))
                    .findFirst()
                    .orElse(null);

            Double criteriaSectionGradeWeightSecondSemester = null;

            if (Objects.nonNull(criteriaSectionSecondSemester)) {
                criteriaSectionGradeWeightSecondSemester = criteriaSectionSecondSemester.getCriteriaSectionGradeWeight();

                for (CriteriaGroup criteriaGroup : criteriaSectionSecondSemester.getCriteriaGroups()) {
                    criteriaGroup.setSemester(Semester.SEMESTER_II);
                    criteriaGroups.computeIfAbsent(criteriaGroup.getName(), k -> new ArrayList<>()).add(criteriaGroup);
                }
            }

            List<CriteriaGroupDTO> criteriaGroupDTOS = new ArrayList<>();

            for (Map.Entry<String, List<CriteriaGroup>> entry : criteriaGroups.entrySet()) {
                Double weightFirstSemester = entry.getValue().stream()
                        .filter(criteriaGroup -> Objects.equals(Semester.SEMESTER_I, criteriaGroup.getSemester()))
                        .map(CriteriaGroup::getGradeWeight)
                        .findFirst()
                        .orElse(0.0);

                Double weightSecondSemester = entry.getValue().stream()
                        .filter(criteriaGroup -> Objects.equals(Semester.SEMESTER_II, criteriaGroup.getSemester()))
                        .map(CriteriaGroup::getGradeWeight)
                        .findFirst()
                        .orElse(0.0);

                List<Criterion> criteria = entry.getValue().get(0).getCriteria().stream().toList();

                CriteriaGroupDTO criteriaGroupDTO = criteriaGroupMapper.mapToDto(entry.getKey(), weightFirstSemester, weightSecondSemester, criteria);
                criteriaGroupDTOS.add(criteriaGroupDTO);
            }

            CriteriaSectionDTO criteriaSectionDTO = new CriteriaSectionDTO(
                    null,
                    null,
                    criteriaSectionName,
                    criteriaSectionGradeWeightFirstSemester,
                    criteriaSectionGradeWeightSecondSemester,
                    criteriaGroupDTOS
                    );

            criteriaSectionDTOS.add(criteriaSectionDTO);
        }

        EvaluationCriteriaDTO evaluationCriteriaDTO = evaluationCriteriaMapper.mapToDto(evaluationCardTemplate);
        evaluationCriteriaDTO.criteriaSections().addAll(criteriaSectionDTOS);


        String content = objectMapper.writeValueAsString(evaluationCriteriaDTO);

        writer.write(content);
    }


    @Override
    public DataFeedType getType() {
        return DataFeedType.CRITERIA;
    }
}

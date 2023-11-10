package pl.edu.amu.wmi.service.exportdata.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
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

    private static final Double GRADE_WEIGHT_VALUE_ZERO = 0.0;

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

        EvaluationCriteriaDTO evaluationCriteriaDTO = consturctEvaluationCriteriaDTO(studyYear);

        String content = objectMapper.writeValueAsString(evaluationCriteriaDTO);

        writer.write(content);
    }

    private EvaluationCriteriaDTO consturctEvaluationCriteriaDTO(String studyYear) {
        EvaluationCardTemplate evaluationCardTemplate = evaluationCardTemplateRepositoryDAO.findByStudyYear(studyYear).orElseGet(EvaluationCardTemplate::new);

        List<CriteriaSection> criteriaSectionsFirstSemester = evaluationCardTemplate.getCriteriaSectionsFirstSemester();
        List<CriteriaSection> criteriaSectionsSecondSemester = evaluationCardTemplate.getCriteriaSectionsSecondSemester();

        Set<String> criteriaSectionNames = extractCriteriaSectionNames(criteriaSectionsFirstSemester, criteriaSectionsSecondSemester);

        List<CriteriaSectionDTO> criteriaSectionDTOs = new ArrayList<>();

        for (String criteriaSectionName : criteriaSectionNames) {
            CriteriaSectionDTO criteriaSectionDTO = createCriteriaSectionDTO(criteriaSectionName, criteriaSectionsFirstSemester, criteriaSectionsSecondSemester);
            criteriaSectionDTOs.add(criteriaSectionDTO);
        }

        EvaluationCriteriaDTO evaluationCriteriaDTO = evaluationCriteriaMapper.mapToDto(evaluationCardTemplate);
        evaluationCriteriaDTO.criteriaSections().addAll(criteriaSectionDTOs);
        return evaluationCriteriaDTO;
    }

    private CriteriaSectionDTO createCriteriaSectionDTO(String criteriaSectionName,
                                                        List<CriteriaSection> criteriaSectionsFirstSemester,
                                                        List<CriteriaSection> criteriaSectionsSecondSemester) {

        Map<String, List<CriteriaGroup>> criteriaGroups = new HashMap<>();

        CriteriaSection criteriaSectionFirstSemester = findCriteriaSectionByName(criteriaSectionName, criteriaSectionsFirstSemester);
        Long criteriaSectionFirstSemesterId = null;
        Double criteriaSectionGradeWeightFirstSemester = null;
        if (Objects.nonNull(criteriaSectionFirstSemester)) {
            criteriaSectionGradeWeightFirstSemester = criteriaSectionFirstSemester.getCriteriaSectionGradeWeight();
            criteriaSectionFirstSemesterId = criteriaSectionFirstSemester.getId();
            for (CriteriaGroup criteriaGroup : criteriaSectionFirstSemester.getCriteriaGroups()) {
                criteriaGroup.setSemester(Semester.SEMESTER_I);
                criteriaGroups.computeIfAbsent(criteriaGroup.getName(), k -> new ArrayList<>()).add(criteriaGroup);
            }
        }

        CriteriaSection criteriaSectionSecondSemester = findCriteriaSectionByName(criteriaSectionName, criteriaSectionsSecondSemester);
        Long criteriaSectionSecondSemesterId = null;
        Double criteriaSectionGradeWeightSecondSemester = null;
        if (Objects.nonNull(criteriaSectionSecondSemester)) {
            criteriaSectionGradeWeightSecondSemester = criteriaSectionSecondSemester.getCriteriaSectionGradeWeight();
            criteriaSectionSecondSemesterId = criteriaSectionSecondSemester.getId();
            for (CriteriaGroup criteriaGroup : criteriaSectionSecondSemester.getCriteriaGroups()) {
                criteriaGroup.setSemester(Semester.SEMESTER_II);
                criteriaGroups.computeIfAbsent(criteriaGroup.getName(), k -> new ArrayList<>()).add(criteriaGroup);
            }
        }

        List<CriteriaGroupDTO> criteriaGroupDTOS = new ArrayList<>();

        for (Map.Entry<String, List<CriteriaGroup>> entry : criteriaGroups.entrySet()) {
            CriteriaGroupDTO criteriaGroupDTO = createCriteriaGroupDTO(entry);
            criteriaGroupDTOS.add(criteriaGroupDTO);
        }

        return new CriteriaSectionDTO(
                criteriaSectionFirstSemesterId,
                criteriaSectionSecondSemesterId,
                criteriaSectionName,
                criteriaSectionGradeWeightFirstSemester,
                criteriaSectionGradeWeightSecondSemester,
                criteriaGroupDTOS
        );
    }
    private CriteriaGroupDTO createCriteriaGroupDTO(Map.Entry<String, List<CriteriaGroup>> entry) {
        Pair<Long, Double> criteriaGroupDataFirstSemester = getCriteriaGroupDataForSemester(entry, Semester.SEMESTER_I);
        Long idFirstSemester = criteriaGroupDataFirstSemester.getValue0();
        Double weightFirstSemester = criteriaGroupDataFirstSemester.getValue1();

        Pair<Long, Double> criteriaGroupDataSecondSemester = getCriteriaGroupDataForSemester(entry, Semester.SEMESTER_II);
        Long idSecondSemester = criteriaGroupDataSecondSemester.getValue0();
        Double weightSecondSemester = criteriaGroupDataSecondSemester.getValue1();

        List<Criterion> criteria = entry.getValue().get(0).getCriteria().stream().toList();

        return criteriaGroupMapper.mapToDto(
                entry.getKey(),
                idFirstSemester,
                idSecondSemester,
                weightFirstSemester,
                weightSecondSemester,
                criteria);
    }

    private Pair<Long, Double> getCriteriaGroupDataForSemester(Map.Entry<String, List<CriteriaGroup>> entry, Semester semester) {
        CriteriaGroup criteriaGroupForSemester = entry.getValue().stream()
                .filter(criteriaGroup -> Objects.equals(semester, criteriaGroup.getSemester()))
                .findFirst()
                .orElse(null);

        if (Objects.isNull(criteriaGroupForSemester)) {
            return new Pair<>(null, GRADE_WEIGHT_VALUE_ZERO);
        } else {
            return new Pair<>(criteriaGroupForSemester.getId(), criteriaGroupForSemester.getGradeWeight());
        }
    }

    private CriteriaSection findCriteriaSectionByName(String criteriaSectionName, List<CriteriaSection> criteriaSections) {
        return criteriaSections.stream()
                .filter(criteriaSection -> Objects.equals(criteriaSectionName, criteriaSection.getName()))
                .findFirst()
                .orElse(null);
    }

    private Set<String> extractCriteriaSectionNames(List<CriteriaSection> criteriaSectionsFirstSemester,
                                                    List<CriteriaSection> criteriaSectionsSecondSemester) {
        List<String> criteriaSectionFistSemesterNames = extractCriteriaSectionNamesForSemester(criteriaSectionsFirstSemester);
        List<String> criteriaSectionSecondSemesterNames = extractCriteriaSectionNamesForSemester(criteriaSectionsSecondSemester);
        return Stream.concat(criteriaSectionFistSemesterNames.stream(), criteriaSectionSecondSemesterNames.stream()).collect(Collectors.toSet());
    }

    private static List<String> extractCriteriaSectionNamesForSemester(List<CriteriaSection> criteriaSections) {
        return criteriaSections.stream()
                .map(CriteriaSection::getName)
                .toList();
    }

    @Override
    public DataFeedType getType() {
        return DataFeedType.CRITERIA;
    }
}

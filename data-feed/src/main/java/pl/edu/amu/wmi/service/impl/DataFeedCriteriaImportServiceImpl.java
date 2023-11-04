package pl.edu.amu.wmi.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.amu.wmi.dao.CriteriaGroupDAO;
import pl.edu.amu.wmi.dao.ScoringCriteriaDAO;
import pl.edu.amu.wmi.entity.CriteriaGroup;
import pl.edu.amu.wmi.entity.Criterion;
import pl.edu.amu.wmi.entity.ScoringCriteria;
import pl.edu.amu.wmi.enumerations.Semester;
import pl.edu.amu.wmi.mapper.CriteriaGroupMapper;
import pl.edu.amu.wmi.mapper.CriterionMapper;
import pl.edu.amu.wmi.mapper.ScoringCriteriaMapper;
import pl.edu.amu.wmi.model.CriteriaGroupDTO;
import pl.edu.amu.wmi.model.CriterionDTO;
import pl.edu.amu.wmi.model.ScoringCriteriaDTO;
import pl.edu.amu.wmi.model.enumeration.DataFeedType;
import pl.edu.amu.wmi.service.DataFeedImportService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class DataFeedCriteriaImportServiceImpl implements DataFeedImportService {

    private final CriteriaGroupMapper criteriaGroupMapper;
    private final CriterionMapper criterionMapper;
    private final ScoringCriteriaMapper scoringCriteriaMapper;
    private final CriteriaGroupDAO criteriaGroupDAO;
    private final ScoringCriteriaDAO scoringCriteriaDAO;

    public DataFeedCriteriaImportServiceImpl(CriteriaGroupMapper criteriaGroupMapper, CriterionMapper criterionMapper, ScoringCriteriaMapper scoringCriteriaMapper, CriteriaGroupDAO criteriaGroupDAO, ScoringCriteriaDAO scoringCriteriaDAO) {
        this.criteriaGroupMapper = criteriaGroupMapper;
        this.criterionMapper = criterionMapper;
        this.scoringCriteriaMapper = scoringCriteriaMapper;
        this.criteriaGroupDAO = criteriaGroupDAO;
        this.scoringCriteriaDAO = scoringCriteriaDAO;
    }

    @Override
    public DataFeedType getType() {
        return DataFeedType.NEW_CRITERIA;
    }

    @Override
    @Transactional
    public void saveRecords(MultipartFile data, String studyYear) throws Exception {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<CriteriaGroupDTO> criteriaGroups = objectMapper.readValue(data.getInputStream(), new TypeReference<List<CriteriaGroupDTO>>(){});
            // TODO: 11/2/2023 check if any criteria exist for study year
            // TODO: 11/2/2023 add study year to some criterion entity
            // TODO: what when weight is equal to zero?? we shouldn't add such criterion
            criteriaGroups.forEach(this::saveCriteriaGroup);
        } catch (Exception exception) {
            log.error("Exception during parsing the criteria");
            throw exception;
        }
    }

    private void saveCriteriaGroup(CriteriaGroupDTO criteriaGroupDTO) {
        CriteriaGroup firstSemesterCriteriaGroup = criteriaGroupMapper.mapToEntityForFirstSemester(criteriaGroupDTO);
        CriteriaGroup secondSemesterCriteriaGroup = criteriaGroupMapper.mapToEntityForSecondSemester(criteriaGroupDTO);

        for (CriterionDTO criterionDTO : criteriaGroupDTO.criteria()) {
            Set<ScoringCriteria> savedScoringCriteria = saveScoringCriteria(criterionDTO.scoringCriteria());

            Criterion criterionForFirstSemester = createCriterion(criterionDTO, savedScoringCriteria, Semester.SEMESTER_I);
            firstSemesterCriteriaGroup.getCriteria().add(criterionForFirstSemester);

            Criterion criterionForSecondSemester = createCriterion(criterionDTO, savedScoringCriteria, Semester.SEMESTER_II);
            secondSemesterCriteriaGroup.getCriteria().add(criterionForSecondSemester);
        }

        criteriaGroupDAO.save(firstSemesterCriteriaGroup);
        criteriaGroupDAO.save(secondSemesterCriteriaGroup);
    }

    private Criterion createCriterion(CriterionDTO criterionDTO, Set<ScoringCriteria> savedScoringCriteria, Semester semesterI) {
        Criterion criterion = switch (semesterI) {
            case SEMESTER_I -> criterionMapper.mapToEntityForFirstSemester(criterionDTO);
            case SEMESTER_II -> criterionMapper.mapToEntityForSecondSemester(criterionDTO);
        };
        criterion.setScoringCriteria(savedScoringCriteria);
        return criterion;
    }

    private Set<ScoringCriteria> saveScoringCriteria(List<ScoringCriteriaDTO> scoringCriteriaDTOs) {
        List<ScoringCriteria> scoringCriteria = scoringCriteriaMapper.mapToEntitiesList(scoringCriteriaDTOs);
        return new HashSet<>(scoringCriteriaDAO.saveAll(scoringCriteria));
    }
}

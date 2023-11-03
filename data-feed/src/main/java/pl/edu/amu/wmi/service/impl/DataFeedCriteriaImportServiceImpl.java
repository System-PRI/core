package pl.edu.amu.wmi.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.amu.wmi.dao.CriteriaGroupDAO;
import pl.edu.amu.wmi.entity.CriteriaGroup;
import pl.edu.amu.wmi.mapper.CriteriaGroupMapper;
import pl.edu.amu.wmi.model.CriteriaGroupDTO;
import pl.edu.amu.wmi.model.enumeration.DataFeedType;
import pl.edu.amu.wmi.service.DataFeedImportService;

import java.util.List;

@Component
@Slf4j
public class DataFeedCriteriaImportServiceImpl implements DataFeedImportService {

    private final CriteriaGroupMapper criteriaGroupMapper;
    private final CriteriaGroupDAO criteriaGroupDAO;

    public DataFeedCriteriaImportServiceImpl(CriteriaGroupMapper criteriaGroupMapper, CriteriaGroupDAO criteriaGroupDAO) {
        this.criteriaGroupMapper = criteriaGroupMapper;
        this.criteriaGroupDAO = criteriaGroupDAO;
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
            // what when weight is equal to zero?? we shouldn't add such criterion
            for (CriteriaGroupDTO criteriaGroupDTO : criteriaGroups) {
                CriteriaGroup firstSemesterEntity = criteriaGroupMapper.mapToEntityForFirstSemester(criteriaGroupDTO);
                CriteriaGroup secondSemesterEntity = criteriaGroupMapper.mapToEntityForSecondSemester(criteriaGroupDTO);
                criteriaGroupDAO.save(firstSemesterEntity);
                criteriaGroupDAO.save(secondSemesterEntity);

            }
        } catch (Exception exception) {
            log.error("Exception during parsing the criteria");
            throw exception;
        }
    }
}

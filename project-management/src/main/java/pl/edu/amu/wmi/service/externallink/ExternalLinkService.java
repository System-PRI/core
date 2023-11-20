package pl.edu.amu.wmi.service.externallink;

import pl.edu.amu.wmi.model.externallink.ExternalLinkDTO;
import pl.edu.amu.wmi.model.externallink.ExternalLinkDataDTO;

import java.util.List;
import java.util.Set;

public interface ExternalLinkService {

    List<ExternalLinkDataDTO> findAll();

    Set<ExternalLinkDTO> findByProjectId(Long id);

    Set<ExternalLinkDTO> updateExternalLinks(Long projectId, Set<ExternalLinkDTO> externalLinks);

    Set<String> findDefinitionHeadersByStudyYear(String studyYear);

}
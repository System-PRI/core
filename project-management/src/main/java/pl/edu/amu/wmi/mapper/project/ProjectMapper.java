package pl.edu.amu.wmi.mapper.project;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pl.edu.amu.wmi.entity.Project;
import pl.edu.amu.wmi.enumerations.AcceptanceStatus;
import pl.edu.amu.wmi.mapper.externallink.ExternalLinkMapper;
import pl.edu.amu.wmi.mapper.grade.PointsMapper;
import pl.edu.amu.wmi.model.project.ProjectDTO;
import pl.edu.amu.wmi.model.project.ProjectDetailsDTO;

import java.util.List;

import static pl.edu.amu.wmi.enumerations.AcceptanceStatus.ACCEPTED;
import static pl.edu.amu.wmi.enumerations.AcceptanceStatus.CONFIRMED;

@Mapper(componentModel = "spring", uses = { SupervisorProjectMapper.class, ExternalLinkMapper.class, PointsMapper.class })
public interface ProjectMapper {

    @Mapping(target = "supervisor", ignore = true)
    Project mapToEntity(ProjectDetailsDTO dto);

    @Mapping(target = "accepted", source = "acceptanceStatus", qualifiedByName = "AcceptedToBoolean")
    @Mapping(target = "confirmed", source = "acceptanceStatus", qualifiedByName = "ConfirmedToBoolean")
    ProjectDetailsDTO mapToDto(Project project);


    @Mapping(target = "supervisor", source = "entity.supervisor")
    @Mapping(target = "accepted", source = "acceptanceStatus", qualifiedByName = "AcceptedToBoolean")
    @Mapping(target = "pointsFirstSemester", source = "evaluationCard.totalPointsFirstSemester", qualifiedByName = "PointsToPercent")
    @Mapping(target = "pointsSecondSemester", source = "evaluationCard.totalPointsSecondSemester", qualifiedByName = "PointsToPercent")
    @Mapping(target = "criteriaMet", source = "evaluationCard.disqualified", qualifiedByName = "DisqualifiedToCriteriaMet")
    ProjectDTO mapToProjectDto(Project entity);

    List<ProjectDTO> mapToDtoList(List<Project> entityList);

    @Named("AcceptedToBoolean")
    default boolean mapAccepted(AcceptanceStatus status) {
        return status.equals(ACCEPTED);
    }

    @Named("ConfirmedToBoolean")
    default boolean mapConfirmed(AcceptanceStatus status) {
        return status.equals(CONFIRMED) || status.equals(ACCEPTED);
    }

    @Named("DisqualifiedToCriteriaMet")
    default boolean mapCriteriaMet(boolean isDisqualified) {
        return !isDisqualified;
    }

}
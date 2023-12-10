package pl.edu.amu.wmi.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.amu.wmi.entity.ProjectDefense;

import java.util.List;

@Repository
public interface ProjectDefenseDAO extends JpaRepository<ProjectDefense, Long> {
    List<ProjectDefense> findAllByStudyYear(String studyYear);

    List<ProjectDefense> findAllByProjectId(Long projectId);

}

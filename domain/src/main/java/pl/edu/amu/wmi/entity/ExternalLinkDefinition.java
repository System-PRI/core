package pl.edu.amu.wmi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "EXTERNAL_LINK_DEFINITION")
public class ExternalLinkDefinition extends AbstractEntity {

    private String name;

    private String columnHeader;

    private LocalDate deadline;

    @ManyToOne
    @JoinColumn(
            name = "STUDY_YEAR",
            referencedColumnName = "STUDY_YEAR"
    )
    private StudyYear studyYear;

}

package pl.edu.amu.wmi.model.project;

import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.amu.wmi.enumerations.ProjectRole;

@Data
@NoArgsConstructor
public class StudentDTO {

    private String indexNumber;

    private ProjectRole role;

    private String name;

    private String email;

    private boolean accepted;

}

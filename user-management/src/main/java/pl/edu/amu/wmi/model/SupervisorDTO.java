package pl.edu.amu.wmi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SupervisorDTO {

    private String surname;

    private String name;

    private String email;

    private String indexNumber;

}

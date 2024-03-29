package pl.edu.amu.wmi.model.externallink;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class ExternalLinkDTO {

    @NotNull
    private String id;

    private String url;

    private String name;

    private String columnHeader;

    private LocalDate deadline;

}


package pl.edu.amu.wmi.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class DefenseScheduleConfigDTO {

    private DateRangeDTO dateRange;

    private Integer slotDuration;

    private TimeRangeDTO timeRange;

    public DefenseScheduleConfigDTO() {
        this.dateRange = new DateRangeDTO();
        this.timeRange = new TimeRangeDTO();
    }

    @Data
    public static class DateRangeDTO {
        @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="dd/MM/yyyy")
        private LocalDate start;
        @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="dd/MM/yyyy")
        private LocalDate end;
    }

    @Data
    public static class TimeRangeDTO {
        @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="HH:mm")
        private LocalTime start;
        @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="HH:mm")
        private LocalTime end;
    }
}



package pl.edu.amu.wmi.controller;

import com.opencsv.exceptions.CsvException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.amu.wmi.model.enumeration.DataFeedType;
import pl.edu.amu.wmi.service.DataFeedExportService;
import pl.edu.amu.wmi.service.DataFeedImportService;
import pl.edu.amu.wmi.service.impl.DataFeedServiceFactory;

import java.io.IOException;


@RestController
@RequestMapping("/data")
@Slf4j
public class DataFeedController {

    private final DataFeedServiceFactory dataFeedServiceFactory;

    private final DataFeedExportService dataFeedExportService;

    public DataFeedController(DataFeedServiceFactory dataFeedServiceFactory, DataFeedExportService dataFeedExportService) {
        this.dataFeedServiceFactory = dataFeedServiceFactory;
        this.dataFeedExportService = dataFeedExportService;
    }

    @PostMapping("/import/student")
    public ResponseEntity<Void> createStudents(@RequestHeader("study-year") String studyYear,
                                               @RequestHeader("user-index-number") String userIndexNumber, @RequestParam MultipartFile data) throws Exception {
        DataFeedImportService service = DataFeedServiceFactory.getService(DataFeedType.NEW_STUDENT);
        service.saveRecords(data, studyYear);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/import/supervisor")
    public ResponseEntity<Void> createSupervisors(@RequestHeader("study-year") String studyYear,
                                                 @RequestHeader("user-index-number") String userIndexNumber,@RequestParam MultipartFile data) throws Exception {
        DataFeedImportService service = DataFeedServiceFactory.getService(DataFeedType.NEW_SUPERVISOR);
        service.saveRecords(data, studyYear);
        return ResponseEntity.ok().build();
    }

    @GetMapping("export/student")
    public void exportStudentsData(@RequestHeader("study-year") String studyYear,
                                   @RequestHeader("user-index-number") String userIndexNumber, HttpServletResponse servletResponse) throws IOException, CsvException {
        servletResponse.setContentType("text/csv");
        servletResponse.setCharacterEncoding("UTF-8");
        servletResponse.addHeader("Content-Disposition", "attachment; filename=\"students.csv\"");
        dataFeedExportService.exportData(servletResponse.getWriter(), studyYear);
    }

}

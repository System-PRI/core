package pl.edu.amu.wmi.controller.supervisoravailability;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.edu.amu.wmi.model.supervisordefense.SupervisorDefenseAssignmentDTO;
import pl.edu.amu.wmi.service.supervisoravailability.SupervisorAvailabilityService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/schedule/availability/supervisor")
public class SupervisorAvailabilityController {

    private final SupervisorAvailabilityService supervisorAvailabilityService;

    @Autowired
    public SupervisorAvailabilityController(SupervisorAvailabilityService supervisorAvailabilityService) {
        this.supervisorAvailabilityService = supervisorAvailabilityService;
    }

    @Secured({"COORDINATOR", "SUPERVISOR"})
    @PutMapping("")
    public ResponseEntity<Void> putSupervisorAvailability(
            @RequestHeader("study-year") String studyYear,
            @Valid @RequestBody Map<String, List<SupervisorDefenseAssignmentDTO>> supervisorAvailabilitySurvey) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        supervisorAvailabilityService.putSupervisorAvailability(studyYear, userDetails.getUsername(), supervisorAvailabilitySurvey);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}

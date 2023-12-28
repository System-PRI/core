package pl.edu.amu.wmi.service.project.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.amu.wmi.dao.SupervisorDAO;
import pl.edu.amu.wmi.exception.BusinessException;
import pl.edu.amu.wmi.mapper.project.SupervisorProjectMapper;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class SupervisorProjectServiceImplTest {

    @Mock
    private SupervisorDAO supervisorDAO;

    @Mock
    private SupervisorProjectMapper supervisorProjectMapper;

    @InjectMocks
    private SupervisorProjectServiceImpl supervisorProjectService;

    @Test
    void getSupervisorsAvailability() {
    }

    @Test
    void isSupervisorAvailable() {
        Mockito.when(supervisorDAO.findByStudyYearAndUserData_IndexNumber(any(), any())).thenReturn(null);

        assertThrows(BusinessException.class, () -> this.supervisorProjectService.isSupervisorAvailable(any(), any()));
    }

    @Test
    void updateSupervisorsAvailability() {
    }
}

package pl.edu.amu.wmi.service;

import pl.edu.amu.wmi.entity.AbstractEntity;
import pl.edu.amu.wmi.model.SupervisorDTO;

import java.util.List;

public interface SupervisorService {

    List<SupervisorDTO> findAll();

    AbstractEntity findById(Long id);
}
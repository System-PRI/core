package pl.edu.amu.wmi.mapper.project;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.edu.amu.wmi.entity.Student;
import pl.edu.amu.wmi.model.project.StudentDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StudentProjectMapper {

    @Mapping(target = "name", expression = "java(String.format(\"%s %s\", entity.getUserData().getFirstName(), entity.getUserData().getLastName()))")
    @Mapping(target = "email", source = "userData.email")
    @Mapping(target = "indexNumber", source = "userData.indexNumber")
    @Mapping(target = "role", ignore = true)
    StudentDTO mapToDto(Student entity);

    List<StudentDTO> mapToDtoList(List<Student> entityList);

}

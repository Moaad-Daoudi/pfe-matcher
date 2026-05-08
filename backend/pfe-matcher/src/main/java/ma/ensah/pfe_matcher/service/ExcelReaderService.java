package ma.ensah.pfe_matcher.service;

import ma.ensah.pfe_matcher.model.Professor;
import ma.ensah.pfe_matcher.model.Student;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ExcelReaderService {
    List<Student> readStudent(MultipartFile file) throws Exception;
    List<Professor> readProfessors(MultipartFile file) throws Exception;
}
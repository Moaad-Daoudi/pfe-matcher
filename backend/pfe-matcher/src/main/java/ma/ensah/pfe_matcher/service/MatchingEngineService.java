package ma.ensah.pfe_matcher.service;

import ma.ensah.pfe_matcher.model.Assignment;
import ma.ensah.pfe_matcher.model.Professor;
import ma.ensah.pfe_matcher.model.Student;
import java.util.List;

public interface MatchingEngineService {
    List<Assignment> assignStudentsToProfs(List<Student> students, List<Professor> professors);
}
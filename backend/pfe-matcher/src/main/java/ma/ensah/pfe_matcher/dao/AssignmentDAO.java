package ma.ensah.pfe_matcher.dao;

import ma.ensah.pfe_matcher.model.Assignment;
import ma.ensah.pfe_matcher.model.Professor;
import java.util.List;
import java.util.Map;

public interface AssignmentDAO {
    void saveAll(List<Assignment> newAssignments);
    void saveJuryProfessors(List<Professor> professors);
    List<Assignment> getAll();
    List<Professor> getJuryProfessors();
    void clear();
    Map<String, Integer> getProfessorLoads();
}

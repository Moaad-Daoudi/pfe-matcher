package ma.ensah.pfe_matcher.dao;

import ma.ensah.pfe_matcher.model.Assignment;
import java.util.List;
import java.util.Map;

public interface AssignmentDAO {
    void saveAll(List<Assignment> newAssignments);
    List<Assignment> getAll();
    void clear();
    Map<String, Integer> getProfessorLoads();
}
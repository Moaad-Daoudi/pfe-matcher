package ma.ensah.pfe_matcher.dao;

import ma.ensah.pfe_matcher.model.Assignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AssignmentDAOImpl implements AssignmentDAO {

    private final List<Assignment> assignments = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(AssignmentDAOImpl.class);

    @Override
    public void saveAll(List<Assignment> newAssignments) {
        logger.info("Saving {} new assignments to memory. Previous size: {}", newAssignments.size(), this.assignments.size());
        this.assignments.addAll(newAssignments);
    }

    @Override
    public List<Assignment> getAll() {
        return new ArrayList<>(assignments); // Return a copy to prevent external modification
    }

    @Override
    public void clear() {
        assignments.clear();
        logger.info("Storage cleared.");
    }

    @Override
    public Map<String, Integer> getProfessorLoads() {
        Map<String, Integer> loads = new HashMap<>();
        for (Assignment a : assignments) {
            String profName = a.getProfessor().getLastname();
            loads.put(profName, loads.getOrDefault(profName, 0) + 1);
        }
        return loads;
    }
}
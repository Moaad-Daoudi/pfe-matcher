package ma.ensah.pfe_matcher.dao;

import ma.ensah.pfe_matcher.model.Assignment;
import ma.ensah.pfe_matcher.model.Professor;
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
    private final List<Professor> juryProfessors = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(AssignmentDAOImpl.class);

    @Override
    public void saveAll(List<Assignment> newAssignments) {
        logger.info("Saving {} new assignments to memory. Previous size: {}", newAssignments.size(), this.assignments.size());
        this.assignments.addAll(newAssignments);
    }

    @Override
    public void saveJuryProfessors(List<Professor> professors) {
        if (professors == null || professors.isEmpty()) {
            return;
        }
        int before = this.juryProfessors.size();
        for (Professor p : professors) {
            if (p == null) {
                continue;
            }
            if (this.juryProfessors.stream().noneMatch(existing -> sameProfessor(existing, p))) {
                this.juryProfessors.add(p);
            }
        }
        logger.info("Saved jury pool professors. Added: {}. Total jury pool size: {}",
                this.juryProfessors.size() - before, this.juryProfessors.size());
    }

    @Override
    public List<Assignment> getAll() {
        return new ArrayList<>(assignments); // Return a copy to prevent external modification
    }

    @Override
    public List<Professor> getJuryProfessors() {
        return new ArrayList<>(juryProfessors);
    }

    @Override
    public void clear() {
        assignments.clear();
        juryProfessors.clear();
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

    private boolean sameProfessor(Professor a, Professor b) {
        String aLast = safe(a.getLastname());
        String aFirst = safe(a.getFirstname());
        String aDept = safe(a.getDepartment());
        String bLast = safe(b.getLastname());
        String bFirst = safe(b.getFirstname());
        String bDept = safe(b.getDepartment());

        return aLast.equalsIgnoreCase(bLast)
                && aFirst.equalsIgnoreCase(bFirst)
                && aDept.equalsIgnoreCase(bDept);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}

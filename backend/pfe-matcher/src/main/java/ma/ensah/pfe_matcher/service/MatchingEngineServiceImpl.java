package ma.ensah.pfe_matcher.service;

import ma.ensah.pfe_matcher.dao.AssignmentDAO;
import ma.ensah.pfe_matcher.model.Assignment;
import ma.ensah.pfe_matcher.model.Professor;
import ma.ensah.pfe_matcher.model.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchingEngineServiceImpl implements MatchingEngineService {

    private static final Logger logger = LoggerFactory.getLogger(MatchingEngineService.class);

    @Autowired
    private AssignmentDAO storageDAO;

    // Define allowed departments here (or load from application.properties)
    private static final Set<String> ALLOWED_DEPARTMENTS = new HashSet<>(
            Arrays.asList("Informatique", "Mathématique")
    );

    @Override
    public List<Assignment> assignStudentsToProfs(List<Student> students, List<Professor> professors) {
        // FILTER: Only keep professors from allowed departments
        List<Professor> validProfessors = professors.stream()
                .filter(p -> ALLOWED_DEPARTMENTS.stream()
                        .anyMatch(dept -> dept.equalsIgnoreCase(p.getDepartment())))
                .collect(Collectors.toList());

        if (validProfessors.isEmpty()) {
            throw new RuntimeException("No professors found in valid departments (Informatique/Mathématique)");
        }

        int dynamicMax = (int) Math.ceil((double) students.size() / validProfessors.size()) + 1;
        validProfessors.forEach(p -> p.setMaxCapacity(dynamicMax));
        logger.info("Dynamic Capacity Calculated: {} students / {} profs = Max {} per prof",
                students.size(), validProfessors.size(), dynamicMax);

        List<Student> shuffled = new ArrayList<>(students);
        Collections.shuffle(shuffled);

        Map<String, Integer> totalLoad = new HashMap<>(storageDAO.getProfessorLoads());
        List<Assignment> newAssignments = new ArrayList<>();

        for (Student student : shuffled) {
            // Find professor with lowest load that is under their NEW dynamicMax
            Professor chosen = validProfessors.stream()
                    .filter(p -> totalLoad.getOrDefault(p.getLastname(), 0) < p.getMaxCapacity())
                    .min(Comparator.comparingInt(p -> totalLoad.getOrDefault(p.getLastname(), 0)))
                    .orElseThrow(() -> new RuntimeException("All professors reached calculated capacity"));

            String profKey = chosen.getLastname();
            totalLoad.put(profKey, totalLoad.getOrDefault(profKey, 0) + 1);

            newAssignments.add(new Assignment(UUID.randomUUID().toString(), student, chosen));
        }

        storageDAO.saveAll(newAssignments);
        return newAssignments;
    }
}
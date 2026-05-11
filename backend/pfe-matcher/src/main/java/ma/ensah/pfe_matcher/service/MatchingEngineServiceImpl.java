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

    @Autowired 
    private ConfigService configService;

    // Define allowed departments here (or load from application.properties)
//     private static final Set<String> ALLOWED_DEPARTMENTS = new HashSet<>(
//             Arrays.asList("Informatique", "Mathématique")
//     );

    @Override
    public List<Assignment> assignStudentsToProfs(List<Student> students, List<Professor> professors) {
        List<String> allowedDepts = configService.getAllowedDepartments();

        List<Professor> validProfessors = professors.stream()
                .filter(p -> allowedDepts.stream()
                        .anyMatch(d -> p.getDepartment().toUpperCase().contains(d.toUpperCase())))
                .collect(Collectors.toList());

        if (validProfessors.isEmpty()) throw new RuntimeException("Aucun professeur trouvé.");

        int dynamicMax = (int) Math.ceil((double) students.size() / validProfessors.size());
        validProfessors.forEach(p -> p.setMaxCapacity(dynamicMax));

        List<Student> shuffled = new ArrayList<>(students);
        Collections.shuffle(shuffled);

        Map<String, Integer> totalLoad = new HashMap<>(storageDAO.getProfessorLoads());
        List<Assignment> newAssignments = new ArrayList<>();

        for (Student student : shuffled) {
            Professor chosen = validProfessors.stream()
                    .filter(p -> totalLoad.getOrDefault(p.getLastname(), 0) < p.getMaxCapacity())
                    .min(Comparator.comparingInt(p -> totalLoad.getOrDefault(p.getLastname(), 0)))
                    .orElseThrow(() -> new RuntimeException("Capacité maximale atteinte"));

            totalLoad.put(chosen.getLastname(), totalLoad.getOrDefault(chosen.getLastname(), 0) + 1);
            newAssignments.add(new Assignment(UUID.randomUUID().toString(), student, chosen));
        }
        storageDAO.saveAll(newAssignments);
        return newAssignments;
    }
}
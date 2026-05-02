package ma.ensah.pfe_matcher.service;

import ma.ensah.pfe_matcher.model.Assignment;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    public Map<String, Object> getStats(List<Assignment> assignments) {
        Map<String, Object> stats = new HashMap<>();

        // Students per prof
        stats.put("studentsPerProf", assignments.stream()
                .collect(Collectors.groupingBy(a -> a.getProfessor().getLastname(), Collectors.counting())));

        // Count per field
        stats.put("fieldStats", assignments.stream()
                .collect(Collectors.groupingBy(a -> a.getStudent().getField(), Collectors.counting())));

        return stats;
    }
}

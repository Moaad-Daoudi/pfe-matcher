package ma.ensah.pfe_matcher.validation;

import ma.ensah.pfe_matcher.model.Assignment;
import ma.ensah.pfe_matcher.validation.ValidationRule;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FairnessRule implements ValidationRule {

    @Override
    public String validate(List<Assignment> assignments) {
        if (assignments == null || assignments.isEmpty()) return null;

        // Calculate how many students each professor has
        Map<String, Long> loadPerProf = assignments.stream()
                .collect(Collectors.groupingBy(a -> a.getProfessor().getLastname(), Collectors.counting()));

        // Calculate the global average
        double totalStudents = assignments.size();
        double totalProfs = loadPerProf.size();
        double averageLoad = totalStudents / totalProfs;

        // Define the "Fair" range based on the average
        // Example: If avg is 4.2, then 4 and 5 are "Fair". 3 and 6 are "Unfair".
        int minAllowed = (int) Math.floor(averageLoad);
        int maxAllowed = (int) Math.ceil(averageLoad);

        // Check for violations
        StringBuilder violations = new StringBuilder();

        for (Map.Entry<String, Long> entry : loadPerProf.entrySet()) {
            long count = entry.getValue();

            // If the count is strictly less than floor or strictly greater than ceil
            if (count < minAllowed || count > maxAllowed) {
                violations.append("Alerte Équité : Prof. ").append(entry.getKey())
                        .append(" a ").append(count)
                        .append(" étudiant(s). La moyenne est de ")
                        .append(String.format("%.2f", averageLoad))
                        .append(". Écart détecté. ");
            }
        }

        return violations.length() > 0 ? violations.toString() : null;
    }
}
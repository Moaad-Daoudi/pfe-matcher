package ma.ensah.pfe_matcher.model;

import java.util.List;
import java.util.Map;

public class AssignmentResult {
    private List<Assignment> assignments;
    private List<String> violations; // The anomalies list
    private Map<String, Object> stats; // Dashboard data

    public AssignmentResult() {
    }

    public AssignmentResult(List<Assignment> assignments, List<String> violations, Map<String, Object> stats) {
        this.assignments = assignments;
        this.violations = violations;
        this.stats = stats;
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<Assignment> assignments) {
        this.assignments = assignments;
    }

    public List<String> getViolations() {
        return violations;
    }

    public void setViolations(List<String> violations) {
        this.violations = violations;
    }

    public Map<String, Object> getStats() {
        return stats;
    }

    public void setStats(Map<String, Object> stats) {
        this.stats = stats;
    }
}

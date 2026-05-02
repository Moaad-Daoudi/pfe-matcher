package ma.ensah.pfe_matcher.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class AssignmentResult {
    private List<Assignment> assignments;
    private List<String> violations; // The anomalies list
    private Map<String, Object> stats; // Dashboard data
}

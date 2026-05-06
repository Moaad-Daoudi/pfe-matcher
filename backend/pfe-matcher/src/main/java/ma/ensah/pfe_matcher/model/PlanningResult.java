package ma.ensah.pfe_matcher.model;

import java.util.List;
import java.util.Map;

public class PlanningResult {
    private List<Soutenance> soutenances;
    private List<String> violations;
    private Map<String, Object> stats;
    private String pdfFileName;

    public PlanningResult() {
    }

    public PlanningResult(List<Soutenance> soutenances, List<String> violations, Map<String, Object> stats, String pdfFileName) {
        this.soutenances = soutenances;
        this.violations = violations;
        this.stats = stats;
        this.pdfFileName = pdfFileName;
    }

    public List<Soutenance> getSoutenances() {
        return soutenances;
    }

    public void setSoutenances(List<Soutenance> soutenances) {
        this.soutenances = soutenances;
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

    public String getPdfFileName() {
        return pdfFileName;
    }

    public void setPdfFileName(String pdfFileName) {
        this.pdfFileName = pdfFileName;
    }
}

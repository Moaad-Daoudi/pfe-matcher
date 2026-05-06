package ma.ensah.pfe_matcher.service;

import ma.ensah.pfe_matcher.model.PlanningRequest;
import ma.ensah.pfe_matcher.model.Soutenance;

import java.util.List;
import java.util.Map;

public interface PlanningGenerationService {
    List<Soutenance> generatePlanning(PlanningRequest request);
    Map<String, Object> buildStats(List<Soutenance> soutenances, List<String> violations);
}

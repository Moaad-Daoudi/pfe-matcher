package ma.ensah.pfe_matcher.service;

import ma.ensah.pfe_matcher.model.Soutenance;
import ma.ensah.pfe_matcher.validation.PlanningValidationRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlanningValidationService {

    @Autowired
    private List<PlanningValidationRule> rules;

    public List<String> runAll(List<Soutenance> soutenances) {
        List<String> violations = new ArrayList<>();
        for (PlanningValidationRule rule : rules) {
            violations.addAll(rule.validate(soutenances));
        }
        return violations;
    }
}

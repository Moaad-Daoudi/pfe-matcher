package ma.ensah.pfe_matcher.validation;

import java.util.List;
import ma.ensah.pfe_matcher.model.Soutenance;

public interface PlanningValidationRule {
    List<String> validate(List<Soutenance> soutenances);
}

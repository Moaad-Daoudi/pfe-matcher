package ma.ensah.pfe_matcher.validation;

import ma.ensah.pfe_matcher.model.Assignment;

import java.util.List;

public interface ValidationRule {
    String validate(List<Assignment> assignments);
}

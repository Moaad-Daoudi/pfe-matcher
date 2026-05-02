package ma.ensah.pfe_matcher.service;

import ma.ensah.pfe_matcher.model.Assignment;
import ma.ensah.pfe_matcher.validation.ValidationRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ValidationService {
    @Autowired
    private List<ValidationRule> rules; // Spring automatically collects ALL beans that implement ValidationRule!

    public List<String> runAll(List<Assignment> assignments) {
        return rules.stream()
                .map(rule -> rule.validate(assignments))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}

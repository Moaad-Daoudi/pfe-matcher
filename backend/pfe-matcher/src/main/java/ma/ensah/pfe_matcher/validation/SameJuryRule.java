package ma.ensah.pfe_matcher.validation;

import ma.ensah.pfe_matcher.model.Professor;
import ma.ensah.pfe_matcher.model.Soutenance;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class SameJuryRule implements PlanningValidationRule {

    @Override
    public List<String> validate(List<Soutenance> soutenances) {
        List<String> violations = new ArrayList<>();
        for (Soutenance s : soutenances) {
            Professor enc = s.getEncadrant();
            Professor j1 = s.getJury1();
            Professor j2 = s.getJury2();

            if (sameProfessor(enc, j1) || sameProfessor(enc, j2) || sameProfessor(j1, j2)) {
                violations.add("Meme jury conflict: double emploi des roles de professeur dans soutenance id " + s.getId() + ".");
            }
        }
        return violations;
    }

    private boolean sameProfessor(Professor a, Professor b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.getId() != null && b.getId() != null) {
            return a.getId().equalsIgnoreCase(b.getId());
        }
        return Objects.equals(safe(a.getLastname()), safe(b.getLastname()))
                && Objects.equals(safe(a.getFirstname()), safe(b.getFirstname()));
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}

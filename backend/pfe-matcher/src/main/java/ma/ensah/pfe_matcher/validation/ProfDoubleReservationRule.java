package ma.ensah.pfe_matcher.validation;

import ma.ensah.pfe_matcher.model.Professor;
import ma.ensah.pfe_matcher.model.Soutenance;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class ProfDoubleReservationRule implements PlanningValidationRule {

    @Override
    public List<String> validate(List<Soutenance> soutenances) {
        List<String> violations = new ArrayList<>();
        for (int i = 0; i < soutenances.size(); i++) {
            for (int j = i + 1; j < soutenances.size(); j++) {
                Soutenance a = soutenances.get(i);
                Soutenance b = soutenances.get(j);

                if (!a.getDate().equals(b.getDate()) || !overlaps(a, b)) {
                    continue;
                }

                List<Professor> profsA = new ArrayList<>();
                profsA.add(a.getEncadrant());
                profsA.add(a.getJury1());
                profsA.add(a.getJury2());

                List<Professor> profsB = new ArrayList<>();
                profsB.add(b.getEncadrant());
                profsB.add(b.getJury1());
                profsB.add(b.getJury2());

                for (Professor pa : profsA) {
                    for (Professor pb : profsB) {
                        if (sameProfessor(pa, pb)) {
                            violations.add("Professor double booking: " + fullName(pa)
                                    + " est prévu dans deux soutenances a " + a.getDate()
                                    + " à des moments qui se chevauchent.");
                        }
                    }
                }
            }
        }
        return violations;
    }

    private boolean overlaps(Soutenance a, Soutenance b) {
        return a.getStartTime().isBefore(b.getEndTime()) && b.getStartTime().isBefore(a.getEndTime());
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

    private String fullName(Professor p) {
        return safe(p.getLastname()) + " " + safe(p.getFirstname());
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}

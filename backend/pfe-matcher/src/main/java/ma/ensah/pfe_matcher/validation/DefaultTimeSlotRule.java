package ma.ensah.pfe_matcher.validation;

import ma.ensah.pfe_matcher.model.Soutenance;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DefaultTimeSlotRule implements PlanningValidationRule {

    private static final LocalTime MORNING_START = LocalTime.of(9, 0);
    private static final LocalTime MORNING_END = LocalTime.of(12, 0);
    private static final LocalTime AFTERNOON_START = LocalTime.of(14, 0);
    private static final LocalTime AFTERNOON_END = LocalTime.of(18, 0);

    @Override
    public List<String> validate(List<Soutenance> soutenances) {
        List<String> violations = new ArrayList<>();
        for (Soutenance s : soutenances) {
            boolean inMorning = !s.getStartTime().isBefore(MORNING_START) && !s.getEndTime().isAfter(MORNING_END);
            boolean inAfternoon = !s.getStartTime().isBefore(AFTERNOON_START) && !s.getEndTime().isAfter(AFTERNOON_END);

            if (!inMorning && !inAfternoon) {
                violations.add("Violation du créneau horaire : soutenance id " + s.getId()
                        + " est en dehors des fenetres par defaut (09:00-12:00, 14:00-18:00).");
            }
        }
        return violations;
    }
}

package ma.ensah.pfe_matcher.validation;

import ma.ensah.pfe_matcher.model.Soutenance;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SalleOverlapRule implements PlanningValidationRule {

	@Override
	public List<String> validate(List<Soutenance> soutenances) {
		List<String> violations = new ArrayList<>();
		for (int i = 0; i < soutenances.size(); i++) {
			for (int j = i + 1; j < soutenances.size(); j++) {
				Soutenance a = soutenances.get(i);
				Soutenance b = soutenances.get(j);

				if (!a.getDate().equals(b.getDate())) {
					continue;
				}
				if (!a.getSalle().equalsIgnoreCase(b.getSalle())) {
					continue;
				}
				if (!overlaps(a, b)) {
					continue;
				}

				violations.add("salle overlap: salle " + a.getSalle() + " et utilise deux fois " + a.getDate() + " ("
						+ a.getStartTime() + "-" + a.getEndTime() + " et " + b.getStartTime() + "-" + b.getEndTime()
						+ ").");
			}
		}
		return violations;
	}

	private boolean overlaps(Soutenance a, Soutenance b) {
		return a.getStartTime().isBefore(b.getEndTime()) && b.getStartTime().isBefore(a.getEndTime());
	}
}

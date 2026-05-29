package ma.ensah.pfe_matcher.service;

import ma.ensah.pfe_matcher.dao.AssignmentDAO;
import ma.ensah.pfe_matcher.model.Assignment;
import ma.ensah.pfe_matcher.model.PlanningRequest;
import ma.ensah.pfe_matcher.model.Professor;
import ma.ensah.pfe_matcher.model.Soutenance;
import ma.ensah.pfe_matcher.model.TimeSlot;
import ma.ensah.pfe_matcher.util.ColorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;

@Service
public class PlanningGenerationServiceImpl implements PlanningGenerationService {

// Removed ConfigService dependency

    @Value("${planning.morning.start}")
    private String morningStartProp;

    @Value("${planning.morning.end}")
    private String morningEndProp;

    @Value("${planning.afternoon.start}")
    private String afternoonStartProp;

    @Value("${planning.afternoon.end}")
    private String afternoonEndProp;

    @Value("${planning.duration.minutes}")
    private int defaultDurationMinutes;

    private static final DateTimeFormatter[] DATE_FORMATS = new DateTimeFormatter[] {
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
    };

    @Value("${planning.salles}")
    private String sallesProp;

    @Autowired
    private AssignmentDAO assignmentDAO;

    @Override
    public List<Soutenance> generatePlanning(PlanningRequest request) {
        List<Assignment> rawAssignments = assignmentDAO.getAll();
        if (rawAssignments.isEmpty()) {
            throw new IllegalArgumentException("Aucune tache disponible. Executez d'abord l'affectation.");
        }

        // --- MERGE BINOMES ---
        List<Assignment> assignments = new ArrayList<>();
        Set<String> processedStudentIds = new HashSet<>();

        List<List<String>> binomes = request.getBinomes() != null ? request.getBinomes() : new ArrayList<>();

        for (Assignment a : rawAssignments) {
            if (a.getStudent() == null || processedStudentIds.contains(a.getStudent().getId())) {
                continue;
            }

            // Check if this student is in a binome
            String binomePartnerId = null;
            for (List<String> b : binomes) {
                if (b.size() >= 2) {
                    if (b.get(0).equals(a.getStudent().getId()))
                        binomePartnerId = b.get(1);
                    else if (b.get(1).equals(a.getStudent().getId()))
                        binomePartnerId = b.get(0);
                }
            }

            if (binomePartnerId != null) {
                final String pId = binomePartnerId;
                Optional<Assignment> partnerAssign = rawAssignments.stream()
                        .filter(ra -> rawAssignments != null && ra.getStudent() != null
                                && ra.getStudent().getId().equals(pId))
                        .findFirst();

                if (partnerAssign.isPresent()) {
                    a.setStudent2(partnerAssign.get().getStudent());
                    if (!partnerAssign.get().getProfessor().getId().equals(a.getProfessor().getId())) {
                        a.setProfessor2(partnerAssign.get().getProfessor());
                    }
                    processedStudentIds.add(partnerAssign.get().getStudent().getId());
                }
            }

            assignments.add(a);
            processedStudentIds.add(a.getStudent().getId());
        }
        // --- END MERGE ---

        int duration = request.getDurationMinutes() > 0 ? request.getDurationMinutes() : defaultDurationMinutes;
        List<LocalDate> dates = resolveDates(request);
        List<String> salles = resolveSalles(request);
        List<TimeSlot> slots = buildSlots(dates, duration, request);
        List<Professor> professorPool = buildProfessorPool(assignments);

        if (professorPool.size() < 3) {
            throw new IllegalArgumentException("au moins trois professeurs pour constituer un jury.");
        }

        List<Soutenance> planned = new ArrayList<>();
        int counter = 1;

        Map<String, Deque<Assignment>> assignmentsByField = buildAssignmentsByField(assignments);
        List<String> schedulingOrder = buildSchedulingOrder(assignmentsByField);

        Map<LocalDate, List<TimeSlot>> slotsByDate = slots.stream()
                .collect(Collectors.groupingBy(
                        TimeSlot::getDate,
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<List<TimeSlot>> days = new ArrayList<>(slotsByDate.values());
        for (List<TimeSlot> daySlots : days) {
            daySlots.sort(Comparator.comparing(TimeSlot::getStartTime));
        }

        boolean placedInGlobalCycle;
        do {
            placedInGlobalCycle = false;
            for (List<TimeSlot> daySlots : days) {
                for (String field : schedulingOrder) {
                    Deque<Assignment> queue = assignmentsByField.get(field);
                    if (queue == null || queue.isEmpty()) {
                        continue;
                    }
                    Soutenance placed = tryPlaceFromFieldQueue(
                            queue,
                            counter,
                            professorPool,
                            daySlots,
                            salles,
                            planned);
                    if (placed != null) {
                        planned.add(placed);
                        counter++;
                        placedInGlobalCycle = true;
                    }
                }
            }
        } while (placedInGlobalCycle);

        if (hasRemainingAssignments(assignmentsByField)) {
            throw new IllegalArgumentException(
                    "Pas assez de capacite pour planifier toutes les soutenances avec une repartition quotidienne par filiere.");
        }

        planned.sort(Comparator.comparing(Soutenance::getDate)
                .thenComparing(Soutenance::getStartTime)
                .thenComparing(Soutenance::getSalle));
        return planned;
    }

    @Override
    public Map<String, Object> buildStats(List<Soutenance> soutenances, List<String> violations) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSoutenances", soutenances.size());
        stats.put("totalViolations", violations.size());

        stats.put("soutenancesPerDate", soutenances.stream()
                .collect(Collectors.groupingBy(s -> s.getDate().toString(), Collectors.counting())));

        stats.put("soutenancesPerRoom", soutenances.stream()
                .collect(Collectors.groupingBy(Soutenance::getSalle, Collectors.counting())));

        return stats;
    }

    private Soutenance placeOneSoutenance(int counter,
            Assignment assignment,
            Professor encadrant,
            List<Professor> pool,
            List<TimeSlot> slots,
            List<String> salles,
            List<Soutenance> planned) {
        for (TimeSlot slot : slots) {
            for (String room : salles) {
                if (!isRoomAvailable(room, slot, planned)) {
                    continue;
                }
                if (!isProfessorAvailable(encadrant, slot, planned)) {
                    continue;
                }
                if (assignment.getProfessor2() != null
                        && !isProfessorAvailable(assignment.getProfessor2(), slot, planned)) {
                    continue;
                }

                List<Professor> jury = findAvailableJury(encadrant, assignment.getProfessor2(), pool, slot, planned);
                if (jury == null) {
                    continue;
                }

                String generatedId = assignment.getStudent() != null
                        && assignment.getStudent().getId() != null
                        && !assignment.getStudent().getId().isBlank()
                                ? assignment.getStudent().getId().trim()
                                : String.valueOf(counter);

                return new Soutenance(
                        generatedId,
                        assignment,
                        encadrant,
                        jury.get(0),
                        jury.get(1),
                        slot.getDate(),
                        slot.getStartTime(),
                        slot.getEndTime(),
                        room);
            }
        }
        return null;
    }

    private List<Professor> findAvailableJury(Professor encadrant,
            Professor encadrant2,
            List<Professor> pool,
            TimeSlot slot,
            List<Soutenance> planned) {
        List<Professor> candidates = pool.stream()
                .filter(p -> !sameProfessor(p, encadrant))
                .filter(p -> encadrant2 == null || !sameProfessor(p, encadrant2))
                .collect(Collectors.toList());

        // Organize candidates by combining with a workload count, sorting them by
        // workload ascending
        candidates.sort(Comparator.comparingInt(p -> getProfessorSoutenanceCount(p, planned)));

        if (encadrant2 != null) {
            for (int i = 0; i < candidates.size(); i++) {
                Professor jury2 = candidates.get(i);
                if (isProfessorAvailable(jury2, slot, planned)) {
                    return List.of(encadrant2, jury2);
                }
            }
            return null;
        }

        for (int i = 0; i < candidates.size(); i++) {
            for (int j = i + 1; j < candidates.size(); j++) {
                Professor jury1 = candidates.get(i);
                Professor jury2 = candidates.get(j);
                if (sameProfessor(jury1, jury2)) {
                    continue;
                }
                if (isProfessorAvailable(jury1, slot, planned) && isProfessorAvailable(jury2, slot, planned)) {
                    return List.of(jury1, jury2);
                }
            }
        }
        return null;
    }

    private int getProfessorSoutenanceCount(Professor professor, List<Soutenance> planned) {
        int count = 0;
        for (Soutenance s : planned) {
            if (involvesProfessor(s, professor)) {
                count++;
            }
        }
        return count;
    }

    private boolean isRoomAvailable(String room, TimeSlot slot, List<Soutenance> planned) {
        for (Soutenance s : planned) {
            if (!s.getDate().equals(slot.getDate())) {
                continue;
            }
            if (!s.getSalle().equalsIgnoreCase(room)) {
                continue;
            }
            if (overlaps(slot, s)) {
                return false;
            }
        }
        return true;
    }

    private boolean isProfessorAvailable(Professor professor, TimeSlot slot, List<Soutenance> planned) {
        for (Soutenance s : planned) {
            if (!s.getDate().equals(slot.getDate())) {
                continue;
            }
            if (!involvesProfessor(s, professor)) {
                continue;
            }

            if (overlaps(slot, s)) {
                return false;
            }
        }
        return true;
    }

    private boolean overlaps(TimeSlot slot, Soutenance soutenance) {
        return slot.getStartTime().isBefore(soutenance.getEndTime())
                && soutenance.getStartTime().isBefore(slot.getEndTime());
    }

    private boolean involvesProfessor(Soutenance soutenance, Professor professor) {
        return sameProfessor(soutenance.getEncadrant(), professor)
                || sameProfessor(soutenance.getJury1(), professor)
                || sameProfessor(soutenance.getJury2(), professor);
    }

    private List<LocalDate> resolveDates(PlanningRequest request) {
        if (request.getDates() != null && !request.getDates().isEmpty()) {
            List<LocalDate> dates = request.getDates().stream()
                    .map(this::parseDate)
                    .filter(this::isPlanningDay)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            if (dates.isEmpty()) {
                throw new IllegalArgumentException("aucune date disponible apres exclusion du dimanche.");
            }
            return dates;
        }

        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("donner a startDate/endDate.");
        }

        LocalDate start = parseDate(request.getStartDate());
        LocalDate end = parseDate(request.getEndDate());
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("endDate doit etre posterieur ou egal a startDate.");
        }

        List<LocalDate> result = new ArrayList<>();
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            if (isPlanningDay(cursor)) {
                result.add(cursor);
            }
            cursor = cursor.plusDays(1);
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("aucune date disponible apres exclusion du dimanche.");
        }
        return result;
    }

    private boolean isPlanningDay(LocalDate date) {
        return date != null && date.getDayOfWeek() != DayOfWeek.SUNDAY;
    }

    private List<String> resolveSalles(PlanningRequest request) {
        if (request.getSalles() == null || request.getSalles().isEmpty()) {
            // Fall back to the comma-separated list from application.properties
            return Arrays.stream(sallesProp.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());
        }

        return request.getSalles().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    private List<TimeSlot> buildSlots(List<LocalDate> dates, int durationMinutes, PlanningRequest request) {
        List<TimeSlot> slots = new ArrayList<>();
        // Use request value if provided, otherwise fall back to application.properties
        LocalTime morningStart   = resolveTime(request.getMorningStart(),   morningStartProp);
        LocalTime morningEnd     = resolveTime(request.getMorningEnd(),     morningEndProp);
        LocalTime afternoonStart = resolveTime(request.getAfternoonStart(), afternoonStartProp);
        LocalTime afternoonEnd   = resolveTime(request.getAfternoonEnd(),   afternoonEndProp);

        for (LocalDate date : dates) {
            addWindowSlots(slots, date, morningStart, morningEnd, durationMinutes);
            addWindowSlots(slots, date, afternoonStart, afternoonEnd, durationMinutes);
        }
        return slots;
    }

    /** Returns the parsed request value when non-blank, otherwise parses the property fallback. */
    private LocalTime resolveTime(String requestValue, String propertyFallback) {
        if (requestValue != null && !requestValue.isBlank()) {
            return LocalTime.parse(requestValue.trim());
        }
        return LocalTime.parse(propertyFallback);
    }

    private void addWindowSlots(List<TimeSlot> slots,
            LocalDate date,
            LocalTime windowStart,
            LocalTime windowEnd,
            int durationMinutes) {
        LocalTime start = windowStart;
        while (!start.plusMinutes(durationMinutes).isAfter(windowEnd)) {
            slots.add(new TimeSlot(date, start, start.plusMinutes(durationMinutes)));
            start = start.plusMinutes(durationMinutes);
        }
    }

    private List<Professor> buildProfessorPool(List<Assignment> assignments) {
        Map<String, Professor> byKey = new LinkedHashMap<>();
        for (Assignment assignment : assignments) {
            Professor p = assignment.getProfessor();
            byKey.putIfAbsent(profKey(p), p);
        }
        return new ArrayList<>(byKey.values());
    }

    private Map<String, Deque<Assignment>> buildAssignmentsByField(List<Assignment> assignments) {
        Map<String, Deque<Assignment>> byField = new LinkedHashMap<>();
        for (Assignment assignment : assignments) {
            String field = extractField(assignment);
            byField.computeIfAbsent(field, key -> new ArrayDeque<>()).addLast(assignment);
        }
        return byField;
    }

    private List<String> buildSchedulingOrder(Map<String, Deque<Assignment>> byField) {
        List<String> order = new ArrayList<>();

        for (String field : byField.keySet()) {
            if (!order.contains(field)) {
                order.add(field);
            }
        }
        return order;
    }

    private Soutenance tryPlaceFromFieldQueue(Deque<Assignment> queue,
            int counter,
            List<Professor> professorPool,
            List<TimeSlot> daySlots,
            List<String> salles,
            List<Soutenance> planned) {
        int attempts = queue.size();
        while (attempts-- > 0) {
            Assignment assignment = queue.pollFirst();
            if (assignment == null) {
                break;
            }
            Soutenance placed = placeOneSoutenance(
                    counter,
                    assignment,
                    assignment.getProfessor(),
                    professorPool,
                    daySlots,
                    salles,
                    planned);
            if (placed != null) {
                return placed;
            }
            queue.addLast(assignment);
        }
        return null;
    }

    private boolean hasRemainingAssignments(Map<String, Deque<Assignment>> assignmentsByField) {
        for (Deque<Assignment> queue : assignmentsByField.values()) {
            if (queue != null && !queue.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private String extractField(Assignment assignment) {
        if (assignment != null
                && assignment.getStudent() != null
                && assignment.getStudent().getField() != null
                && !assignment.getStudent().getField().isBlank()) {
            return assignment.getStudent().getField().trim();
        }
        return "UNKNOWN";
    }

    private boolean sameProfessor(Professor a, Professor b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.getId() != null && b.getId() != null) {
            return a.getId().equalsIgnoreCase(b.getId());
        }
        return safe(a.getLastname()).equalsIgnoreCase(safe(b.getLastname()))
                && safe(a.getFirstname()).equalsIgnoreCase(safe(b.getFirstname()));
    }

    private String profKey(Professor p) {
        if (p.getId() != null && !p.getId().isBlank()) {
            return p.getId();
        }
        return safe(p.getLastname()) + "|" + safe(p.getFirstname());
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("valuer de date ne peut pas etre vide");
        }
        for (DateTimeFormatter formatter : DATE_FORMATS) {
            try {
                return LocalDate.parse(value.trim(), formatter);
            } catch (DateTimeParseException ignored) {
                // Try next format
            }
        }
        throw new IllegalArgumentException("Unsupported date format: " + value);
    }
}

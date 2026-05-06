package ma.ensah.pfe_matcher.service;

import ma.ensah.pfe_matcher.dao.AssignmentDAO;
import ma.ensah.pfe_matcher.model.Assignment;
import ma.ensah.pfe_matcher.model.PlanningRequest;
import ma.ensah.pfe_matcher.model.Professor;
import ma.ensah.pfe_matcher.model.Soutenance;
import ma.ensah.pfe_matcher.model.TimeSlot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlanningGenerationServiceImpl implements PlanningGenerationService {

    private static final DateTimeFormatter[] DATE_FORMATS = new DateTimeFormatter[]{
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
    };

    private static final LocalTime MORNING_START = LocalTime.of(9, 0);
    private static final LocalTime MORNING_END = LocalTime.of(12, 0);
    private static final LocalTime AFTERNOON_START = LocalTime.of(14, 0);
    private static final LocalTime AFTERNOON_END = LocalTime.of(18, 0);

    private static final List<String> DEFAULT_SALLES = List.of("S4A", "S5A", "S16A", "S17A", "AMPHI A");

    @Autowired
    private AssignmentDAO assignmentDAO;

    @Override
    public List<Soutenance> generatePlanning(PlanningRequest request) {
        List<Assignment> assignments = assignmentDAO.getAll();
        if (assignments.isEmpty()) {
            throw new IllegalArgumentException("Aucune tache disponible. Executez d'abord l'affectation.");
        }

        int duration = request.getDurationMinutes() > 0 ? request.getDurationMinutes() : 60;
        List<LocalDate> dates = resolveDates(request);
        List<String> salles = resolveSalles(request);
        List<TimeSlot> slots = buildSlots(dates, duration);
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
                        Collectors.toList()
                ));
        for (Map.Entry<LocalDate, List<TimeSlot>> entry : slotsByDate.entrySet()) {
            entry.getValue().sort(Comparator.comparing(TimeSlot::getStartTime));
            counter = scheduleDayRoundRobin(
                    entry.getValue(),
                    schedulingOrder,
                    assignmentsByField,
                    salles,
                    professorPool,
                    planned,
                    counter
            );
        }

        if (hasRemainingAssignments(assignmentsByField)) {
            throw new IllegalArgumentException(
                    "Pas assez de capacite pour planifier toutes les soutenances avec une repartition quotidienne par filiere."
            );
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

                List<Professor> jury = findAvailableJury(encadrant, pool, slot, planned);
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
                        room
                );
            }
        }
        return null;
    }

    private List<Professor> findAvailableJury(Professor encadrant,
                                              List<Professor> pool,
                                              TimeSlot slot,
                                              List<Soutenance> planned) {
        List<Professor> candidates = pool.stream()
                .filter(p -> !sameProfessor(p, encadrant))
                .collect(Collectors.toList());

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
            return request.getDates().stream()
                    .map(this::parseDate)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
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
            result.add(cursor);
            cursor = cursor.plusDays(1);
        }
        return result;
    }

    private List<String> resolveSalles(PlanningRequest request) {
        if (request.getSalles() == null || request.getSalles().isEmpty()) {
            return new ArrayList<>(DEFAULT_SALLES);
        }

        return request.getSalles().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    private List<TimeSlot> buildSlots(List<LocalDate> dates, int durationMinutes) {
        List<TimeSlot> slots = new ArrayList<>();
        for (LocalDate date : dates) {
            addWindowSlots(slots, date, MORNING_START, MORNING_END, durationMinutes);
            addWindowSlots(slots, date, AFTERNOON_START, AFTERNOON_END, durationMinutes);
        }
        return slots;
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
        for (String preferred : List.of("GI", "ID", "TDIA")) {
            if (byField.containsKey(preferred)) {
                order.add(preferred);
            }
        }
        for (String field : byField.keySet()) {
            if (!order.contains(field)) {
                order.add(field);
            }
        }
        return order;
    }

    private int scheduleDayRoundRobin(List<TimeSlot> daySlots,
                                      List<String> schedulingOrder,
                                      Map<String, Deque<Assignment>> assignmentsByField,
                                      List<String> salles,
                                      List<Professor> professorPool,
                                      List<Soutenance> planned,
                                      int counter) {
        boolean placedInCycle;
        do {
            placedInCycle = false;
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
                        planned
                );
                if (placed != null) {
                    planned.add(placed);
                    counter++;
                    placedInCycle = true;
                }
            }
        } while (placedInCycle);

        return counter;
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
                    planned
            );
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
            String raw = assignment.getStudent().getField().trim().toUpperCase();
            String normalized = raw.replaceAll("[^A-Z0-9]+", " ").trim();

            if (normalized.contains("TDIA")) {
                return "TDIA";
            }
            if (normalized.contains("GENIE INFORMATIQUE")) {
                return "GI";
            }
            if (normalized.contains("INGENIERIE DIGITALE") || normalized.contains("INFORMATIQUE DECISIONNELLE")) {
                return "ID";
            }

            List<String> tokens = Arrays.asList(normalized.split("\\s+"));
            if (tokens.contains("GI")) {
                return "GI";
            }
            if (tokens.contains("ID")) {
                return "ID";
            }
            return normalized;
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

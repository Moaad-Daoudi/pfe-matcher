package ma.ensah.pfe_matcher.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Soutenance {
    private String id;
    private Assignment assignment;
    private Professor encadrant;
    private Professor jury1;
    private Professor jury2;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String salle;

    public Soutenance() {
    }

    public Soutenance(String id, Assignment assignment, Professor encadrant, Professor jury1, Professor jury2, LocalDate date, LocalTime startTime, LocalTime endTime, String salle) {
        this.id = id;
        this.assignment = assignment;
        this.encadrant = encadrant;
        this.jury1 = jury1;
        this.jury2 = jury2;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.salle = salle;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public Professor getEncadrant() {
        return encadrant;
    }

    public void setEncadrant(Professor encadrant) {
        this.encadrant = encadrant;
    }

    public Professor getJury1() {
        return jury1;
    }

    public void setJury1(Professor jury1) {
        this.jury1 = jury1;
    }

    public Professor getJury2() {
        return jury2;
    }

    public void setJury2(Professor jury2) {
        this.jury2 = jury2;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getSalle() {
        return salle;
    }

    public void setSalle(String salle) {
        this.salle = salle;
    }
}

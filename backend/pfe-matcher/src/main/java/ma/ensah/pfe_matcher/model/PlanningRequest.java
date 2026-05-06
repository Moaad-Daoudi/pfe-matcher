package ma.ensah.pfe_matcher.model;

import java.util.List;

public class PlanningRequest {
    private String startDate;
    private String endDate;
    private List<String> dates;
    private List<String> salles;
    private int durationMinutes = 60;

    public PlanningRequest() {
    }

    public PlanningRequest(String startDate, String endDate, List<String> dates, List<String> salles, int durationMinutes) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.dates = dates;
        this.salles = salles;
        this.durationMinutes = durationMinutes;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public List<String> getDates() {
        return dates;
    }

    public void setDates(List<String> dates) {
        this.dates = dates;
    }

    public List<String> getSalles() {
        return salles;
    }

    public void setSalles(List<String> salles) {
        this.salles = salles;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}

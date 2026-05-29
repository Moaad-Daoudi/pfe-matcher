package ma.ensah.pfe_matcher.model;

import java.util.List;

public class PlanningRequest {
    private String startDate;
    private String endDate;
    private List<String> dates;
    private List<String> salles;
    private int durationMinutes = 0;  // 0 means "use default from properties"
    private List<List<String>> binomes;

    // Optional time overrides — null/blank means "use application.properties default"
    private String morningStart;
    private String morningEnd;
    private String afternoonStart;
    private String afternoonEnd;

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

    public List<List<String>> getBinomes() {
        return binomes;
    }

    public void setBinomes(List<List<String>> binomes) {
        this.binomes = binomes;
    }

    public String getMorningStart() {
        return morningStart;
    }

    public void setMorningStart(String morningStart) {
        this.morningStart = morningStart;
    }

    public String getMorningEnd() {
        return morningEnd;
    }

    public void setMorningEnd(String morningEnd) {
        this.morningEnd = morningEnd;
    }

    public String getAfternoonStart() {
        return afternoonStart;
    }

    public void setAfternoonStart(String afternoonStart) {
        this.afternoonStart = afternoonStart;
    }

    public String getAfternoonEnd() {
        return afternoonEnd;
    }

    public void setAfternoonEnd(String afternoonEnd) {
        this.afternoonEnd = afternoonEnd;
    }
}

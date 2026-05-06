package ma.ensah.pfe_matcher.model;

public class Professor {
    private String id;
    private String lastname;
    private String firstname;
    private String department;
    private int maxCapacity;
    private int currentLoad = 0;

    public Professor() {
    }

    public Professor(String id, String lastname, String firstname, String department, int maxCapacity, int currentLoad) {
        this.id = id;
        this.lastname = lastname;
        this.firstname = firstname;
        this.department = department;
        this.maxCapacity = maxCapacity;
        this.currentLoad = currentLoad;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public int getCurrentLoad() {
        return currentLoad;
    }

    public void setCurrentLoad(int currentLoad) {
        this.currentLoad = currentLoad;
    }
}

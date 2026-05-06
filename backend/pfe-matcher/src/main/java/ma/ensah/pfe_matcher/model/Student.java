package ma.ensah.pfe_matcher.model;

public class Student {
    private String id;
    private String cne;
    private String lastname;
    private String firstname;
    private String field;

    public Student() {
    }

    public Student(String id, String cne, String lastname, String firstname, String field) {
        this.id = id;
        this.cne = cne;
        this.lastname = lastname;
        this.firstname = firstname;
        this.field = field;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCne() {
        return cne;
    }

    public void setCne(String cne) {
        this.cne = cne;
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

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}

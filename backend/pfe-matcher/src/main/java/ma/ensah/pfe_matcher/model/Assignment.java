package ma.ensah.pfe_matcher.model;

public class Assignment {
    private String id;
    private Student student;
    private Professor professor;

    public Assignment() {
    }

    public Assignment(String id, Student student, Professor professor) {
        this.id = id;
        this.student = student;
        this.professor = professor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Professor getProfessor() {
        return professor;
    }

    public void setProfessor(Professor professor) {
        this.professor = professor;
    }
}

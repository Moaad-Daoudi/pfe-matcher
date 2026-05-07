package ma.ensah.pfe_matcher.model;

public class PV {
    private long id;
    private String nom;
    private String prenom;
    private String filiere;
    private String encadrant;
    private String jury1;
    private String jury2;
    private String date;

    public PV() {
        super();
    }

    public PV(String nom, String prenom, String filiere, String encadrant, String jury1, String jury2, String date) {
        super();
        this.nom = nom;
        this.prenom = prenom;
        this.filiere = filiere;
        this.encadrant = encadrant;
        this.jury1 = jury1;
        this.jury2 = jury2;
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getFiliere() {
        return filiere;
    }

    public void setFiliere(String filiere) {
        this.filiere = filiere;
    }

    public String getEncadrant() {
        return encadrant;
    }

    public void setEncadrant(String encadrant) {
        this.encadrant = encadrant;
    }

    public String getJury1() {
        return jury1;
    }

    public void setJury1(String jury1) {
        this.jury1 = jury1;
    }

    public String getJury2() {
        return jury2;
    }

    public void setJury2(String jury2) {
        this.jury2 = jury2;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
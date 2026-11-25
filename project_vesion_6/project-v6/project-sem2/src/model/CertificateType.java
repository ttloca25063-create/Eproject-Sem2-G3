package model;

import java.util.ArrayList;
import java.util.List;

public class CertificateType {
    private int certificateTypeID;
    private String code;
    private String name;
    private String level;
    private String description;
    private double passScore = 5.0;

    private List<Course> courses = new ArrayList<>();
    private List<Exam> exams = new ArrayList<>();

    public CertificateType() {}

    public CertificateType(int certificateTypeID, String code, String name, String level, String description) {
        this(certificateTypeID, code, name, level, description, 5.0);
    }

    public CertificateType(int certificateTypeID, String code, String name, String level, String description, double passScore) {
        this.certificateTypeID = certificateTypeID;
        this.code = code;
        this.name = name;
        this.level = level;
        this.description = description;
        this.passScore = passScore;
    }

    public int getCertificateTypeID() { return certificateTypeID; }
    public void setCertificateTypeID(int certificateTypeID) { this.certificateTypeID = certificateTypeID; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPassScore() { return passScore; }
    public void setPassScore(double passScore) { this.passScore = passScore; }

    // Methods as per Class Diagram
    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public List<Exam> getExams() {
        return exams;
    }

    public void setExams(List<Exam> exams) {
        this.exams = exams;
    }

    @Override
    public String toString() {
        return code != null ? code + " - " + name : name;
    }
}

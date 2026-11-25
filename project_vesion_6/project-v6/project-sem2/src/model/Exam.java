package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Exam {
    private int examID;
    private String examCode;
    private String examName;
    private int certificateTypeID;
    private String certificateTypeName;
    private LocalDate examDate;
    private LocalDate registrationDeadline;
    private String location;
    private double examFee;
    private String status;

    private List<ExamRegistration> registrations = new ArrayList<>();

    public Exam() {}

    public Exam(int examID, String examName, double examFee) {
        this.examID = examID;
        this.examName = examName;
        this.examFee = examFee;
    }

    public int getExamID() { return examID; }
    public void setExamID(int examID) { this.examID = examID; }
    public String getExamCode() { return examCode; }
    public void setExamCode(String examCode) { this.examCode = examCode; }
    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }
    public int getCertificateTypeID() { return certificateTypeID; }
    public void setCertificateTypeID(int certificateTypeID) { this.certificateTypeID = certificateTypeID; }
    public String getCertificateTypeName() { return certificateTypeName; }
    public void setCertificateTypeName(String certificateTypeName) { this.certificateTypeName = certificateTypeName; }
    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }
    public LocalDate getRegistrationDeadline() { return registrationDeadline; }
    public void setRegistrationDeadline(LocalDate registrationDeadline) { this.registrationDeadline = registrationDeadline; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public double getExamFee() { return examFee; }
    public void setExamFee(double examFee) { this.examFee = examFee; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Methods as per Class Diagram
    public List<ExamRegistration> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(List<ExamRegistration> registrations) {
        this.registrations = registrations;
    }

    @Override
    public String toString() {
        return examName + " (Fee: " + String.format("%,.0f", examFee) + " USD)";
    }
}

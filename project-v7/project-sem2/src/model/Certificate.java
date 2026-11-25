package model;

import java.time.LocalDate;

public class Certificate {
    private int certificateID;
    private String certificateNumber;
    private int certificateTypeID;
    private Integer studentID;
    private Integer examResultID;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String status;
    private Integer issuedBy;

    public Certificate() {}

    public Certificate(int certificateID, String certificateNumber, int certificateTypeID, Integer studentID,
                       Integer examResultID, LocalDate issueDate, LocalDate expiryDate, String status, Integer issuedBy) {
        this.certificateID = certificateID;
        this.certificateNumber = certificateNumber;
        this.certificateTypeID = certificateTypeID;
        this.studentID = studentID;
        this.examResultID = examResultID;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.status = status;
        this.issuedBy = issuedBy;
    }

    // Getters and Setters
    public int getCertificateID() { return certificateID; }
    public void setCertificateID(int certificateID) { this.certificateID = certificateID; }
    public String getCertificateNumber() { return certificateNumber; }
    public void setCertificateNumber(String certificateNumber) { this.certificateNumber = certificateNumber; }
    public int getCertificateTypeID() { return certificateTypeID; }
    public void setCertificateTypeID(int certificateTypeID) { this.certificateTypeID = certificateTypeID; }
    public Integer getStudentID() { return studentID; }
    public void setStudentID(Integer studentID) { this.studentID = studentID; }
    public Integer getExamResultID() { return examResultID; }
    public void setExamResultID(Integer examResultID) { this.examResultID = examResultID; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getIssuedBy() { return issuedBy; }
    public void setIssuedBy(Integer issuedBy) { this.issuedBy = issuedBy; }

    // Methods as per Class Diagram
    public boolean issue() {
        this.status = "Valid";
        this.issueDate = LocalDate.now();
        return true;
    }

    public boolean revoke() {
        this.status = "Revoked";
        return true;
    }

    public boolean isEligible() {
        return "Valid".equalsIgnoreCase(this.status);
    }
}

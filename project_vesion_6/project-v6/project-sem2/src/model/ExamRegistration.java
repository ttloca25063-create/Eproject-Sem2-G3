package model;

import java.time.LocalDate;

public class ExamRegistration {
    private int registrationID;
    private int examID;
    private Integer studentID;
    private String candidateType;   // "Internal" | "External"
    private String fullName;
    private LocalDate dob;
    private String idNumber;
    private String phone;
    private String email;
    private boolean feeExempt;
    private double examFee;
    private String paidStatus;      // "Unpaid" | "Paid" | "Exempted"
    private LocalDate registrationDate;
    private String status;

    public ExamRegistration() {}

    public int getRegistrationID() { return registrationID; }
    public void setRegistrationID(int registrationID) { this.registrationID = registrationID; }
    public int getExamID() { return examID; }
    public void setExamID(int examID) { this.examID = examID; }
    public Integer getStudentID() { return studentID; }
    public void setStudentID(Integer studentID) { this.studentID = studentID; }
    public String getCandidateType() { return candidateType; }
    public void setCandidateType(String candidateType) { this.candidateType = candidateType; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }
    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean isFeeExempt() { return feeExempt; }
    public void setFeeExempt(boolean feeExempt) { this.feeExempt = feeExempt; }
    public double getExamFee() { return examFee; }
    public void setExamFee(double examFee) { this.examFee = examFee; }
    public String getPaidStatus() { return paidStatus; }
    public void setPaidStatus(String paidStatus) { this.paidStatus = paidStatus; }
    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate registrationDate) { this.registrationDate = registrationDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Methods as per Class Diagram
    public double calculateFee() {
        if (isInternal() || feeExempt) {
            return 0.0;
        }
        return this.examFee;
    }

    public boolean isInternal() {
        return "Internal".equalsIgnoreCase(this.candidateType) || this.studentID != null;
    }
}

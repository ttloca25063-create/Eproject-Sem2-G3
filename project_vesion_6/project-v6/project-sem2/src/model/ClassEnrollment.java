package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ClassEnrollment {
    private int enrollmentID;
    private int studentID;
    private int classID;
    private LocalDate enrollmentDate;
    private BigDecimal tuitionFee;
    private BigDecimal discount;
    private String paymentStatus;
    private String status;
    private List<Payment> payments = new ArrayList<>();

    public ClassEnrollment() {}

    public ClassEnrollment(int enrollmentID, int studentID, int classID, LocalDate enrollmentDate,
                           BigDecimal tuitionFee, BigDecimal discount, String paymentStatus, String status) {
        this.enrollmentID = enrollmentID;
        this.studentID = studentID;
        this.classID = classID;
        this.enrollmentDate = enrollmentDate;
        this.tuitionFee = tuitionFee;
        this.discount = discount;
        this.paymentStatus = paymentStatus;
        this.status = status;
    }

    public int getEnrollmentID() { return enrollmentID; }
    public void setEnrollmentID(int enrollmentID) { this.enrollmentID = enrollmentID; }
    public int getStudentID() { return studentID; }
    public void setStudentID(int studentID) { this.studentID = studentID; }
    public int getClassID() { return classID; }
    public void setClassID(int classID) { this.classID = classID; }
    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }
    public BigDecimal getTuitionFee() { return tuitionFee; }
    public void setTuitionFee(BigDecimal tuitionFee) { this.tuitionFee = tuitionFee; }
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Methods as per Class Diagram
    public BigDecimal getPaidAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (Payment p : payments) {
            if (p.getAmount() != null) {
                total = total.add(p.getAmount());
            }
        }
        return total;
    }

    public void addPayment(BigDecimal amount) {
        Payment p = new Payment();
        p.setEnrollmentID(this.enrollmentID);
        p.setAmount(amount);
        p.setPaymentDate(LocalDate.now());
        p.setMethod("Cash");
        payments.add(p);
    }

    public List<Payment> getPayments() { return payments; }
    public void setPayments(List<Payment> payments) { this.payments = payments; }
}

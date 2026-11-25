package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Payment {
    private int paymentID;
    private int enrollmentID;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String method;
    private String note;

    public Payment() {}

    public Payment(int paymentID, int enrollmentID, BigDecimal amount, LocalDate paymentDate, String method, String note) {
        this.paymentID = paymentID;
        this.enrollmentID = enrollmentID;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.method = method;
        this.note = note;
    }

    public int getPaymentID() { return paymentID; }
    public void setPaymentID(int paymentID) { this.paymentID = paymentID; }
    public int getEnrollmentID() { return enrollmentID; }
    public void setEnrollmentID(int enrollmentID) { this.enrollmentID = enrollmentID; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    // Methods as per Class Diagram
    public boolean processPayment() {
        return this.amount != null && this.amount.compareTo(BigDecimal.ZERO) > 0;
    }
}

package model;

import java.math.BigDecimal;

public class TestScore {
    private int testID;
    private int studentID;
    private BigDecimal score;
    private String note;

    public TestScore() {}

    public TestScore(int testID, int studentID, BigDecimal score, String note) {
        this.testID = testID;
        this.studentID = studentID;
        this.score = score;
        this.note = note;
    }

    public int getTestID() { return testID; }
    public void setTestID(int testID) { this.testID = testID; }
    public int getStudentID() { return studentID; }
    public void setStudentID(int studentID) { this.studentID = studentID; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}

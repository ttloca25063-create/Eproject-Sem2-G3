package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PeriodicTest {
    private int testID;
    private int classID;
    private String testName;
    private LocalDate testDate;
    private int skillTypeID;
    private BigDecimal maxScore;
    private List<TestScore> scores = new ArrayList<>();

    public PeriodicTest() {}

    public PeriodicTest(int testID, int classID, String testName, LocalDate testDate, int skillTypeID, BigDecimal maxScore) {
        this.testID = testID;
        this.classID = classID;
        this.testName = testName;
        this.testDate = testDate;
        this.skillTypeID = skillTypeID;
        this.maxScore = maxScore;
    }

    public int getTestID() { return testID; }
    public void setTestID(int testID) { this.testID = testID; }
    public int getClassID() { return classID; }
    public void setClassID(int classID) { this.classID = classID; }
    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }
    public LocalDate getTestDate() { return testDate; }
    public void setTestDate(LocalDate testDate) { this.testDate = testDate; }
    public int getSkillTypeID() { return skillTypeID; }
    public void setSkillTypeID(int skillTypeID) { this.skillTypeID = skillTypeID; }
    public BigDecimal getMaxScore() { return maxScore; }
    public void setMaxScore(BigDecimal maxScore) { this.maxScore = maxScore; }

    // Methods as per Class Diagram
    public List<TestScore> getScores() {
        return scores;
    }

    public void setScores(List<TestScore> scores) {
        this.scores = scores;
    }
}

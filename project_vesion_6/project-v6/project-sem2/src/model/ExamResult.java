package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExamResult {
    private int resultID;
    private int registrationID;
    private String candidateName;
    private String candidateType;
    private double averageScore;
    private double totalScore;
    private String result;          // "Pass" | "Fail" | "Pending"
    private String resultStatus;    // Compatibility with existing UI
    private LocalDate examDate;
    private LocalDate gradedDate;
    private Integer gradedBy;
    private String note;

    private List<ExamResultDetail> details = new ArrayList<>();

    public ExamResult() {}

    public int getResultID() { return resultID; }
    public void setResultID(int resultID) { this.resultID = resultID; }
    public int getRegistrationID() { return registrationID; }
    public void setRegistrationID(int registrationID) { this.registrationID = registrationID; }
    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }
    public String getCandidateType() { return candidateType; }
    public void setCandidateType(String candidateType) { this.candidateType = candidateType; }
    public double getAverageScore() { return averageScore > 0 ? averageScore : totalScore; }
    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
        this.totalScore = averageScore;
    }
    public double getTotalScore() { return totalScore > 0 ? totalScore : averageScore; }
    public void setTotalScore(double totalScore) {
        this.totalScore = totalScore;
        this.averageScore = totalScore;
    }
    public String getResult() { return result != null ? result : resultStatus; }
    public void setResult(String result) {
        this.result = result;
        this.resultStatus = result;
    }
    public String getResultStatus() { return resultStatus != null ? resultStatus : result; }
    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
        this.result = resultStatus;
    }
    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) {
        this.examDate = examDate;
    }
    public LocalDate getGradedDate() { return gradedDate; }
    public void setGradedDate(LocalDate gradedDate) {
        this.gradedDate = gradedDate;
    }
    public Integer getGradedBy() { return gradedBy; }
    public void setGradedBy(Integer gradedBy) { this.gradedBy = gradedBy; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    // Methods as per Class Diagram
    public List<ExamResultDetail> getDetails() {
        return details;
    }

    public void setDetails(List<ExamResultDetail> details) {
        this.details = details;
    }

    public double calculateAverage() {
        if (details == null || details.isEmpty()) return this.averageScore;
        double sum = 0;
        for (ExamResultDetail d : details) {
            sum += d.getScore();
        }
        this.averageScore = Math.round((sum / details.size()) * 100.0) / 100.0;
        this.totalScore = this.averageScore;
        return this.averageScore;
    }

    public boolean isPassed() {
        return "Pass".equalsIgnoreCase(getResult());
    }
}

package model;

import java.time.LocalDateTime;

public class Report {
    private int reportID;
    private String reportType;
    private String reportMonth;
    private int createdBy;
    private LocalDateTime createdAt;
    private String filePath;

    public Report() {}

    public Report(int reportID, String reportType, String reportMonth, int createdBy, LocalDateTime createdAt, String filePath) {
        this.reportID = reportID;
        this.reportType = reportType;
        this.reportMonth = reportMonth;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.filePath = filePath;
    }

    public int getReportID() { return reportID; }
    public void setReportID(int reportID) { this.reportID = reportID; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public String getReportMonth() { return reportMonth; }
    public void setReportMonth(String reportMonth) { this.reportMonth = reportMonth; }
    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    // Methods as per Class Diagram
    public boolean generate() {
        this.createdAt = LocalDateTime.now();
        return true;
    }

    public boolean export() {
        return this.filePath != null && !this.filePath.isEmpty();
    }
}

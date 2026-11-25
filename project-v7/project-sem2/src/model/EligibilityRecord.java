package model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class EligibilityRecord {
    private final StringProperty fullName;
    private final StringProperty studentCode;
    private final StringProperty examName;
    private final StringProperty averageScore;
    private final StringProperty result;
    private final StringProperty certNumber;

    public EligibilityRecord(String fullName, String studentCode, String examName, String averageScore, String result, String certNumber) {
        this.fullName = new SimpleStringProperty(fullName);
        this.studentCode = new SimpleStringProperty(studentCode != null ? studentCode : "External");
        this.examName = new SimpleStringProperty(examName);
        this.averageScore = new SimpleStringProperty(averageScore != null ? averageScore : "Pending");
        this.result = new SimpleStringProperty(result != null ? result : "Pending");
        this.certNumber = new SimpleStringProperty(certNumber != null ? certNumber : "Not Issued");
    }

    public StringProperty fullNameProperty() { return fullName; }
    public StringProperty studentCodeProperty() { return studentCode; }
    public StringProperty examNameProperty() { return examName; }
    public StringProperty averageScoreProperty() { return averageScore; }
    public StringProperty resultProperty() { return result; }
    public StringProperty certNumberProperty() { return certNumber; }
}
package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Course {
    private int courseID;
    private String courseName;
    private int certificateTypeID;
    private int duration;
    private BigDecimal tuitionFee;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private List<ClassRoom> classes = new ArrayList<>();

    public Course() {}

    public Course(int courseID, String courseName, int certificateTypeID, int duration,
                  BigDecimal tuitionFee, LocalDate startDate, LocalDate endDate, String status) {
        this.courseID = courseID;
        this.courseName = courseName;
        this.certificateTypeID = certificateTypeID;
        this.duration = duration;
        this.tuitionFee = tuitionFee;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public int getCourseID() { return courseID; }
    public void setCourseID(int courseID) { this.courseID = courseID; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public int getCertificateTypeID() { return certificateTypeID; }
    public void setCertificateTypeID(int certificateTypeID) { this.certificateTypeID = certificateTypeID; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    public BigDecimal getTuitionFee() { return tuitionFee; }
    public void setTuitionFee(BigDecimal tuitionFee) { this.tuitionFee = tuitionFee; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Methods as per Class Diagram
    public List<ClassRoom> getClasses() {
        return classes;
    }

    public void setClasses(List<ClassRoom> classes) {
        this.classes = classes;
    }
}

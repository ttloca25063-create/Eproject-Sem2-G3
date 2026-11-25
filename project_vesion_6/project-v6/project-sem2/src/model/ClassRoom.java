package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ClassRoom {
    private int classID;
    private String classCode;
    private String className;
    private int courseID;
    private Integer teacherID;
    private LocalDate startDate;
    private LocalDate endDate;
    private int maxStudents;
    private String room;
    private String schedule;
    private String status;

    private List<ClassEnrollment> enrollments = new ArrayList<>();
    private List<TeachingSchedule> schedules = new ArrayList<>();

    public ClassRoom() {}

    public ClassRoom(int classID, String classCode, String className, int courseID, Integer teacherID,
                     LocalDate startDate, LocalDate endDate, int maxStudents, String room, String schedule, String status) {
        this.classID = classID;
        this.classCode = classCode;
        this.className = className;
        this.courseID = courseID;
        this.teacherID = teacherID;
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxStudents = maxStudents;
        this.room = room;
        this.schedule = schedule;
        this.status = status;
    }

    // Getters & Setters
    public int getClassID() { return classID; }
    public void setClassID(int classID) { this.classID = classID; }
    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public int getCourseID() { return courseID; }
    public void setCourseID(int courseID) { this.courseID = courseID; }
    public Integer getTeacherID() { return teacherID; }
    public void setTeacherID(Integer teacherID) { this.teacherID = teacherID; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public int getMaxStudents() { return maxStudents; }
    public void setMaxStudents(int maxStudents) { this.maxStudents = maxStudents; }
    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Methods as per Class Diagram
    public int getStudentCount() {
        return enrollments != null ? enrollments.size() : 0;
    }

    public List<ClassEnrollment> getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(List<ClassEnrollment> enrollments) {
        this.enrollments = enrollments;
    }

    public List<TeachingSchedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<TeachingSchedule> schedules) {
        this.schedules = schedules;
    }
}

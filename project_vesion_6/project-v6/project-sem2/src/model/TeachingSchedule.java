package model;

import java.time.LocalDate;
import java.time.LocalTime;

public class TeachingSchedule {
    private int scheduleID;
    private int classID;
    private String className;
    private int teacherID;
    private String teacherName;
    private int skillTypeID;
    private String skillName;
    private LocalDate teachingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String room;
    private String status;

    public TeachingSchedule() {}

    public int getScheduleID() { return scheduleID; }
    public void setScheduleID(int scheduleID) { this.scheduleID = scheduleID; }
    public int getClassID() { return classID; }
    public void setClassID(int classID) { this.classID = classID; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public int getTeacherID() { return teacherID; }
    public void setTeacherID(int teacherID) { this.teacherID = teacherID; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public int getSkillTypeID() { return skillTypeID; }
    public void setSkillTypeID(int skillTypeID) { this.skillTypeID = skillTypeID; }
    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }
    public LocalDate getTeachingDate() { return teachingDate; }
    public void setTeachingDate(LocalDate teachingDate) { this.teachingDate = teachingDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Methods as per Class Diagram
    public boolean assignTeacher(int teacherID, int skillTypeID) {
        this.teacherID = teacherID;
        this.skillTypeID = skillTypeID;
        this.status = "Scheduled";
        return true;
    }
}

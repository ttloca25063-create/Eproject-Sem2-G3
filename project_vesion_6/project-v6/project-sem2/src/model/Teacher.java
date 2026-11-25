package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Teacher {
    private int teacherID;
    private int userID;
    private String teacherCode;
    private String fullName;
    private String email;
    private String phone;
    private String degree;
    private String experience;
    private LocalDate hireDate;
    private String skillsDisplay;
    private String status;

    private List<TeacherSkill> skills = new ArrayList<>();
    private List<TeachingSchedule> schedules = new ArrayList<>();

    public Teacher() {}

    public Teacher(int teacherID, int userID, String teacherCode, String fullName, String email,
                   String phone, String degree, String experience, LocalDate hireDate, String skillsDisplay, String status) {
        this.teacherID = teacherID;
        this.userID = userID;
        this.teacherCode = teacherCode;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.degree = degree;
        this.experience = experience;
        this.hireDate = hireDate;
        this.skillsDisplay = skillsDisplay;
        this.status = status;
    }

    public int getTeacherID() { return teacherID; }
    public void setTeacherID(int teacherID) { this.teacherID = teacherID; }
    public int getUserID() { return userID; }
    public void setUserID(int userID) { this.userID = userID; }
    public String getTeacherCode() { return teacherCode; }
    public void setTeacherCode(String teacherCode) { this.teacherCode = teacherCode; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }
    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    public String getSkillsDisplay() { return skillsDisplay; }
    public void setSkillsDisplay(String skillsDisplay) { this.skillsDisplay = skillsDisplay; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Methods as per Class Diagram
    public List<TeacherSkill> getSkills() {
        return skills;
    }

    public void setSkills(List<TeacherSkill> skills) {
        this.skills = skills;
    }

    public List<TeachingSchedule> getSchedule() {
        return schedules;
    }

    public void setSchedule(List<TeachingSchedule> schedules) {
        this.schedules = schedules;
    }
}

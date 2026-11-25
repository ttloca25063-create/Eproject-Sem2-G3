package model;

import java.util.ArrayList;
import java.util.List;

public class SkillType {
    private int skillTypeID;
    private String code;
    private String name;
    private List<Teacher> teachers = new ArrayList<>();

    public SkillType() {}

    public SkillType(int skillTypeID, String code, String name) {
        this.skillTypeID = skillTypeID;
        this.code = code;
        this.name = name;
    }

    public int getSkillTypeID() { return skillTypeID; }
    public void setSkillTypeID(int skillTypeID) { this.skillTypeID = skillTypeID; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // Methods as per Class Diagram
    public List<Teacher> getTeachers() {
        return teachers;
    }

    public void setTeachers(List<Teacher> teachers) {
        this.teachers = teachers;
    }
}

package model;

public class TeacherSkill {
    private int teacherID;
    private int skillTypeID;
    private String proficiencyLevel;

    public TeacherSkill() {}

    public TeacherSkill(int teacherID, int skillTypeID, String proficiencyLevel) {
        this.teacherID = teacherID;
        this.skillTypeID = skillTypeID;
        this.proficiencyLevel = proficiencyLevel;
    }

    public int getTeacherID() { return teacherID; }
    public void setTeacherID(int teacherID) { this.teacherID = teacherID; }
    public int getSkillTypeID() { return skillTypeID; }
    public void setSkillTypeID(int skillTypeID) { this.skillTypeID = skillTypeID; }
    public String getProficiencyLevel() { return proficiencyLevel; }
    public void setProficiencyLevel(String proficiencyLevel) { this.proficiencyLevel = proficiencyLevel; }
}

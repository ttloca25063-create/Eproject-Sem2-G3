package model;

public class ExamResultDetail {
    private int detailID;
    private int resultID;
    private int skillID;
    private String skillName;   // Dùng hiển thị
    private double score;

    public int getDetailID() { return detailID; }
    public void setDetailID(int detailID) { this.detailID = detailID; }
    public int getResultID() { return resultID; }
    public void setResultID(int resultID) { this.resultID = resultID; }
    public int getSkillID() { return skillID; }
    public void setSkillID(int skillID) { this.skillID = skillID; }
    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
}
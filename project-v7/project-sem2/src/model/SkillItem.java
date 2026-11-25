package model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SkillItem {
    private final int skillTypeID;
    private final StringProperty code;
    private final StringProperty name;
    private final StringProperty minPassScore;

    public SkillItem(int skillTypeID, String code, String name, String minPassScore) {
        this.skillTypeID = skillTypeID;
        this.code = new SimpleStringProperty(code);
        this.name = new SimpleStringProperty(name);
        this.minPassScore = new SimpleStringProperty(minPassScore);
    }

    public int getSkillTypeID() { return skillTypeID; }
    public StringProperty codeProperty() { return code; }
    public StringProperty nameProperty() { return name; }
    public StringProperty minPassScoreProperty() { return minPassScore; }
}
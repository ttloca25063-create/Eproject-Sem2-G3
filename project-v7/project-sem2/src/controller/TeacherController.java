package controller;

import dao.TeacherDAO;
import model.Teacher;
import util.PasswordUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TeacherController {
    @FXML private TextField txtTeacherCode, txtFullName, txtEmail, txtPhone, txtDegree, txtExperience;
    @FXML private CheckBox chkListening, chkSpeaking, chkReading, chkWriting;
    @FXML private ComboBox<String> cboProficiencyLevel, cboStatus;
    @FXML private TableView<Teacher> tblTeacherList;
    @FXML private TableColumn<Teacher, String> colCode, colFullName, colSkills, colStatus;

    private final TeacherDAO teacherDAO = new TeacherDAO();
    private Teacher selectedTeacher = null;

    @FXML
    public void initialize() {
        if (cboProficiencyLevel != null) {
            cboProficiencyLevel.setItems(FXCollections.observableArrayList("B2", "C1", "C2", "IELTS 7.5+", "IELTS 8.0+"));
            cboProficiencyLevel.getSelectionModel().select(0);
        }
        if (cboStatus != null) {
            cboStatus.setItems(FXCollections.observableArrayList("Active", "Inactive"));
            cboStatus.getSelectionModel().select("Active");
        }

        colCode.setCellValueFactory(new PropertyValueFactory<>("teacherCode"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colSkills.setCellValueFactory(new PropertyValueFactory<>("skillsDisplay"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        tblTeacherList.getSelectionModel().selectedItemProperty().addListener((obs, o, sel) -> {
            selectedTeacher = sel;
            if (sel != null) {
                txtTeacherCode.setText(sel.getTeacherCode());
                txtTeacherCode.setDisable(true);
                txtFullName.setText(sel.getFullName());
                txtEmail.setText(sel.getEmail() != null ? sel.getEmail() : "");
                txtPhone.setText(sel.getPhone() != null ? sel.getPhone() : "");
                txtDegree.setText(sel.getDegree() != null ? sel.getDegree() : "");
                txtExperience.setText(sel.getExperience() != null ? sel.getExperience() : "");
                if (cboStatus != null) cboStatus.setValue(sel.getStatus());

                Set<Integer> skills = teacherDAO.getTeacherSkillIDs(sel.getTeacherID());
                chkListening.setSelected(skills.contains(1));
                chkSpeaking.setSelected(skills.contains(2));
                chkReading.setSelected(skills.contains(3));
                chkWriting.setSelected(skills.contains(4));
            }
        });

        tblTeacherList.setItems(FXCollections.observableArrayList(teacherDAO.getAllTeachers()));
    }

    private List<Integer> getSelectedSkillIDs() {
        List<Integer> selectedSkillIDs = new ArrayList<>();
        if (chkListening.isSelected()) selectedSkillIDs.add(1);
        if (chkSpeaking.isSelected())  selectedSkillIDs.add(2);
        if (chkReading.isSelected())   selectedSkillIDs.add(3);
        if (chkWriting.isSelected())   selectedSkillIDs.add(4);
        return selectedSkillIDs;
    }

    @FXML
    private void handleSaveTeacher() {
        List<Integer> selectedSkillIDs = getSelectedSkillIDs();

        if (selectedSkillIDs.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please select at least one skill (Listening, Speaking, Reading, Writing)!").showAndWait();
            return;
        }

        String teacherCode = txtTeacherCode.getText().trim();
        String fullName = txtFullName.getText().trim();
        if (teacherCode.isEmpty() || fullName.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Teacher Code and Full Name are required!").showAndWait();
            return;
        }

        Teacher t = new Teacher();
        t.setTeacherCode(teacherCode);
        t.setFullName(fullName);
        t.setEmail(PasswordUtil.emptyToNull(txtEmail.getText()));
        t.setPhone(PasswordUtil.emptyToNull(txtPhone.getText()));
        t.setDegree(txtDegree.getText().trim());
        t.setExperience(txtExperience.getText().trim());
        t.setStatus(cboStatus != null && cboStatus.getValue() != null ? cboStatus.getValue() : "Active");

        if (teacherDAO.createTeacherWithSkills(t, t.getTeacherCode().toLowerCase(), PasswordUtil.sha256("Teacher@123"), selectedSkillIDs, cboProficiencyLevel.getValue())) {
            tblTeacherList.setItems(FXCollections.observableArrayList(teacherDAO.getAllTeachers()));
            handleClearTeacher();
            new Alert(Alert.AlertType.INFORMATION, "Teacher and skill types saved!").showAndWait();
        } else {
            new Alert(Alert.AlertType.ERROR, "Failed to save teacher (possible duplicate code, username, or email).").showAndWait();
        }
    }

    @FXML
    private void handleUpdateTeacher() {
        if (selectedTeacher == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a teacher from the table to update!").showAndWait();
            return;
        }
        List<Integer> selectedSkillIDs = getSelectedSkillIDs();
        if (selectedSkillIDs.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Teacher must have at least one skill!").showAndWait();
            return;
        }
        String fullName = txtFullName.getText().trim();
        if (fullName.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Full Name is required!").showAndWait();
            return;
        }
        selectedTeacher.setFullName(fullName);
        selectedTeacher.setEmail(PasswordUtil.emptyToNull(txtEmail.getText()));
        selectedTeacher.setPhone(PasswordUtil.emptyToNull(txtPhone.getText()));
        selectedTeacher.setDegree(txtDegree.getText().trim());
        selectedTeacher.setExperience(txtExperience.getText().trim());
        selectedTeacher.setStatus(cboStatus != null && cboStatus.getValue() != null ? cboStatus.getValue() : "Active");

        if (teacherDAO.updateTeacherWithSkills(selectedTeacher, selectedSkillIDs, cboProficiencyLevel.getValue())) {
            tblTeacherList.setItems(FXCollections.observableArrayList(teacherDAO.getAllTeachers()));
            handleClearTeacher();
            new Alert(Alert.AlertType.INFORMATION, "Teacher updated successfully!").showAndWait();
        } else {
            new Alert(Alert.AlertType.ERROR, "Failed to update teacher (possible duplicate email).").showAndWait();
        }
    }

    @FXML
    private void handleDeleteTeacher() {
        if (selectedTeacher == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a teacher to deactivate!").showAndWait();
            return;
        }
        if (teacherDAO.deleteTeacher(selectedTeacher.getTeacherID())) {
            tblTeacherList.setItems(FXCollections.observableArrayList(teacherDAO.getAllTeachers()));
            handleClearTeacher();
            new Alert(Alert.AlertType.INFORMATION, "Teacher deactivated!").showAndWait();
        } else {
            new Alert(Alert.AlertType.ERROR, "Cannot deactivate teacher: Teacher is currently assigned to an active class.").showAndWait();
        }
    }

    @FXML
    private void handleClearTeacher() {
        selectedTeacher = null;
        txtTeacherCode.clear();
        txtTeacherCode.setDisable(false);
        txtFullName.clear();
        txtEmail.clear();
        txtPhone.clear();
        txtDegree.clear();
        txtExperience.clear();
        chkListening.setSelected(false);
        chkSpeaking.setSelected(false);
        chkReading.setSelected(false);
        chkWriting.setSelected(false);
        if (cboStatus != null) cboStatus.setValue("Active");
        tblTeacherList.getSelectionModel().clearSelection();
    }
}

package controller;

import dao.TeachingScheduleDAO;
import model.TeachingSchedule;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;

public class TeachingScheduleController {
    @FXML private ComboBox<String> cboClass, cboTeacher, cboSkillType;
    @FXML private DatePicker dpTeachingDate;
    @FXML private TextField txtStartTime, txtEndTime, txtRoom;
    @FXML private TableView<TeachingSchedule> tblScheduleList;
    @FXML private TableColumn<TeachingSchedule, String> colClass, colTeacher, colSkill, colRoom, colStatus;
    @FXML private TableColumn<TeachingSchedule, LocalDate> colDate;
    @FXML private TableColumn<TeachingSchedule, LocalTime> colStartTime, colEndTime;

    private final TeachingScheduleDAO scheduleDAO = new TeachingScheduleDAO();

    @FXML
    public void initialize() {
        colClass.setCellValueFactory(new PropertyValueFactory<>("className"));
        colTeacher.setCellValueFactory(new PropertyValueFactory<>("teacherName"));
        colSkill.setCellValueFactory(new PropertyValueFactory<>("skillName"));
        if (colDate != null) colDate.setCellValueFactory(new PropertyValueFactory<>("teachingDate"));
        if (colStartTime != null) colStartTime.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        if (colEndTime != null) colEndTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        colRoom.setCellValueFactory(new PropertyValueFactory<>("room"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadDropdownData();
        tblScheduleList.setItems(FXCollections.observableArrayList(scheduleDAO.getAllSchedules()));
    }

    private void loadDropdownData() {
        try (Connection conn = DBConnection.getConnection()) {
            // Classes
            try (PreparedStatement ps = conn.prepareStatement("SELECT ClassID, ClassName FROM Class");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cboClass.getItems().add(rs.getInt("ClassID") + " - " + rs.getString("ClassName"));
                }
                if (!cboClass.getItems().isEmpty()) cboClass.getSelectionModel().select(0);
            }

            // Teachers
            try (PreparedStatement ps = conn.prepareStatement("SELECT t.TeacherID, u.FullName FROM Teacher t JOIN User u ON t.UserID = u.UserID");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cboTeacher.getItems().add(rs.getInt("TeacherID") + " - " + rs.getString("FullName"));
                }
                if (!cboTeacher.getItems().isEmpty()) cboTeacher.getSelectionModel().select(0);
            }

            // Skills
            try (PreparedStatement ps = conn.prepareStatement("SELECT SkillTypeID, Name FROM SkillType");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cboSkillType.getItems().add(rs.getInt("SkillTypeID") + " - " + rs.getString("Name"));
                }
                if (!cboSkillType.getItems().isEmpty()) cboSkillType.getSelectionModel().select(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSaveSchedule() {
        try {
            if (cboClass.getValue() == null || !cboClass.getValue().contains(" - ") ||
                cboTeacher.getValue() == null || !cboTeacher.getValue().contains(" - ") ||
                cboSkillType.getValue() == null || !cboSkillType.getValue().contains(" - ")) {
                new Alert(Alert.AlertType.WARNING, "Please select Class, Teacher, and Skill Type!").showAndWait();
                return;
            }
            int classID = Integer.parseInt(cboClass.getValue().split(" - ")[0]);
            int teacherID = Integer.parseInt(cboTeacher.getValue().split(" - ")[0]);
            int skillID = Integer.parseInt(cboSkillType.getValue().split(" - ")[0]);

            TeachingSchedule ts = new TeachingSchedule();
            ts.setClassID(classID);
            ts.setTeacherID(teacherID);
            ts.setSkillTypeID(skillID);
            ts.setTeachingDate(dpTeachingDate.getValue() != null ? dpTeachingDate.getValue() : LocalDate.now().plusDays(1));
            ts.setStartTime(LocalTime.parse(txtStartTime.getText().trim()));
            ts.setEndTime(LocalTime.parse(txtEndTime.getText().trim()));
            ts.setRoom(txtRoom.getText().trim());

            String result = scheduleDAO.assignTeachingSchedule(ts);
            if ("SUCCESS".equals(result)) {
                tblScheduleList.setItems(FXCollections.observableArrayList(scheduleDAO.getAllSchedules()));
                new Alert(Alert.AlertType.INFORMATION, "Teaching schedule assigned successfully!").showAndWait();
            } else {
                new Alert(Alert.AlertType.ERROR, result).showAndWait();
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.WARNING, "Invalid time format (use HH:mm, e.g., 18:00) or missing fields!").showAndWait();
        }
    }

    @FXML
    private void handleDeleteSchedule() {
        TeachingSchedule selected = tblScheduleList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a schedule row to delete.").showAndWait();
            return;
        }
        if (scheduleDAO.deleteSchedule(selected.getScheduleID())) {
            tblScheduleList.setItems(FXCollections.observableArrayList(scheduleDAO.getAllSchedules()));
            new Alert(Alert.AlertType.INFORMATION, "Schedule deleted successfully!").showAndWait();
        } else {
            new Alert(Alert.AlertType.ERROR, "Cannot delete completed schedule or database error.").showAndWait();
        }
    }
}

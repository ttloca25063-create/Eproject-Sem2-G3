package controller;

import dao.PeriodicTestDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class PeriodicTestController {
    @FXML private TextField txtClassID;
    @FXML private ComboBox<String> cboSkillType, cboCourse;
    @FXML private TextField txtTestName;
    @FXML private DatePicker dpTestDate;
    @FXML private TextField txtStudentID;
    @FXML private TextField txtScore;
    @FXML private TextField txtNote;

    @FXML private TableView<ObservableList<String>> tblTests;
    @FXML private TableColumn<ObservableList<String>, String> colTestID;
    @FXML private TableColumn<ObservableList<String>, String> colClassID;
    @FXML private TableColumn<ObservableList<String>, String> colSkill;
    @FXML private TableColumn<ObservableList<String>, String> colTestName;
    @FXML private TableColumn<ObservableList<String>, String> colTestDate;

    @FXML private TableView<ObservableList<String>> tblTestScores;
    @FXML private TableColumn<ObservableList<String>, String> colScoreStudentID, colScoreStudentCode, colScoreStudentName, colScoreValue, colScoreNote;

    @FXML private TableView<ObservableList<String>> tblCourseGradebook;
    @FXML private TableColumn<ObservableList<String>, String> colGbCourse, colGbClass, colGbStudentCode, colGbFullName, colGbTestsTaken, colGbDetails, colGbAvgScore;

    private final ObservableList<ObservableList<String>> testList = FXCollections.observableArrayList();
    private final ObservableList<ObservableList<String>> testScoreList = FXCollections.observableArrayList();
    private final ObservableList<ObservableList<String>> gradebookList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colTestID.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(0)));
        colClassID.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(1)));
        colSkill.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(2)));
        colTestName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(3)));
        colTestDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(4)));
        tblTests.setItems(testList);

        if (tblTestScores != null) {
            colScoreStudentID.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(0)));
            colScoreStudentCode.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(1)));
            colScoreStudentName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(2)));
            colScoreValue.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(3)));
            colScoreNote.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(4)));
            tblTestScores.setItems(testScoreList);

            tblTestScores.getSelectionModel().selectedItemProperty().addListener((obs, o, sel) -> {
                if (sel != null && sel.size() >= 5) {
                    txtStudentID.setText(sel.get(0));
                    txtScore.setText(sel.get(3));
                    txtNote.setText(sel.get(4));
                }
            });
        }

        if (tblCourseGradebook != null) {
            colGbCourse.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(0)));
            colGbClass.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(1)));
            colGbStudentCode.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(2)));
            colGbFullName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(3)));
            colGbTestsTaken.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(4)));
            colGbDetails.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(5)));
            colGbAvgScore.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(6)));
            tblCourseGradebook.setItems(gradebookList);
        }

        tblTests.getSelectionModel().selectedItemProperty().addListener((obs, o, sel) -> {
            if (sel != null) {
                loadScoresForSelectedTest(Integer.parseInt(sel.get(0)));
            }
        });

        loadSkills();
        loadCourses();
        loadTests();
        handleLoadCourseGradebook();
    }

    private void loadSkills() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT skillTypeID, name FROM SkillType");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cboSkillType.getItems().add(rs.getInt("skillTypeID") + " - " + rs.getString("name"));
            }
            if (!cboSkillType.getItems().isEmpty()) {
                cboSkillType.getSelectionModel().select(0);
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private void loadCourses() {
        if (cboCourse == null) return;
        cboCourse.getItems().clear();
        cboCourse.getItems().add("0 - All Courses");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT CourseID, CourseCode, CourseName FROM Course ORDER BY CourseID");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cboCourse.getItems().add(rs.getInt("CourseID") + " - " + rs.getString("CourseCode") + " (" + rs.getString("CourseName") + ")");
            }
            cboCourse.getSelectionModel().select(0);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private void loadTests() {
        testList.clear();
        String sql = "SELECT pt.testID, pt.classID, st.name, pt.testName, pt.testDate "
                   + "FROM PeriodicTest pt JOIN SkillType st ON pt.skillTypeID = st.skillTypeID ORDER BY pt.testID DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                testList.add(FXCollections.observableArrayList(
                    String.valueOf(rs.getInt("testID")),
                    String.valueOf(rs.getInt("classID")),
                    rs.getString("name"),
                    rs.getString("testName"),
                    rs.getDate("testDate").toString()
                ));
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private void loadScoresForSelectedTest(int testID) {
        if (tblTestScores == null) return;
        testScoreList.clear();
        try (Connection conn = DBConnection.getConnection()) {
            PeriodicTestDAO dao = new PeriodicTestDAO(conn);
            for (String[] row : dao.getScoresByTest(testID)) {
                testScoreList.add(FXCollections.observableArrayList(row));
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void handleLoadCourseGradebook() {
        if (tblCourseGradebook == null) return;
        gradebookList.clear();
        Integer courseID = null;
        if (cboCourse != null && cboCourse.getValue() != null && cboCourse.getValue().contains(" - ")) {
            int parsed = Integer.parseInt(cboCourse.getValue().split(" - ")[0]);
            if (parsed > 0) courseID = parsed;
        }
        try (Connection conn = DBConnection.getConnection()) {
            PeriodicTestDAO dao = new PeriodicTestDAO(conn);
            for (String[] row : dao.getCourseGradebook(courseID)) {
                gradebookList.add(FXCollections.observableArrayList(row));
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void handleCreateTest() {
        if (cboSkillType.getValue() == null || txtTestName.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter Class ID, Skill, and Test Name.");
            return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            int classID = Integer.parseInt(txtClassID.getText().trim());
            int skillID = Integer.parseInt(cboSkillType.getValue().split(" - ")[0]);
            Date date = Date.valueOf(dpTestDate.getValue() != null ? dpTestDate.getValue() : LocalDate.now());

            PeriodicTestDAO dao = new PeriodicTestDAO(conn);
            if (dao.createPeriodicTest(classID, skillID, txtTestName.getText().trim(), date, new BigDecimal("10.00"))) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Periodic test created.");
                txtTestName.clear();
                loadTests();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void handleSaveScore() {
        ObservableList<String> selected = tblTests.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Select Test", "Please select a test from the table first.");
            return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            int testID = Integer.parseInt(selected.get(0));
            int studentID = Integer.parseInt(txtStudentID.getText().trim());
            BigDecimal score = new BigDecimal(txtScore.getText().trim());
            if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(new BigDecimal("10.00")) > 0) {
                showAlert(Alert.AlertType.WARNING, "Invalid Score", "Score must be between 0 and 10.");
                return;
            }

            PeriodicTestDAO dao = new PeriodicTestDAO(conn);
            if (dao.saveStudentTestScore(testID, studentID, score, txtNote.getText().trim())) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Student test score saved.");
                loadScoresForSelectedTest(testID);
                handleLoadCourseGradebook();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

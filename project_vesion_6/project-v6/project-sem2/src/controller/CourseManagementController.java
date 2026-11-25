package controller;

import dao.CourseDAO;
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

public class CourseManagementController {
    @FXML private ComboBox<String> cboCertificateType;
    @FXML private TextField txtCourseCode;
    @FXML private TextField txtCourseName;
    @FXML private TextField txtDuration;
    @FXML private TextField txtTuitionFee;
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private TableView<ObservableList<String>> tblCourses;
    @FXML private TableColumn<ObservableList<String>, String> colCourseID;
    @FXML private TableColumn<ObservableList<String>, String> colCourseCode;
    @FXML private TableColumn<ObservableList<String>, String> colCertType;
    @FXML private TableColumn<ObservableList<String>, String> colCourseName;
    @FXML private TableColumn<ObservableList<String>, String> colDuration;
    @FXML private TableColumn<ObservableList<String>, String> colFee;
    @FXML private TableColumn<ObservableList<String>, String> colStatus;

    private final ObservableList<ObservableList<String>> courseList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colCourseID.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(0)));
        if (colCourseCode != null) {
            colCourseCode.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(1)));
        }
        colCertType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(2)));
        colCourseName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(3)));
        colDuration.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(4)));
        colFee.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(5)));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(6)));
        tblCourses.setItems(courseList);

        tblCourses.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, sel) -> {
            if (sel != null && sel.size() >= 7) {
                if (txtCourseCode != null) txtCourseCode.setText(sel.get(1));
                txtCourseName.setText(sel.get(3));
                txtDuration.setText(sel.get(4));
                txtTuitionFee.setText(sel.get(5));
                String certCode = sel.get(2);
                for (String item : cboCertificateType.getItems()) {
                    if (item.endsWith(" - " + certCode)) {
                        cboCertificateType.setValue(item);
                        break;
                    }
                }
            }
        });

        loadCertificateTypes();
        loadCourses();
    }

    private void loadCertificateTypes() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT certificateTypeID, code FROM CertificateType");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cboCertificateType.getItems().add(rs.getInt("certificateTypeID") + " - " + rs.getString("code"));
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private void loadCourses() {
        courseList.clear();
        String sql = "SELECT c.courseID, c.courseCode, ct.code, c.courseName, c.duration, c.tuitionFee, c.status "
                   + "FROM Course c LEFT JOIN CertificateType ct ON c.certificateTypeID = ct.certificateTypeID";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList(
                    String.valueOf(rs.getInt("courseID")),
                    rs.getString("courseCode") != null ? rs.getString("courseCode") : "",
                    rs.getString("code") != null ? rs.getString("code") : "",
                    rs.getString("courseName"),
                    String.valueOf(rs.getInt("duration")),
                    rs.getBigDecimal("tuitionFee") != null ? rs.getBigDecimal("tuitionFee").toString() : "0",
                    rs.getString("status")
                );
                courseList.add(row);
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void handleAddCourse() {
        String selectedCert = cboCertificateType.getValue();
        String courseCode = txtCourseCode != null ? txtCourseCode.getText().trim() : "";
        String courseName = txtCourseName.getText().trim();
        if (selectedCert == null || !selectedCert.contains(" - ")) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a Certificate Type.");
            return;
        }
        if (courseCode.isEmpty() || courseName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Course Code and Course Name are required.");
            return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            int certTypeID = Integer.parseInt(selectedCert.split(" - ")[0]);
            int duration = Integer.parseInt(txtDuration.getText().trim());
            BigDecimal fee = new BigDecimal(txtTuitionFee.getText().trim());
            Date start = Date.valueOf(dpStartDate.getValue() != null ? dpStartDate.getValue() : LocalDate.now());
            Date end = Date.valueOf(dpEndDate.getValue() != null ? dpEndDate.getValue() : LocalDate.now().plusMonths(2));

            CourseDAO dao = new CourseDAO(conn);
            if (dao.insertCourse(courseCode, certTypeID, courseName, duration, fee, start, end)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Course added successfully.");
                if (txtCourseCode != null) txtCourseCode.clear();
                txtCourseName.clear();
                txtDuration.clear();
                txtTuitionFee.clear();
                loadCourses();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", e.getMessage());
        }
    }

    @FXML
    private void handleUpdateCourse() {
        ObservableList<String> selected = tblCourses.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Select Course", "Please select a course row to update.");
            return;
        }
        String selectedCert = cboCertificateType.getValue();
        String courseCode = txtCourseCode != null ? txtCourseCode.getText().trim() : "";
        String courseName = txtCourseName.getText().trim();
        if (selectedCert == null || !selectedCert.contains(" - ") || courseCode.isEmpty() || courseName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Certificate Type, Course Code, and Course Name are required.");
            return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            int courseID = Integer.parseInt(selected.get(0));
            int certTypeID = Integer.parseInt(selectedCert.split(" - ")[0]);
            int duration = Integer.parseInt(txtDuration.getText().trim());
            BigDecimal fee = new BigDecimal(txtTuitionFee.getText().trim());
            Date start = Date.valueOf(dpStartDate.getValue() != null ? dpStartDate.getValue() : LocalDate.now());
            Date end = Date.valueOf(dpEndDate.getValue() != null ? dpEndDate.getValue() : LocalDate.now().plusMonths(2));

            CourseDAO dao = new CourseDAO(conn);
            if (dao.updateCourse(courseID, courseCode, certTypeID, courseName, duration, fee, start, end)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Course updated successfully.");
                loadCourses();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Update Error", e.getMessage());
        }
    }

    @FXML
    private void handleArchiveCourse() {
        ObservableList<String> selected = tblCourses.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Select Course", "Please select a course row.");
            return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            CourseDAO dao = new CourseDAO(conn);
            if (dao.archiveCourse(Integer.parseInt(selected.get(0)))) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Course archived.");
                loadCourses();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Cannot Archive", e.getMessage());
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
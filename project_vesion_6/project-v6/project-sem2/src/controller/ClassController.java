package controller;

import dao.ClassDAO;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.ClassRoom;
import util.DBConnection;

import java.sql.Connection;
import java.time.LocalDate;

public class ClassController {
    @FXML private TextField txtClassCode, txtClassName, txtCourseID, txtTeacherID, txtRoom, txtSchedule, txtMaxStudents, txtSearchKeyword;
    @FXML private DatePicker dpStartDate, dpEndDate;
    @FXML private ComboBox<String> cbStatus, cbFilterStatus;
    @FXML private TableView<ClassRoom> tblClasses;
    @FXML private ListView<String> lvClassStudents;

    @FXML private TableColumn<ClassRoom, Number> colClassID, colCourseID, colTeacherID, colMaxStudents;
    @FXML private TableColumn<ClassRoom, String> colClassCode, colClassName, colRoom, colSchedule, colStatus;

    @FXML
    public void initialize() {
        try {

            if (cbStatus != null) {
                cbStatus.setItems(FXCollections.observableArrayList("Open", "Ongoing", "Completed", "Cancelled"));
                cbStatus.setValue("Open");
            }
            if (cbFilterStatus != null) {
                cbFilterStatus.setItems(FXCollections.observableArrayList("All", "Open", "Ongoing", "Completed", "Cancelled"));
                cbFilterStatus.setValue("All");
            }

            if (colClassID != null) colClassID.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getClassID()));
            if (colClassCode != null) colClassCode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getClassCode()));
            if (colClassName != null) colClassName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getClassName()));
            if (colCourseID != null) colCourseID.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCourseID()));
            if (colTeacherID != null) colTeacherID.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getTeacherID() != null ? c.getValue().getTeacherID() : 0));
            if (colRoom != null) colRoom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoom()));
            if (colSchedule != null) colSchedule.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSchedule()));
            if (colMaxStudents != null) colMaxStudents.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getMaxStudents()));
            if (colStatus != null) colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));

            if (tblClasses != null) {
                tblClasses.getSelectionModel().selectedItemProperty().addListener((obs, oldV, sel) -> {
                    if (sel != null) {
                        txtClassCode.setText(sel.getClassCode());
                        txtClassName.setText(sel.getClassName());
                        txtCourseID.setText(String.valueOf(sel.getCourseID()));
                        txtTeacherID.setText(sel.getTeacherID() != null ? String.valueOf(sel.getTeacherID()) : "");
                        txtRoom.setText(sel.getRoom());
                        txtSchedule.setText(sel.getSchedule());
                        txtMaxStudents.setText(String.valueOf(sel.getMaxStudents()));
                        dpStartDate.setValue(sel.getStartDate());
                        dpEndDate.setValue(sel.getEndDate());
                        if (cbStatus != null) cbStatus.setValue(sel.getStatus());
                        handleViewStudents();
                    }
                });
            }

            handleSearchClass();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleCreateClass() {
        try (Connection conn = DBConnection.getConnection()) {
            ClassDAO classDAO = new ClassDAO(conn);
            ClassRoom newClass = new ClassRoom(
                0,
                txtClassCode.getText().trim(),
                txtClassName.getText().trim(),
                Integer.parseInt(txtCourseID.getText().trim()),
                txtTeacherID.getText().isEmpty() ? null : Integer.parseInt(txtTeacherID.getText().trim()),
                dpStartDate.getValue() != null ? dpStartDate.getValue() : LocalDate.now(),
                dpEndDate.getValue() != null ? dpEndDate.getValue() : LocalDate.now().plusMonths(3),
                Integer.parseInt(txtMaxStudents.getText().trim()),
                txtRoom.getText().trim(),
                txtSchedule.getText().trim(),
                cbStatus != null && cbStatus.getValue() != null ? cbStatus.getValue() : "Open"
            );

            if (classDAO.createClass(newClass)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Class created successfully!");
                handleSearchClass();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid input or duplicate Class Code: " + e.getMessage());
        }
    }

    @FXML
    public void handleEditClass() {
        ClassRoom selected = tblClasses.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a class to edit.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            ClassDAO classDAO = new ClassDAO(conn);
            selected.setClassName(txtClassName.getText().trim());
            selected.setCourseID(Integer.parseInt(txtCourseID.getText().trim()));
            selected.setTeacherID(txtTeacherID.getText().isEmpty() ? null : Integer.parseInt(txtTeacherID.getText().trim()));
            selected.setStartDate(dpStartDate.getValue());
            selected.setEndDate(dpEndDate.getValue());
            selected.setMaxStudents(Integer.parseInt(txtMaxStudents.getText().trim()));
            selected.setRoom(txtRoom.getText().trim());
            selected.setSchedule(txtSchedule.getText().trim());
            if (cbStatus != null) selected.setStatus(cbStatus.getValue());

            if (classDAO.updateClass(selected)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Class updated successfully!");
                handleSearchClass();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Update failed: " + e.getMessage());
        }
    }

    @FXML
    public void handleSearchClass() {
        try (Connection conn = DBConnection.getConnection()) {
            ClassDAO classDAO = new ClassDAO(conn);
            String keyword = txtSearchKeyword != null && txtSearchKeyword.getText() != null ? txtSearchKeyword.getText().trim() : "";
            String status = cbFilterStatus != null && cbFilterStatus.getValue() != null ? cbFilterStatus.getValue() : "All";
            tblClasses.setItems(FXCollections.observableArrayList(classDAO.searchClasses(keyword, status)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleViewStudents() {
        if (tblClasses == null) return;
        ClassRoom selected = tblClasses.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try (Connection conn = DBConnection.getConnection()) {
            ClassDAO classDAO = new ClassDAO(conn);
            lvClassStudents.setItems(FXCollections.observableArrayList(classDAO.getStudentsInClass(selected.getClassID())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    @FXML
    public void handleDeleteClass() {
        ClassRoom selected = tblClasses.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a class to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete class [" + selected.getClassCode() + " - " + selected.getClassName() + "]?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            ClassDAO classDAO = new ClassDAO(conn);
            String result = classDAO.deleteClass(selected.getClassID());
            if ("SUCCESS".equals(result)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Class deleted successfully!");
                clearForm();
                handleSearchClass();
            } else {
                showAlert(Alert.AlertType.ERROR, "Cannot Delete", result);
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Delete failed: " + e.getMessage());
        }
    }

    private void clearForm() {
        txtClassCode.clear();
        txtClassName.clear();
        txtCourseID.clear();
        txtTeacherID.clear();
        txtRoom.clear();
        txtSchedule.clear();
        txtMaxStudents.clear();
        dpStartDate.setValue(null);
        dpEndDate.setValue(null);
        if (cbStatus != null) cbStatus.setValue("Open");
        if (lvClassStudents != null) lvClassStudents.getItems().clear();
    }
}



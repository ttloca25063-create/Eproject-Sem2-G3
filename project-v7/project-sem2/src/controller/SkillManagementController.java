package controller;

import dao.SkillTypeDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.SkillItem;
import util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SkillManagementController {
    @FXML private TextField txtSkillCode;
    @FXML private TextField txtSkillName;
    @FXML private TextField txtMinScore;
    @FXML private TableView<SkillItem> tblSkills;
    @FXML private TableColumn<SkillItem, String> colSkillID;
    @FXML private TableColumn<SkillItem, String> colSkillCode;
    @FXML private TableColumn<SkillItem, String> colSkillName;
    @FXML private TableColumn<SkillItem, String> colMinScore;

    private final ObservableList<SkillItem> skillList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colSkillID.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getSkillTypeID())));
        colSkillCode.setCellValueFactory(d -> d.getValue().codeProperty());
        colSkillName.setCellValueFactory(d -> d.getValue().nameProperty());
        colMinScore.setCellValueFactory(d -> d.getValue().minPassScoreProperty());
        tblSkills.setItems(skillList);

        tblSkills.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtSkillCode.setText(newVal.codeProperty().get());
                txtSkillName.setText(newVal.nameProperty().get());
                txtMinScore.setText(newVal.minPassScoreProperty().get());
            }
        });
        loadSkills();
    }

    private void loadSkills() {
        skillList.clear();
        String sql = "SELECT skillTypeID, code, name, minPassScore FROM SkillType ORDER BY skillTypeID";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                skillList.add(new SkillItem(
                    rs.getInt("skillTypeID"),
                    rs.getString("code"),
                    rs.getString("name"),
                    rs.getBigDecimal("minPassScore").toString()
                ));
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Load Error", e.getMessage());
        }
    }

    @FXML
    private void handleAddSkill() {
        try (Connection conn = DBConnection.getConnection()) {
            SkillTypeDAO dao = new SkillTypeDAO(conn);
            BigDecimal score = new BigDecimal(txtMinScore.getText().trim());
            if (dao.insertSkill(txtSkillCode.getText().trim(), txtSkillName.getText().trim(), score)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Skill added successfully.");
                loadSkills();
                handleClear();
            } else {
                showAlert(Alert.AlertType.WARNING, "Duplicate", "Skill code or name already exists.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void handleUpdateSkill() {
        SkillItem selected = tblSkills.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Select Item", "Please select a skill row to update.");
            return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            SkillTypeDAO dao = new SkillTypeDAO(conn);
            BigDecimal score = new BigDecimal(txtMinScore.getText().trim());
            if (dao.updateSkill(selected.getSkillTypeID(), txtSkillName.getText().trim(), score)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Skill updated successfully.");
                loadSkills();
                handleClear();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void handleDeleteSkill() {
        SkillItem selected = tblSkills.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Select Item", "Please select a skill row to delete.");
            return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            SkillTypeDAO dao = new SkillTypeDAO(conn);
            if (dao.deleteSkill(selected.getSkillTypeID())) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Skill deleted successfully.");
                loadSkills();
                handleClear();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Cannot Delete", e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        txtSkillCode.clear();
        txtSkillName.clear();
        txtMinScore.clear();
        tblSkills.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
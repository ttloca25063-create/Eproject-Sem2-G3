package controller;

import dao.CertificateReportDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.EligibilityRecord;
import util.DBConnection;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;

public class EligibilityLookupController {
    @FXML private TextField txtKeyword;
    @FXML private Button btnSearch;
    @FXML private Button btnExportCsv;
    @FXML private TableView<EligibilityRecord> tblResults;
    @FXML private TableColumn<EligibilityRecord, String> colFullName;
    @FXML private TableColumn<EligibilityRecord, String> colStudentCode;
    @FXML private TableColumn<EligibilityRecord, String> colExamName;
    @FXML private TableColumn<EligibilityRecord, String> colAverageScore;
    @FXML private TableColumn<EligibilityRecord, String> colResult;
    @FXML private TableColumn<EligibilityRecord, String> colCertNumber;

    private final ObservableList<EligibilityRecord> recordList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colFullName.setCellValueFactory(data -> data.getValue().fullNameProperty());
        colStudentCode.setCellValueFactory(data -> data.getValue().studentCodeProperty());
        colExamName.setCellValueFactory(data -> data.getValue().examNameProperty());
        colAverageScore.setCellValueFactory(data -> data.getValue().averageScoreProperty());
        colResult.setCellValueFactory(data -> data.getValue().resultProperty());
        colCertNumber.setCellValueFactory(data -> data.getValue().certNumberProperty());
        tblResults.setItems(recordList);
    }

    @FXML
    private void handleSearch() {
        String keyword = txtKeyword.getText().trim();
        if (keyword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Required", "Please enter a Student Code, ID Number, or Certificate Number.");
            return;
        }

        recordList.clear();
        try (Connection conn = DBConnection.getConnection()) {
            CertificateReportDAO dao = new CertificateReportDAO(conn);
            try (ResultSet rs = dao.lookupEligibility(keyword)) {
                while (rs.next()) {
                    recordList.add(new EligibilityRecord(
                        rs.getString("fullName"),
                        rs.getString("studentCode"),
                        rs.getString("examName"),
                        rs.getString("averageScore"),
                        rs.getString("result"),
                        rs.getString("certificateNumber")
                    ));
                }
            }
            if (recordList.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "No Results", "No matching candidate or certificate record found.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    @FXML
    private void handleExportCsv() {
        if (recordList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Data", "Please search and load records before exporting.");
            return;
        }
        try (FileWriter writer = new FileWriter("Eligibility_Report.csv")) {
            writer.write("Candidate Name,Student Code,Exam Name,Average Score,Result,Certificate Number\n");
            for (EligibilityRecord r : recordList) {
                writer.write(String.format("%s,%s,%s,%s,%s,%s\n",
                    r.fullNameProperty().get(),
                    r.studentCodeProperty().get(),
                    r.examNameProperty().get(),
                    r.averageScoreProperty().get(),
                    r.resultProperty().get(),
                    r.certNumberProperty().get()));
            }
            showAlert(Alert.AlertType.INFORMATION, "Export Success", "Report saved to Eligibility_Report.csv");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Export Error", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
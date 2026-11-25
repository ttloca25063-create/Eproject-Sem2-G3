package controller;

import dao.CertificateDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.User;
import service.CertificateService;
import util.DBConnection;
import util.SessionManager;

import java.sql.Connection;

public class CertificateController {
    @FXML private TextField txtExamResultID;
    @FXML private Label lblEligibilityStatus, lblGeneratedCertNumber;
    @FXML private Button btnIssueCertificate, btnRevokeCertificate;

    @FXML private TableView<ObservableList<String>> tblPendingResults;
    @FXML private TableColumn<ObservableList<String>, String> colPendingResultID, colPendingName, colPendingExam, colPendingScore, colPendingPaid;

    @FXML private TableView<ObservableList<String>> tblCertificates;
    @FXML private TableColumn<ObservableList<String>, String> colCertID, colCertNumber, colCertType, colCertCandidate, colCertScore, colCertDate, colCertStatus;

    private final ObservableList<ObservableList<String>> pendingList = FXCollections.observableArrayList();
    private final ObservableList<ObservableList<String>> certList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        User current = SessionManager.getCurrentUser();
        boolean isStudent = current != null && current.getRoleID() == 3;
        if (isStudent) {
            if (btnIssueCertificate != null) {
                btnIssueCertificate.setVisible(false);
                btnIssueCertificate.setManaged(false);
            }
            if (btnRevokeCertificate != null) {
                btnRevokeCertificate.setVisible(false);
                btnRevokeCertificate.setManaged(false);
            }
        }

        if (tblPendingResults != null) {
            colPendingResultID.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(0)));
            colPendingName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(1)));
            colPendingExam.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(2)));
            colPendingScore.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(3)));
            colPendingPaid.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(4)));
            tblPendingResults.setItems(pendingList);

            tblPendingResults.getSelectionModel().selectedItemProperty().addListener((obs, o, sel) -> {
                if (sel != null) {
                    txtExamResultID.setText(sel.get(0));
                    handleVerifyEligibility();
                }
            });
        }

        if (tblCertificates != null) {
            colCertID.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(0)));
            colCertNumber.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(1)));
            colCertType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(2)));
            colCertCandidate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(3)));
            colCertScore.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(4)));
            colCertDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(5)));
            colCertStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(6)));
            tblCertificates.setItems(certList);
        }

        loadTables();
    }

    private void loadTables() {
        try (Connection conn = DBConnection.getConnection()) {
            CertificateDAO dao = new CertificateDAO(conn);
            if (tblPendingResults != null) {
                pendingList.clear();
                for (String[] row : dao.getPendingPassedResults()) {
                    pendingList.add(FXCollections.observableArrayList(row));
                }
            }
            if (tblCertificates != null) {
                certList.clear();
                for (String[] row : dao.getIssuedCertificatesDisplay()) {
                    certList.add(FXCollections.observableArrayList(row));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleVerifyEligibility() {
        try (Connection conn = DBConnection.getConnection()) {
            CertificateService certService = new CertificateService(conn);
            int resultID = Integer.parseInt(txtExamResultID.getText().trim());
            String status = certService.checkEligibility(resultID);

            if ("ELIGIBLE".equals(status)) {
                lblEligibilityStatus.setText("Status: ELIGIBLE FOR CERTIFICATE");
                lblEligibilityStatus.setStyle("-fx-text-fill: #22543D; -fx-font-weight: bold; -fx-font-size: 14px;");
                User current = SessionManager.getCurrentUser();
                boolean isStudent = current != null && current.getRoleID() == 3;
                btnIssueCertificate.setDisable(isStudent);
            } else {
                lblEligibilityStatus.setText(status);
                lblEligibilityStatus.setStyle("-fx-text-fill: #742A2A; -fx-font-weight: bold; -fx-font-size: 14px;");
                btnIssueCertificate.setDisable(true);
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Please enter a valid numeric Exam Result ID.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleIssueCertificate() {
        User current = SessionManager.getCurrentUser();
        if (current != null && current.getRoleID() == 3) {
            showAlert(Alert.AlertType.WARNING, "Access Denied", "Only Staff/Admin can issue certificates.");
            return;
        }
        int staffUserID = current != null ? current.getUserID() : 1;
        try (Connection conn = DBConnection.getConnection()) {
            CertificateService certService = new CertificateService(conn);
            int resultID = Integer.parseInt(txtExamResultID.getText().trim());
            String certNumber = certService.issueCertificate(resultID, staffUserID);

            if (certNumber != null) {
                lblGeneratedCertNumber.setText("Issued Certificate Number: " + certNumber);
                lblEligibilityStatus.setText("Certificate successfully issued!");
                btnIssueCertificate.setDisable(true);
                loadTables();
                showAlert(Alert.AlertType.INFORMATION, "Certificate Issued",
                          "Certificate generated successfully!\nCertificate Number: " + certNumber);
            }
        } catch (IllegalStateException ex) {
            showAlert(Alert.AlertType.ERROR, "Blocked", ex.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleRevokeCertificate() {
        User current = SessionManager.getCurrentUser();
        if (current != null && current.getRoleID() == 3) {
            showAlert(Alert.AlertType.WARNING, "Access Denied", "Only Staff/Admin can revoke certificates.");
            return;
        }
        if (tblCertificates == null) return;
        ObservableList<String> sel = tblCertificates.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select an issued certificate to revoke.");
            return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            CertificateDAO dao = new CertificateDAO(conn);
            if (dao.revokeCertificate(Integer.parseInt(sel.get(0)))) {
                showAlert(Alert.AlertType.INFORMATION, "Revoked", "Certificate " + sel.get(1) + " has been revoked.");
                loadTables();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

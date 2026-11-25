package ui;

import controller.ExamController;
import dao.CertificateTypeDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.CertificateType;
import model.Exam;
import util.DBConnection;

import java.time.LocalDate;

public class ExamManagementUIController {

    @FXML
    private TableView<Exam> tblExams;
    @FXML
    private TableColumn<Exam, Integer> colID;
    @FXML
    private TableColumn<Exam, String> colCode, colName, colCertType, colStatus;
    @FXML
    private TableColumn<Exam, LocalDate> colExamDate, colDeadline;
    @FXML
    private TableColumn<Exam, Double> colFee;

    @FXML
    private TextField txtCode, txtName, txtFee, txtLocation;
    @FXML
    private ComboBox<CertificateType> cbCertType;
    @FXML
    private ComboBox<String> cbStatus;
    @FXML
    private DatePicker dpExamDate, dpDeadline;
    @FXML
    private Label lblMessage;

    private final ExamController controller = new ExamController();
    private final ObservableList<Exam> data = FXCollections.observableArrayList();
    private Exam selectedExam = null;

    @FXML
    public void initialize() {
        colID.setCellValueFactory(new PropertyValueFactory<>("examID"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("examCode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("examName"));
        colCertType.setCellValueFactory(new PropertyValueFactory<>("certificateTypeName"));
        colExamDate.setCellValueFactory(new PropertyValueFactory<>("examDate"));
        colDeadline.setCellValueFactory(new PropertyValueFactory<>("registrationDeadline"));
        colFee.setCellValueFactory(new PropertyValueFactory<>("examFee"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tblExams.setItems(data);

        try (java.sql.Connection conn = DBConnection.getConnection()) {
            CertificateTypeDAO certTypeDAO = new CertificateTypeDAO(conn);
            cbCertType.setItems(FXCollections.observableArrayList(certTypeDAO.getAll()));
        } catch (java.sql.SQLException e) {
            showMessage("Lỗi kết nối CSDL: " + e.getMessage(), true);
            e.printStackTrace();
        }

        cbCertType.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(CertificateType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        cbCertType.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(CertificateType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        cbStatus.setItems(FXCollections.observableArrayList("Upcoming", "Open", "Closed", "Completed"));
        cbStatus.setValue("Open");

        tblExams.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedExam = newVal;
            if (newVal != null)
                fillForm(newVal);
        });

        loadData();
    }

    private void loadData() {
        data.setAll(controller.getAllExams());
    }

    private void fillForm(Exam e) {
        txtCode.setText(e.getExamCode());
        txtName.setText(e.getExamName());
        txtFee.setText(String.valueOf(e.getExamFee()));
        txtLocation.setText(e.getLocation());
        dpExamDate.setValue(e.getExamDate());
        dpDeadline.setValue(e.getRegistrationDeadline());
        cbStatus.setValue(e.getStatus());

        for (CertificateType ct : cbCertType.getItems()) {
            if (ct.getCertificateTypeID() == e.getCertificateTypeID()) {
                cbCertType.setValue(ct);
                break;
            }
        }
    }

    private Exam buildFromForm(int id) {
        Exam e = new Exam();
        e.setExamID(id);
        e.setExamCode(txtCode.getText().trim());
        e.setExamName(txtName.getText().trim());
        e.setLocation(txtLocation.getText().trim());
        e.setExamDate(dpExamDate.getValue());
        e.setRegistrationDeadline(dpDeadline.getValue());
        e.setStatus(cbStatus.getValue());

        CertificateType ct = cbCertType.getValue();
        e.setCertificateTypeID(ct != null ? ct.getCertificateTypeID() : 0);

        try {
            e.setExamFee(Double.parseDouble(txtFee.getText().trim()));
        } catch (NumberFormatException ex) {
            e.setExamFee(-1);
        }
        return e;
    }

    @FXML
    private void handleAdd() {
        if (cbCertType.getValue() == null) {
            showMessage("Error: Please select a certificate type.", true);
            return;
        }
        String res = controller.saveExam(buildFromForm(0));
        boolean err = res.toLowerCase().startsWith("error");
        showMessage(res, err);
        if (!err) {
            loadData();
            handleClear();
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedExam == null) {
            showMessage("Error: Select an exam from the table to update.", true);
            return;
        }
        if (cbCertType.getValue() == null) {
            showMessage("Error: Please select a certificate type.", true);
            return;
        }
        String res = controller.saveExam(buildFromForm(selectedExam.getExamID()));
        boolean err = res.toLowerCase().startsWith("error");
        showMessage(res, err);
        if (!err)
            loadData();
    }

    @FXML
    private void handleDelete() {
        if (selectedExam == null) {
            showMessage("Error: Select an exam to delete.", true);
            return;
        }
        String res = controller.deleteExam(selectedExam.getExamID());
        boolean err = res.toLowerCase().startsWith("error");
        showMessage(res, err);
        if (!err) {
            loadData();
            handleClear();
        }
    }

    @FXML
    private void handleClear() {
        selectedExam = null;
        tblExams.getSelectionModel().clearSelection();
        txtCode.clear();
        txtName.clear();
        txtFee.clear();
        txtLocation.clear();
        dpExamDate.setValue(null);
        dpDeadline.setValue(null);
        cbCertType.setValue(null);
        cbStatus.setValue("Open");
        lblMessage.setText("");
    }

    private void showMessage(String msg, boolean isError) {
        lblMessage.setText(msg);
        lblMessage.setStyle(isError ? "-fx-text-fill: #c0392b;" : "-fx-text-fill: #27ae60;");
    }
}
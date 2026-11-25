package ui;

import controller.ExamRegistrationController;
import dao.ExamDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Exam;
import model.ExamRegistration;

public class ExamRegistrationUIController {

    @FXML private ComboBox<Exam> cbExam;
    @FXML private RadioButton rbInternal, rbExternal;
    @FXML private TextField txtStudentCode, txtFullName, txtIdNumber, txtPhone, txtEmail;
    @FXML private DatePicker dpDob;
    @FXML private Label lblStudentCode, lblFee, lblMessage;
    @FXML private TableView<ExamRegistration> tblRegistrations;
    @FXML private TableColumn<ExamRegistration, Integer> colRegID;
    @FXML private TableColumn<ExamRegistration, String> colName, colType, colPaid;
    @FXML private TableColumn<ExamRegistration, Double> colFee;

    private final ExamRegistrationController controller = new ExamRegistrationController();
    private final ExamDAO examDAO = new ExamDAO();
    private final ObservableList<ExamRegistration> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colRegID.setCellValueFactory(new PropertyValueFactory<>("registrationID"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colType.setCellValueFactory(new PropertyValueFactory<>("candidateType"));
        colFee.setCellValueFactory(new PropertyValueFactory<>("examFee"));
        colPaid.setCellValueFactory(new PropertyValueFactory<>("paidStatus"));
        tblRegistrations.setItems(data);

        cbExam.setItems(FXCollections.observableArrayList(examDAO.getAllOpenExams()));
        cbExam.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Exam item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getExamName());
            }
        });
        cbExam.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Exam item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getExamName());
            }
        });

        rbInternal.selectedProperty().addListener((obs, o, v) -> toggleType(v));
        cbExam.valueProperty().addListener((obs, o, exam) -> {
            updateFeeLabel(exam);
            if (exam != null) loadRegistrations(exam.getExamID()); else data.clear();
        });

        toggleType(true);
    }

    private void toggleType(boolean isInternal) {
        lblStudentCode.setVisible(isInternal);
        txtStudentCode.setVisible(isInternal);
        lblStudentCode.setManaged(isInternal);
        txtStudentCode.setManaged(isInternal);
        if (!isInternal) txtStudentCode.clear();
        updateFeeLabel(cbExam.getValue());
    }

    private void updateFeeLabel(Exam exam) {
        if (exam == null) { lblFee.setText("0 VNĐ"); return; }
        if (rbInternal.isSelected()) lblFee.setText("0 VNĐ (Miễn phí)");
        else lblFee.setText(String.format("%,.0f VNĐ", exam.getExamFee()));
    }

    private void loadRegistrations(int examID) {
        data.setAll(controller.getRegistrationList(examID));
    }

    @FXML
    private void handleRegister() {
        Exam exam = cbExam.getValue();
        if (exam == null) { showMessage("Chọn kỳ thi.", true); return; }
        String result = controller.handleRegister(
                exam.getExamID(),
                rbInternal.isSelected() ? txtStudentCode.getText().trim() : null,
                txtFullName.getText().trim(),
                dpDob.getValue(),
                txtIdNumber.getText().trim(),
                txtPhone.getText().trim(),
                txtEmail.getText().trim());
        boolean err = result.toLowerCase().startsWith("lỗi");
        showMessage(result, err);
        if (!err) loadRegistrations(exam.getExamID());
    }

    @FXML
    private void handleConfirmPayment() {
        ExamRegistration sel = tblRegistrations.getSelectionModel().getSelectedItem();
        if (sel == null) { showMessage("Chọn 1 dòng để xác nhận thanh toán.", true); return; }
        String r = controller.confirmPayment(sel.getRegistrationID());
        showMessage(r, r.toLowerCase().startsWith("lỗi"));
        if (cbExam.getValue() != null) loadRegistrations(cbExam.getValue().getExamID());
    }

    @FXML
    private void handleClear() {
        txtStudentCode.clear(); txtFullName.clear(); txtIdNumber.clear();
        txtPhone.clear(); txtEmail.clear(); dpDob.setValue(null);
        rbInternal.setSelected(true); lblMessage.setText("");
        updateFeeLabel(cbExam.getValue());
    }

    private void showMessage(String msg, boolean isError) {
        lblMessage.setText(msg);
        lblMessage.setStyle(isError
                ? "-fx-text-fill: #c0392b; -fx-font-weight: bold;"
                : "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }
}
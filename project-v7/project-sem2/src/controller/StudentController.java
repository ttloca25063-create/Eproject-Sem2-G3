package controller;

import dao.StudentDAO;
import model.Student;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import util.DBConnection;
import util.PasswordUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StudentController {
    @FXML private TextField txtStudentCode, txtFullName, txtEmail, txtPhone, txtTuitionFee, txtDiscount, txtPaidAmount;
    @FXML private DatePicker dpDateOfBirth;
    @FXML private ComboBox<String> cboClass, cboPaymentMethod;
    @FXML private TableView<Student> tblStudentList;
    @FXML private TableColumn<Student, String> colCode, colFullName, colClass, colPaymentStatus, colStatus;
    @FXML private TableColumn<Student, BigDecimal> colTuitionFee, colPaidAmount;

    private final StudentDAO studentDAO = new StudentDAO();

    @FXML
    public void initialize() {
        cboPaymentMethod.setItems(FXCollections.observableArrayList("Cash", "Bank Transfer"));
        cboPaymentMethod.getSelectionModel().select("Bank Transfer");
        colCode.setCellValueFactory(new PropertyValueFactory<>("studentCode"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colClass.setCellValueFactory(new PropertyValueFactory<>("className"));
        if (colTuitionFee != null) colTuitionFee.setCellValueFactory(new PropertyValueFactory<>("tuitionFee"));
        if (colPaidAmount != null) colPaidAmount.setCellValueFactory(new PropertyValueFactory<>("paidAmount"));
        colPaymentStatus.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadClasses();
        tblStudentList.setItems(FXCollections.observableArrayList(studentDAO.getAllStudentsWithEnrollment()));
    }

    private void loadClasses() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT ClassID, ClassName FROM Class WHERE Status != 'Closed'");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cboClass.getItems().add(rs.getInt("ClassID") + " - " + rs.getString("ClassName"));
            }
            if (!cboClass.getItems().isEmpty()) {
                cboClass.getSelectionModel().select(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEnrollStudent() {
        try {
            String studentCode = txtStudentCode.getText().trim();
            String fullName = txtFullName.getText().trim();
            if (studentCode.isEmpty() || fullName.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Student Code and Full Name are required!").showAndWait();
                return;
            }
            if (cboClass.getValue() == null || !cboClass.getValue().contains(" - ")) {
                new Alert(Alert.AlertType.WARNING, "Please select a class!").showAndWait();
                return;
            }

            Student st = new Student();
            st.setStudentCode(studentCode);
            st.setFullName(fullName);
            st.setEmail(PasswordUtil.emptyToNull(txtEmail.getText()));
            st.setPhone(PasswordUtil.emptyToNull(txtPhone.getText()));
            st.setDateOfBirth(dpDateOfBirth.getValue());

            int selectedClassID = Integer.parseInt(cboClass.getValue().split(" - ")[0]);

            BigDecimal fee = new BigDecimal(txtTuitionFee.getText().trim());
            BigDecimal discount = txtDiscount.getText().isEmpty() ? BigDecimal.ZERO : new BigDecimal(txtDiscount.getText().trim());
            BigDecimal paid = txtPaidAmount.getText().isEmpty() ? BigDecimal.ZERO : new BigDecimal(txtPaidAmount.getText().trim());

            boolean ok = studentDAO.enrollStudentAndRecordPayment(
                st, st.getStudentCode().toLowerCase(), PasswordUtil.sha256("Student@123"),
                selectedClassID, fee, discount, paid, cboPaymentMethod.getValue() != null ? cboPaymentMethod.getValue() : "Bank Transfer"
            );

            if (ok) {
                tblStudentList.setItems(FXCollections.observableArrayList(studentDAO.getAllStudentsWithEnrollment()));
                new Alert(Alert.AlertType.INFORMATION, "Student enrolled and tuition recorded!").showAndWait();
            } else {
                new Alert(Alert.AlertType.ERROR, "Enrollment failed: Class is full or payment amount exceeds fee!").showAndWait();
            }
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.WARNING, "Please enter valid numeric amounts for tuition/payment!").showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void handleRecordPayment() {
        Student selected = tblStudentList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a student from the table first!").showAndWait();
            return;
        }
        try {
            BigDecimal amount = new BigDecimal(txtPaidAmount.getText().trim());
            String method = cboPaymentMethod.getValue() != null ? cboPaymentMethod.getValue() : "Cash";
            String res = studentDAO.recordAdditionalPayment(selected.getStudentID(), amount, method);
            if ("SUCCESS".equals(res)) {
                tblStudentList.setItems(FXCollections.observableArrayList(studentDAO.getAllStudentsWithEnrollment()));
                new Alert(Alert.AlertType.INFORMATION, "Additional tuition payment recorded!").showAndWait();
            } else {
                new Alert(Alert.AlertType.ERROR, res).showAndWait();
            }
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.WARNING, "Please enter a valid payment amount in 'Initial Paid Amount'!").showAndWait();
        }
    }
}

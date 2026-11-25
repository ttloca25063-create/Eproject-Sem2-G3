package controller;

import dao.CertificateTypeDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.CertificateType;
import util.DBConnection;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.ResourceBundle;

public class CertificateTypeController implements Initializable {

    @FXML private TextField txtCode;
    @FXML private TextField txtName;
    @FXML private TextField txtLevel;
    @FXML private TextField txtPassScore;
    @FXML private TextArea txtDescription;
    @FXML private TableView<CertificateType> tblCertTypes;

    private ObservableList<CertificateType> certTypeList = FXCollections.observableArrayList();
    private CertificateType selectedCertType = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            setupTableColumns();
            loadTableData();

            tblCertTypes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedCertType = newSelection;
                    txtCode.setText(newSelection.getCode());
                    txtCode.setDisable(true); // Khóa ô Code khi sửa vì DAO update không đổi Code
                    txtName.setText(newSelection.getName());
                    txtLevel.setText(newSelection.getLevel());
                    if (txtPassScore != null) txtPassScore.setText(String.valueOf(newSelection.getPassScore()));
                    txtDescription.setText(newSelection.getDescription());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Không thể kết nối CSDL: " + e.getMessage());
        }
    }

    private void setupTableColumns() {
        TableColumn<CertificateType, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("certificateTypeID"));
        colId.setPrefWidth(50);

        TableColumn<CertificateType, String> colCode = new TableColumn<>("Code");
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colCode.setPrefWidth(100);

        TableColumn<CertificateType, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colName.setPrefWidth(200);

        TableColumn<CertificateType, String> colLevel = new TableColumn<>("Level");
        colLevel.setCellValueFactory(new PropertyValueFactory<>("level"));
        colLevel.setPrefWidth(100);

        TableColumn<CertificateType, Double> colPassScore = new TableColumn<>("Pass Score");
        colPassScore.setCellValueFactory(new PropertyValueFactory<>("passScore"));
        colPassScore.setPrefWidth(95);

        TableColumn<CertificateType, String> colDesc = new TableColumn<>("Description");
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDesc.setPrefWidth(250);

        tblCertTypes.getColumns().setAll(Arrays.asList(colId, colCode, colName, colLevel, colPassScore, colDesc));
    }

    private void loadTableData() {
        try (Connection conn = DBConnection.getConnection()) {
            CertificateTypeDAO dao = new CertificateTypeDAO(conn);
            certTypeList.setAll(dao.getAll());
            tblCertTypes.setItems(certTypeList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi tải dữ liệu", e.getMessage());
        }
    }

    @FXML
    private void handleAddCertificateType(ActionEvent event) {
        String code = txtCode.getText().trim();
        String name = txtName.getText().trim();
        if (code.isEmpty() || name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập Code và Certificate Name!");
            return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            CertificateTypeDAO dao = new CertificateTypeDAO(conn);
            if (dao.isCodeExists(code)) {
                showAlert(Alert.AlertType.WARNING, "Trùng mã", "Mã chứng chỉ (Code) này đã tồn tại!");
                return;
            }
            double passScore = 5.0;
            if (txtPassScore != null && !txtPassScore.getText().trim().isEmpty()) {
                passScore = Double.parseDouble(txtPassScore.getText().trim());
            }
            CertificateType ct = new CertificateType(0, code, name, txtLevel.getText().trim(), txtDescription.getText().trim(), passScore);
            if (dao.insert(ct)) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm loại chứng chỉ mới!");
                clearForm();
                loadTableData();
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Sai định dạng", "Pass Score phải là số hợp lệ!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi thêm dữ liệu", e.getMessage());
        }
    }

    @FXML
    private void handleUpdateCertificateType(ActionEvent event) {
        if (selectedCertType == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn dòng", "Vui lòng chọn một loại chứng chỉ trên bảng để sửa!");
            return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            CertificateTypeDAO dao = new CertificateTypeDAO(conn);
            double passScore = selectedCertType.getPassScore();
            if (txtPassScore != null && !txtPassScore.getText().trim().isEmpty()) {
                passScore = Double.parseDouble(txtPassScore.getText().trim());
            }
            selectedCertType.setName(txtName.getText().trim());
            selectedCertType.setLevel(txtLevel.getText().trim());
            selectedCertType.setPassScore(passScore);
            selectedCertType.setDescription(txtDescription.getText().trim());

            if (dao.update(selectedCertType)) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật loại chứng chỉ!");
                clearForm();
                loadTableData();
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Sai định dạng", "Pass Score phải là số hợp lệ!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi cập nhật", e.getMessage());
        }
    }

    private void clearForm() {
        txtCode.clear();
        txtCode.setDisable(false);
        txtName.clear();
        txtLevel.clear();
        if (txtPassScore != null) txtPassScore.clear();
        txtDescription.clear();
        selectedCertType = null;
        tblCertTypes.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    @FXML
    private void handleDeleteCertificateType(ActionEvent event) {
        if (selectedCertType == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn dòng", "Vui lòng chọn một loại chứng chỉ trên bảng để xóa!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc chắn muốn xóa loại chứng chỉ [" + selectedCertType.getCode() + " - " + selectedCertType.getName() + "] không?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            CertificateTypeDAO dao = new CertificateTypeDAO(conn);
            String result = dao.delete(selectedCertType.getCertificateTypeID());
            if ("SUCCESS".equals(result)) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa loại chứng chỉ!");
                clearForm();
                loadTableData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Không thể xóa", result);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi cơ sở dữ liệu", e.getMessage());
        }
    }
}
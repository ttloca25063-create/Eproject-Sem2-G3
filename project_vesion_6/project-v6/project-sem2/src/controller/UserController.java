package controller;

import dao.UserDAO;
import model.Permission;
import model.User;
import util.PasswordUtil;
import util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserController {
    @FXML private TextField txtSearch, txtUsername, txtFullName, txtEmail, txtPhone, txtAddress;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cboRole, cboStatus, cboPermRole;
    @FXML private TableView<User> tblUserList;
    @FXML private TableColumn<User, Integer> colUserID;
    @FXML private TableColumn<User, String> colUsername, colFullName, colRole, colStatus;
    @FXML private FlowPane flowPermissions;

    private final UserDAO userDAO = new UserDAO();
    private ObservableList<User> userList = FXCollections.observableArrayList();
    private final Map<Integer, CheckBox> permCheckBoxMap = new LinkedHashMap<>();
    private User selectedUser = null;

    @FXML
    public void initialize() {
        cboRole.setItems(FXCollections.observableArrayList("Admin", "Staff", "Student"));
        cboRole.getSelectionModel().select("Staff");
        cboStatus.setItems(FXCollections.observableArrayList("Active", "Inactive"));
        cboStatus.getSelectionModel().select("Active");

        colUserID.setCellValueFactory(new PropertyValueFactory<>("userID"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("roleName"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        tblUserList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, sel) -> {
            selectedUser = sel;
            if (sel != null) {
                txtUsername.setText(sel.getUsername());
                txtUsername.setDisable(true);
                txtPassword.clear();
                txtFullName.setText(sel.getFullName());
                txtEmail.setText(sel.getEmail() != null ? sel.getEmail() : "");
                txtPhone.setText(sel.getPhone() != null ? sel.getPhone() : "");
                txtAddress.setText(sel.getAddress() != null ? sel.getAddress() : "");
                cboRole.setValue(sel.getRoleName());
                cboStatus.setValue(sel.getStatus());
            }
        });

        loadUsers();
        initPermissionsPanel();
    }

    private void initPermissionsPanel() {
        if (flowPermissions == null || cboPermRole == null) return;
        cboPermRole.setItems(FXCollections.observableArrayList("Admin", "Staff", "Student"));
        List<Permission> perms = userDAO.getAllPermissions();
        flowPermissions.getChildren().clear();
        permCheckBoxMap.clear();
        for (Permission p : perms) {
            CheckBox cb = new CheckBox(p.getPermissionName() + " (" + p.getModule() + ")");
            permCheckBoxMap.put(p.getPermissionID(), cb);
            flowPermissions.getChildren().add(cb);
        }
        cboPermRole.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadPermissionsForRole(mapRoleNameToID(newVal));
            }
        });
        cboPermRole.getSelectionModel().select("Staff");
    }

    private void loadPermissionsForRole(int roleID) {
        Set<Integer> assigned = userDAO.getPermissionIDsByRole(roleID);
        for (Map.Entry<Integer, CheckBox> entry : permCheckBoxMap.entrySet()) {
            entry.getValue().setSelected(assigned.contains(entry.getKey()));
        }
    }

    private void loadUsers() {
        userList.setAll(userDAO.getAllUsers());
        FilteredList<User> filteredData = new FilteredList<>(userList, b -> true);
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
                filteredData.setPredicate(u -> newVal == null || newVal.isEmpty() ||
                    u.getUsername().toLowerCase().contains(newVal.toLowerCase()) ||
                    u.getFullName().toLowerCase().contains(newVal.toLowerCase()));
            });
        }
        tblUserList.setItems(filteredData);
    }

    @FXML
    private void handleSaveUser() {
        String username = txtUsername.getText().trim();
        String rawPassword = txtPassword.getText();
        if (username.isEmpty() || rawPassword.isEmpty() || txtFullName.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Username, Password, and Full Name are required!");
            return;
        }
        if (cboRole.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a role!");
            return;
        }
        if (userDAO.isUsernameExists(username, 0)) {
            showAlert(Alert.AlertType.ERROR, "Duplicate Error", "Username already exists!");
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(PasswordUtil.sha256(rawPassword));
        user.setFullName(txtFullName.getText().trim());
        user.setEmail(PasswordUtil.emptyToNull(txtEmail.getText()));
        user.setPhone(PasswordUtil.emptyToNull(txtPhone.getText()));
        user.setAddress(PasswordUtil.emptyToNull(txtAddress.getText()));
        user.setRoleID(mapRoleNameToID(cboRole.getValue()));
        user.setStatus(cboStatus.getValue() != null ? cboStatus.getValue() : "Active");

        if (userDAO.createUser(user)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "User account & role created successfully!");
            handleClearUser();
            loadUsers();
        } else {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not create user (possible duplicate email).");
        }
    }

    @FXML
    private void handleUpdateUser() {
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a user from the table to update.");
            return;
        }
        if (txtFullName.getText().trim().isEmpty() || cboRole.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Full Name and Role are required!");
            return;
        }
        selectedUser.setFullName(txtFullName.getText().trim());
        selectedUser.setEmail(PasswordUtil.emptyToNull(txtEmail.getText()));
        selectedUser.setPhone(PasswordUtil.emptyToNull(txtPhone.getText()));
        selectedUser.setAddress(PasswordUtil.emptyToNull(txtAddress.getText()));
        selectedUser.setRoleID(mapRoleNameToID(cboRole.getValue()));
        selectedUser.setStatus(cboStatus.getValue() != null ? cboStatus.getValue() : "Active");

        if (userDAO.updateUserAndRole(selectedUser)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "User and role updated successfully!");
            handleClearUser();
            loadUsers();
        } else {
            showAlert(Alert.AlertType.ERROR, "Update Error", "Could not update user (possible duplicate email).");
        }
    }

    @FXML
    private void handleClearUser() {
        selectedUser = null;
        txtUsername.clear();
        txtUsername.setDisable(false);
        txtPassword.clear();
        txtFullName.clear();
        txtEmail.clear();
        txtPhone.clear();
        txtAddress.clear();
        cboRole.getSelectionModel().select("Staff");
        cboStatus.getSelectionModel().select("Active");
        tblUserList.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleSavePermissions() {
        if (cboPermRole == null || cboPermRole.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Role Required", "Please select a role.");
            return;
        }
        int roleID = mapRoleNameToID(cboPermRole.getValue());
        List<Integer> selectedPermIDs = new ArrayList<>();
        for (Map.Entry<Integer, CheckBox> entry : permCheckBoxMap.entrySet()) {
            if (entry.getValue().isSelected()) {
                selectedPermIDs.add(entry.getKey());
            }
        }
        if (userDAO.updateRolePermissions(roleID, selectedPermIDs)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Permissions updated for role: " + cboPermRole.getValue());
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save role permissions.");
        }
    }

    @FXML
    private void handleDeleteUser() {
        User selected = tblUserList.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        int currentAdminID = SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getUserID() : 1;
        if (!userDAO.deactivateOrDeleteUser(selected.getUserID(), currentAdminID)) {
            showAlert(Alert.AlertType.ERROR, "Blocked", "Cannot deactivate currently logged-in Admin account!");
            return;
        }
        loadUsers();
    }

    private int mapRoleNameToID(String roleName) {
        if ("Admin".equals(roleName)) return 1;
        if ("Staff".equals(roleName)) return 2;
        return 3;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

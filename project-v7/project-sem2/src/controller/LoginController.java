package controller;

import dao.AuthDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;
import util.DBConnection;
import util.PasswordUtil;
import util.SessionManager;

import java.sql.Connection;
import java.util.List;

public class LoginController {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblStatus;
    @FXML private Button btnLogin;

    @FXML
    public void initialize() {
        if (txtPassword != null) {
            txtPassword.setOnAction(e -> handleLogin());
        }
    }

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String rawPassword = txtPassword.getText();

        if (username.isEmpty() || rawPassword.isEmpty()) {
            lblStatus.setText("Please enter both username and password.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            AuthDAO authDAO = new AuthDAO(conn);
            String hashed = PasswordUtil.sha256(rawPassword);
            User user = authDAO.login(username, hashed);

            if (user == null) {
                // Fallback check for raw password
                user = authDAO.login(username, rawPassword);
            }

            if (user != null) {
                List<String> perms = authDAO.getPermissionsByRole(user.getRoleID());
                SessionManager.setSession(user, perms);

                Parent root = FXMLLoader.load(getClass().getResource("/view/DashboardView.fxml"));
                Stage stage = (Stage) btnLogin.getScene().getWindow();
                stage.setTitle("Certificate Generation System - Dashboard [" + user.getFullName() + "]");
                stage.setScene(new Scene(root, 1200, 760));
                stage.centerOnScreen();
                stage.setResizable(true);
            } else {
                lblStatus.setText("Invalid username, password, or inactive account.");
            }
        } catch (Exception e) {
            lblStatus.setText("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

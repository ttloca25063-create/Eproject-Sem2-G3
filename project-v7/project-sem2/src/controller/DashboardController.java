package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.User;
import util.SessionManager;

public class DashboardController {
    @FXML private Label lblWelcome;
    @FXML private Label lblRole;
    @FXML private StackPane pnlContentArea;
    @FXML private Button btnLogout;

    // Navigation sections
    @FXML private VBox secAdmin;
    @FXML private VBox secAcademic;
    @FXML private VBox secExam;
    @FXML private VBox secStudent;

    // Admin buttons
    @FXML private Button btnUsers;
    @FXML private Button btnTeachers;
    @FXML private Button btnStudents;
    @FXML private Button btnSchedules;

    // Academic buttons
    @FXML private Button btnCertTypes;
    @FXML private Button btnCourses;
    @FXML private Button btnSkills;
    @FXML private Button btnClasses;
    @FXML private Button btnPeriodicTests;

    // Exam & Cert buttons
    @FXML private Button btnExams;
    @FXML private Button btnExamRegs;
    @FXML private Button btnExamResults;
    @FXML private Button btnCertificates;
    @FXML private Button btnReports;

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            String roleName = user.getRoleName() != null ? user.getRoleName() : getRoleNameById(user.getRoleID());
            lblWelcome.setText("👤 " + user.getFullName());
            if (lblRole != null) {
                lblRole.setText("Role: " + roleName.toUpperCase());
            }
            applyRolePermissions(roleName);
        } else {
            lblWelcome.setText("👤 Administrator");
            if (lblRole != null) lblRole.setText("Role: ADMIN");
            applyRolePermissions("Admin");
        }
    }

    private String getRoleNameById(int roleId) {
        switch (roleId) {
            case 1: return "Admin";
            case 2: return "Staff";
            case 3: return "Student";
            default: return "User";
        }
    }

    private void applyRolePermissions(String role) {
        boolean isAdmin = "Admin".equalsIgnoreCase(role);
        boolean isStaff = "Staff".equalsIgnoreCase(role);
        boolean isStudent = "Student".equalsIgnoreCase(role);

        // UC 02: Only Admin
        if (btnUsers != null) {
            btnUsers.setVisible(isAdmin);
            btnUsers.setManaged(isAdmin);
        }

        // Student Role: hide management sections, show only Student-related tasks
        if (isStudent) {
            if (secAdmin != null) { secAdmin.setVisible(false); secAdmin.setManaged(false); }
            if (secAcademic != null) { secAcademic.setVisible(false); secAcademic.setManaged(false); }
            if (btnExams != null) { btnExams.setVisible(false); btnExams.setManaged(false); }
            if (btnExamResults != null) { btnExamResults.setText("16. View Exam Marks"); }
            if (btnExamRegs != null) { btnExamRegs.setText("15. Register for Exam"); }
            if (btnCertificates != null) { btnCertificates.setText("17. View Certificate"); }
            openReports(); // Default view for student
        } else {
            // Admin or Staff: respect RolePermissions from SessionManager when logged in
            boolean hasSession = SessionManager.getCurrentUser() != null;
            if (secAdmin != null) { secAdmin.setVisible(true); secAdmin.setManaged(true); }
            if (secAcademic != null) { secAcademic.setVisible(true); secAcademic.setManaged(true); }
            if (hasSession && !isAdmin) {
                setBtnVisible(btnUsers, SessionManager.hasPermission("MANAGE_USERS"));
                setBtnVisible(btnTeachers, SessionManager.hasPermission("MANAGE_TEACHERS"));
                setBtnVisible(btnStudents, SessionManager.hasPermission("MANAGE_STUDENTS"));
                setBtnVisible(btnSchedules, SessionManager.hasPermission("MANAGE_SCHEDULES"));
                setBtnVisible(btnCertTypes, SessionManager.hasPermission("MANAGE_COURSES"));
                setBtnVisible(btnCourses, SessionManager.hasPermission("MANAGE_COURSES"));
                setBtnVisible(btnSkills, SessionManager.hasPermission("MANAGE_COURSES"));
                setBtnVisible(btnClasses, SessionManager.hasPermission("MANAGE_CLASSES"));
                setBtnVisible(btnPeriodicTests, SessionManager.hasPermission("MANAGE_CLASSES"));
                setBtnVisible(btnExams, SessionManager.hasPermission("MANAGE_EXAMS"));
                setBtnVisible(btnExamRegs, SessionManager.hasPermission("MANAGE_EXAMS"));
                setBtnVisible(btnExamResults, SessionManager.hasPermission("MANAGE_EXAMS"));
                setBtnVisible(btnCertificates, SessionManager.hasPermission("ISSUE_CERTIFICATES"));
                setBtnVisible(btnReports, SessionManager.hasPermission("VIEW_REPORTS"));
            }
            if (isStaff) {
                openCourses();
            } else {
                openUsers();
            }
        }
    }

    private void setBtnVisible(Button btn, boolean visible) {
        if (btn != null) {
            btn.setVisible(visible);
            btn.setManaged(visible);
        }
    }

    private void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            pnlContentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
            Label err = new Label("Could not load view: " + fxmlPath + "\nError: " + e.getMessage());
            err.setStyle("-fx-text-fill: red; -fx-padding: 20; -fx-font-size: 14px;");
            pnlContentArea.getChildren().setAll(err);
        }
    }

    // Use Case 02: Manage Users and Permissions (Admin)
    @FXML public void openUsers() { loadView("/view/UserView.fxml"); }

    // Use Case 03: Manage Teachers (Admin, Staff)
    @FXML public void openTeachers() { loadView("/view/TeacherView.fxml"); }

    // Use Case 04: Manage Students (Admin, Staff)
    @FXML public void openStudents() { loadView("/view/StudentView.fxml"); }

    // Use Case 05: Manage Teaching Schedules (Admin, Staff)
    @FXML public void openTeachingSchedules() { loadView("/view/TeachingScheduleView.fxml"); }

    // Use Case 06: Manage Certificate Categories / Types (Admin, Staff)
    @FXML public void openCertificateTypes() { loadView("/view/CertificateTypeView.fxml"); }

    // Use Case 07: Manage Courses (Admin, Staff)
    @FXML public void openCourses() { loadView("/view/CourseManagementView.fxml"); }

    // Use Case 08: Manage Skill Types (Admin, Staff)
    @FXML public void openSkills() { loadView("/view/SkillManagementView.fxml"); }

    // Use Case 09: Manage Classes (Admin, Staff)
    @FXML public void openClasses() { loadView("/view/ClassView.fxml"); }

    // Use Case 10: Check Periodic Evaluations (Admin, Staff)
    @FXML public void openPeriodicTests() { loadView("/view/PeriodicTestView.fxml"); }

    // Use Case 12: Manage Exam (Staff)
    @FXML public void openExams() { loadView("/view/ExamManagementView.fxml"); }

    // Use Case 11 & 15: Manage Exam Registrations (Staff) / Register for Exam (Student)
    @FXML public void openExamRegistrations() { loadView("/view/ExamRegistrationView.fxml"); }

    // Use Case 13 & 16: Manage Exam Marks (Staff) / View Exam Marks (Student)
    @FXML public void openExamResults() { loadView("/view/ExamResultView.fxml"); }

    // Use Case 14 & 17: Check Eligibility & Issue Certificate (Staff) / View Certificate (Student)
    @FXML public void openCertificates() { loadView("/view/CertificateView.fxml"); }

    // Use Case 18: Monthly/Course Eligibility Reports and Lookup (All roles)
    @FXML public void openReports() { loadView("/view/EligibilityLookupView.fxml"); }

    @FXML
    private void handleLogout() {
        try {
            SessionManager.clearSession();
            Parent root = FXMLLoader.load(getClass().getResource("/view/LoginView.fxml"));
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.setTitle("CFL - Certificate Generation System");
            stage.setScene(new Scene(root, 440, 400));
            stage.centerOnScreen();
            stage.setResizable(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

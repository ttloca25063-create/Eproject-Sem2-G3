package dao;

import model.Student;
import util.DBConnection;
import util.PasswordUtil;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    // --- Student lookup functions ---
    public Integer findStudentIDByCode(String studentCode) {
        String sql = "SELECT StudentID FROM Student WHERE StudentCode = ? AND Status = 'Active'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentCode);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("StudentID");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public String getFullNameByStudentCode(String studentCode) {
        String sql = "SELECT u.FullName FROM Student s JOIN User u ON s.UserID = u.UserID WHERE s.StudentCode = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentCode);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("FullName");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // --- Student management and enrollment functions ---
    public List<Student> getAllStudentsWithEnrollment() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT s.StudentID, s.UserID, s.StudentCode, u.FullName, u.Email, u.Phone, u.DateOfBirth, " +
                     "s.EnrollmentDate, s.Status, c.ClassName, ce.TuitionFee, ce.Discount, ce.PaymentStatus, " +
                     "COALESCE(SUM(p.Amount), 0) AS PaidAmount " +
                     "FROM Student s " +
                     "JOIN User u ON s.UserID = u.UserID " +
                     "LEFT JOIN ClassEnrollment ce ON s.StudentID = ce.StudentID " +
                     "LEFT JOIN Class c ON ce.ClassID = c.ClassID " +
                     "LEFT JOIN Payment p ON ce.EnrollmentID = p.EnrollmentID " +
                     "GROUP BY s.StudentID, ce.EnrollmentID ORDER BY s.StudentID DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Student st = new Student();
                st.setStudentID(rs.getInt("StudentID"));
                st.setUserID(rs.getInt("UserID"));
                st.setStudentCode(rs.getString("StudentCode"));
                st.setFullName(rs.getString("FullName"));
                st.setEmail(rs.getString("Email"));
                st.setPhone(rs.getString("Phone"));
                Date dob = rs.getDate("DateOfBirth");
                if (dob != null) st.setDateOfBirth(dob.toLocalDate());
                Date enr = rs.getDate("EnrollmentDate");
                if (enr != null) st.setEnrollmentDate(enr.toLocalDate());
                st.setClassName(rs.getString("ClassName") != null ? rs.getString("ClassName") : "Not Enrolled");
                st.setTuitionFee(rs.getBigDecimal("TuitionFee") != null ? rs.getBigDecimal("TuitionFee") : BigDecimal.ZERO);
                st.setDiscount(rs.getBigDecimal("Discount") != null ? rs.getBigDecimal("Discount") : BigDecimal.ZERO);
                st.setPaidAmount(rs.getBigDecimal("PaidAmount"));
                st.setPaymentStatus(rs.getString("PaymentStatus") != null ? rs.getString("PaymentStatus") : "Unpaid");
                st.setStatus(rs.getString("Status"));
                list.add(st);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean enrollStudentAndRecordPayment(Student student, String username, String password, int classID,
                                                 BigDecimal tuitionFee, BigDecimal discount, BigDecimal initialPayment, String paymentMethod) {
        String checkCapSql = "SELECT c.MaxStudents, COUNT(ce.EnrollmentID) AS CurrentCount " +
                             "FROM Class c LEFT JOIN ClassEnrollment ce ON c.ClassID = ce.ClassID " +
                             "WHERE c.ClassID = ? GROUP BY c.ClassID";
        String userSql = "INSERT INTO User (RoleID, Username, PasswordHash, FullName, Email, Phone, DateOfBirth, Status, CreatedAt) " +
                         "VALUES ((SELECT RoleID FROM Role WHERE RoleName = 'Student' LIMIT 1), ?, ?, ?, ?, ?, ?, 'Active', NOW())";
        String studentSql = "INSERT INTO Student (UserID, StudentCode, EnrollmentDate, Status) VALUES (?, ?, CURDATE(), 'Active')";
        String enrollSql = "INSERT INTO ClassEnrollment (StudentID, ClassID, EnrollmentDate, TuitionFee, Discount, PaymentStatus, Status) " +
                           "VALUES (?, ?, CURDATE(), ?, ?, ?, 'Enrolled')";
        String paymentSql = "INSERT INTO Payment (EnrollmentID, Amount, PaymentDate, Method, Note) VALUES (?, ?, CURDATE(), ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement capPs = conn.prepareStatement(checkCapSql)) {
                capPs.setInt(1, classID);
                try (ResultSet rsCap = capPs.executeQuery()) {
                    if (rsCap.next() && rsCap.getInt("CurrentCount") >= rsCap.getInt("MaxStudents")) {
                        return false;
                    }
                }
            }

            BigDecimal netFee = tuitionFee.subtract(discount);
            if (initialPayment.compareTo(netFee) > 0) return false;
            String paymentStatus;
            if (netFee.compareTo(BigDecimal.ZERO) <= 0 || initialPayment.compareTo(netFee) == 0) {
                paymentStatus = "Paid";
            } else if (initialPayment.compareTo(BigDecimal.ZERO) > 0) {
                paymentStatus = "Partially Paid";
            } else {
                paymentStatus = "Unpaid";
            }

            conn.setAutoCommit(false);

            int newUserID = 0;
            try (PreparedStatement psUser = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                psUser.setString(1, username);
                psUser.setString(2, password);
                psUser.setString(3, student.getFullName());
                psUser.setString(4, PasswordUtil.emptyToNull(student.getEmail()));
                psUser.setString(5, PasswordUtil.emptyToNull(student.getPhone()));
                psUser.setDate(6, student.getDateOfBirth() != null ? Date.valueOf(student.getDateOfBirth()) : null);
                psUser.executeUpdate();
                try (ResultSet rsU = psUser.getGeneratedKeys()) {
                    if (rsU.next()) newUserID = rsU.getInt(1);
                }
            }

            int newStudentID = 0;
            try (PreparedStatement psSt = conn.prepareStatement(studentSql, Statement.RETURN_GENERATED_KEYS)) {
                psSt.setInt(1, newUserID);
                psSt.setString(2, student.getStudentCode());
                psSt.executeUpdate();
                try (ResultSet rsS = psSt.getGeneratedKeys()) {
                    if (rsS.next()) newStudentID = rsS.getInt(1);
                }
            }

            int newEnrollmentID = 0;
            try (PreparedStatement psEnr = conn.prepareStatement(enrollSql, Statement.RETURN_GENERATED_KEYS)) {
                psEnr.setInt(1, newStudentID);
                psEnr.setInt(2, classID);
                psEnr.setBigDecimal(3, tuitionFee);
                psEnr.setBigDecimal(4, discount);
                psEnr.setString(5, paymentStatus);
                psEnr.executeUpdate();
                try (ResultSet rsE = psEnr.getGeneratedKeys()) {
                    if (rsE.next()) newEnrollmentID = rsE.getInt(1);
                }
            }

            if (initialPayment.compareTo(BigDecimal.ZERO) > 0) {
                try (PreparedStatement psPay = conn.prepareStatement(paymentSql)) {
                    psPay.setInt(1, newEnrollmentID);
                    psPay.setBigDecimal(2, initialPayment);
                    psPay.setString(3, paymentMethod);
                    psPay.setString(4, "Initial tuition payment");
                    psPay.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }

    public String recordAdditionalPayment(int studentID, BigDecimal amount, String paymentMethod) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return "Payment amount must be greater than 0.";
        }
        String findSql = "SELECT ce.EnrollmentID, ce.TuitionFee, ce.Discount, COALESCE(SUM(p.Amount), 0) AS PaidAmount " +
                         "FROM ClassEnrollment ce " +
                         "LEFT JOIN Payment p ON ce.EnrollmentID = p.EnrollmentID " +
                         "WHERE ce.StudentID = ? " +
                         "GROUP BY ce.EnrollmentID " +
                         "ORDER BY ce.EnrollmentID DESC LIMIT 1";
        String insertPaySql = "INSERT INTO Payment (EnrollmentID, Amount, PaymentDate, Method, Note) VALUES (?, ?, CURDATE(), ?, 'Additional tuition payment')";
        String updateEnrollSql = "UPDATE ClassEnrollment SET PaymentStatus = ? WHERE EnrollmentID = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            int enrollmentID = 0;
            BigDecimal netFee = BigDecimal.ZERO;
            BigDecimal paidSoFar = BigDecimal.ZERO;

            try (PreparedStatement ps = conn.prepareStatement(findSql)) {
                ps.setInt(1, studentID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return "No class enrollment found for this student.";
                    }
                    enrollmentID = rs.getInt("EnrollmentID");
                    BigDecimal tuition = rs.getBigDecimal("TuitionFee") != null ? rs.getBigDecimal("TuitionFee") : BigDecimal.ZERO;
                    BigDecimal discount = rs.getBigDecimal("Discount") != null ? rs.getBigDecimal("Discount") : BigDecimal.ZERO;
                    paidSoFar = rs.getBigDecimal("PaidAmount") != null ? rs.getBigDecimal("PaidAmount") : BigDecimal.ZERO;
                    netFee = tuition.subtract(discount);
                }
            }

            BigDecimal newTotalPaid = paidSoFar.add(amount);
            if (newTotalPaid.compareTo(netFee) > 0) {
                return "Payment exceeds remaining balance (" + netFee.subtract(paidSoFar) + ").";
            }

            String newStatus = newTotalPaid.compareTo(netFee) >= 0 ? "Paid" : "Partially Paid";

            conn.setAutoCommit(false);
            try (PreparedStatement psPay = conn.prepareStatement(insertPaySql)) {
                psPay.setInt(1, enrollmentID);
                psPay.setBigDecimal(2, amount);
                psPay.setString(3, paymentMethod != null ? paymentMethod : "Cash");
                psPay.executeUpdate();
            }
            try (PreparedStatement psUpd = conn.prepareStatement(updateEnrollSql)) {
                psUpd.setString(1, newStatus);
                psUpd.setInt(2, enrollmentID);
                psUpd.executeUpdate();
            }
            conn.commit();
            return "SUCCESS";
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            e.printStackTrace();
            return "Database error: " + e.getMessage();
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }
}

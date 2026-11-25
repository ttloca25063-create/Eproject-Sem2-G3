package dao;

import model.ExamRegistration;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamRegistrationDAO {

    public int insert(ExamRegistration r) {
        String sql = "INSERT INTO ExamRegistration " +
                "(ExamID, StudentID, CandidateType, FullName, DOB, IDNumber, Phone, Email, " +
                " IsFeeExempt, ExamFee, PaidStatus, RegistrationDate, Status) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getExamID());
            if (r.getStudentID() != null) ps.setInt(2, r.getStudentID());
            else ps.setNull(2, Types.INTEGER);
            ps.setString(3, r.getCandidateType());
            ps.setString(4, r.getFullName());
            ps.setDate(5, r.getDob() != null ? Date.valueOf(r.getDob()) : null);
            ps.setString(6, r.getIdNumber());
            ps.setString(7, r.getPhone());
            ps.setString(8, r.getEmail());
            ps.setBoolean(9, r.isFeeExempt());
            ps.setDouble(10, r.getExamFee());
            ps.setString(11, r.getPaidStatus());
            ps.setDate(12, Date.valueOf(r.getRegistrationDate()));
            ps.setString(13, r.getStatus());
            if (ps.executeUpdate() > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return -1;
    }

    public boolean checkDuplicate(int examID, Integer studentID, String idNumber) {
        String trimmedId = idNumber != null ? idNumber.trim() : "";
        boolean hasIdNumber = !trimmedId.isEmpty();

        if (studentID == null && !hasIdNumber) {
            return false;
        }

        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ExamRegistration WHERE ExamID = ? AND Status != 'Cancelled' AND (");
        if (studentID != null && hasIdNumber) {
            sql.append("StudentID = ? OR (IDNumber IS NOT NULL AND IDNumber <> '' AND IDNumber = ?)");
        } else if (studentID != null) {
            sql.append("StudentID = ?");
        } else {
            sql.append("IDNumber IS NOT NULL AND IDNumber <> '' AND IDNumber = ?");
        }
        sql.append(")");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setInt(idx++, examID);
            if (studentID != null) ps.setInt(idx++, studentID);
            if (hasIdNumber) ps.setString(idx, trimmedId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return false;
    }

    public List<ExamRegistration> findByExamID(int examID) {
        List<ExamRegistration> list = new ArrayList<>();
        String sql = "SELECT * FROM ExamRegistration WHERE ExamID = ? ORDER BY RegistrationDate DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException ex) { ex.printStackTrace(); }
        return list;
    }

    public ExamRegistration findById(int id) {
        String sql = "SELECT * FROM ExamRegistration WHERE RegistrationID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException ex) { ex.printStackTrace(); }
        return null;
    }

    public boolean updatePaymentStatus(int regID, String status) {
        String sql = "UPDATE ExamRegistration SET PaidStatus = ? WHERE RegistrationID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, regID);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) { ex.printStackTrace(); }
        return false;
    }

    private ExamRegistration map(ResultSet rs) throws SQLException {
        ExamRegistration r = new ExamRegistration();
        r.setRegistrationID(rs.getInt("RegistrationID"));
        r.setExamID(rs.getInt("ExamID"));
        int sid = rs.getInt("StudentID");
        r.setStudentID(rs.wasNull() ? null : sid);
        r.setCandidateType(rs.getString("CandidateType"));
        r.setFullName(rs.getString("FullName"));
        Date dob = rs.getDate("DOB");
        r.setDob(dob != null ? dob.toLocalDate() : null);
        r.setIdNumber(rs.getString("IDNumber"));
        r.setPhone(rs.getString("Phone"));
        r.setEmail(rs.getString("Email"));
        r.setFeeExempt(rs.getBoolean("IsFeeExempt"));
        r.setExamFee(rs.getDouble("ExamFee"));
        r.setPaidStatus(rs.getString("PaidStatus"));
        r.setRegistrationDate(rs.getDate("RegistrationDate").toLocalDate());
        r.setStatus(rs.getString("Status"));
        return r;
    }
}
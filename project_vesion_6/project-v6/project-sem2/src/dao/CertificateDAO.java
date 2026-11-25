package dao;

import model.Certificate;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CertificateDAO {
    private Connection conn;

    public CertificateDAO(Connection conn) {
        this.conn = conn;
    }

    public List<Certificate> getAllCertificates() throws SQLException {
        List<Certificate> list = new ArrayList<>();
        String sql = "SELECT * FROM Certificate ORDER BY IssueDate DESC";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Certificate(
                    rs.getInt("CertificateID"),
                    rs.getString("CertificateNumber"),
                    rs.getInt("CertificateTypeID"),
                    (Integer) rs.getObject("StudentID"),
                    (Integer) rs.getObject("ExamResultID"),
                    rs.getDate("IssueDate") != null ? rs.getDate("IssueDate").toLocalDate() : null,
                    rs.getDate("ExpiryDate") != null ? rs.getDate("ExpiryDate").toLocalDate() : null,
                    rs.getString("Status"),
                    (Integer) rs.getObject("IssuedBy")
                ));
            }
        }
        return list;
    }

    public ResultSet getEligibilityInfo(int examResultID) throws SQLException {
        String sql = "SELECT er.Result, reg.StudentID, reg.PaidStatus AS ExamPaid, reg.IsFeeExempt, " +
                     "e.CertificateTypeID, ct.Code AS CertCode, " +
                     "(SELECT COUNT(*) FROM Certificate c WHERE c.ExamResultID = er.ResultID) AS AlreadyIssued " +
                     "FROM ExamResult er " +
                     "JOIN ExamRegistration reg ON er.RegistrationID = reg.RegistrationID " +
                     "JOIN Exam e ON reg.ExamID = e.ExamID " +
                     "JOIN CertificateType ct ON e.CertificateTypeID = ct.CertificateTypeID " +
                     "WHERE er.ResultID = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, examResultID);
        return ps.executeQuery();
    }

    public boolean hasUnpaidTuition(int studentID) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ClassEnrollment WHERE StudentID = ? AND PaymentStatus != 'Paid'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentID);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public boolean insertCertificate(Certificate cert) throws SQLException {
        String sql = "INSERT INTO Certificate (CertificateNumber, CertificateTypeID, StudentID, ExamResultID, IssueDate, ExpiryDate, Status, IssuedBy) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cert.getCertificateNumber());
            ps.setInt(2, cert.getCertificateTypeID());
            if (cert.getStudentID() != null) ps.setInt(3, cert.getStudentID()); else ps.setNull(3, Types.INTEGER);
            if (cert.getExamResultID() != null) ps.setInt(4, cert.getExamResultID()); else ps.setNull(4, Types.INTEGER);
            ps.setDate(5, Date.valueOf(cert.getIssueDate()));
            ps.setDate(6, cert.getExpiryDate() != null ? Date.valueOf(cert.getExpiryDate()) : null);
            ps.setString(7, cert.getStatus());
            if (cert.getIssuedBy() != null) ps.setInt(8, cert.getIssuedBy()); else ps.setNull(8, Types.INTEGER);

            return ps.executeUpdate() > 0;
        }
    }

    public List<String[]> getIssuedCertificatesDisplay() throws SQLException {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT c.CertificateID, c.CertificateNumber, ct.Code AS CertType, " +
                     "COALESCE(reg.FullName, u.FullName, 'Candidate') AS CandidateName, " +
                     "COALESCE(er.AverageScore, 0) AS Score, c.IssueDate, c.Status " +
                     "FROM Certificate c " +
                     "JOIN CertificateType ct ON c.CertificateTypeID = ct.CertificateTypeID " +
                     "LEFT JOIN ExamResult er ON c.ExamResultID = er.ResultID " +
                     "LEFT JOIN ExamRegistration reg ON er.RegistrationID = reg.RegistrationID " +
                     "LEFT JOIN Student s ON c.StudentID = s.StudentID " +
                     "LEFT JOIN User u ON s.UserID = u.UserID " +
                     "ORDER BY c.CertificateID DESC";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("CertificateID")),
                    rs.getString("CertificateNumber"),
                    rs.getString("CertType"),
                    rs.getString("CandidateName"),
                    rs.getBigDecimal("Score").toString(),
                    rs.getDate("IssueDate") != null ? rs.getDate("IssueDate").toString() : "",
                    rs.getString("Status")
                });
            }
        }
        return list;
    }

    public List<String[]> getPendingPassedResults() throws SQLException {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT er.ResultID, reg.FullName, e.ExamName, er.AverageScore, reg.PaidStatus " +
                     "FROM ExamResult er " +
                     "JOIN ExamRegistration reg ON er.RegistrationID = reg.RegistrationID " +
                     "JOIN Exam e ON reg.ExamID = e.ExamID " +
                     "LEFT JOIN Certificate c ON c.ExamResultID = er.ResultID " +
                     "WHERE er.Result = 'Pass' AND c.CertificateID IS NULL " +
                     "ORDER BY er.ResultID DESC";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("ResultID")),
                    rs.getString("FullName"),
                    rs.getString("ExamName"),
                    rs.getBigDecimal("AverageScore").toString(),
                    rs.getString("PaidStatus")
                });
            }
        }
        return list;
    }

    public boolean revokeCertificate(int certificateID) throws SQLException {
        String sql = "UPDATE Certificate SET Status = 'Revoked' WHERE CertificateID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, certificateID);
            return ps.executeUpdate() > 0;
        }
    }
}
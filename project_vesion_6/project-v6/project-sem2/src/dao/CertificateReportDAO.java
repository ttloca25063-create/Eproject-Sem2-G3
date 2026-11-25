package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CertificateReportDAO {
    private final Connection conn;

    public CertificateReportDAO(Connection conn) {
        this.conn = conn;
    }

    public ResultSet lookupEligibility(String keyword) throws SQLException {
        String sql = "SELECT reg.fullName, reg.idNumber, s.studentCode, e.examName, er.averageScore, er.result, reg.paidStatus, c.certificateNumber, c.status AS certStatus "
                   + "FROM ExamRegistration reg "
                   + "LEFT JOIN Student s ON reg.studentID = s.studentID "
                   + "JOIN Exam e ON reg.examID = e.examID "
                   + "LEFT JOIN ExamResult er ON reg.registrationID = er.registrationID "
                   + "LEFT JOIN Certificate c ON er.resultID = c.examResultID "
                   + "WHERE s.studentCode = ? OR c.certificateNumber = ? OR reg.idNumber = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, keyword);
        ps.setString(2, keyword);
        ps.setString(3, keyword);
        return ps.executeQuery();
    }
}
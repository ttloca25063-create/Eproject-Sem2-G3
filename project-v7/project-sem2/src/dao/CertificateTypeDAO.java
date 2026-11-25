package dao;

import model.CertificateType;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CertificateTypeDAO {
    private Connection conn;

    public CertificateTypeDAO(Connection conn) {
        this.conn = conn;
    }

    public List<CertificateType> getAll() throws SQLException {
        List<CertificateType> list = new ArrayList<>();
        String sql = "SELECT * FROM CertificateType";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new CertificateType(
                    rs.getInt("CertificateTypeID"),
                    rs.getString("Code"),
                    rs.getString("Name"),
                    rs.getString("Level"),
                    rs.getString("Description"),
                    rs.getDouble("PassScore")
                ));
            }
        }
        return list;
    }

    public boolean isCodeExists(String code) throws SQLException {
        String sql = "SELECT COUNT(*) FROM CertificateType WHERE Code = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean insert(CertificateType type) throws SQLException {
        String sql = "INSERT INTO CertificateType (Code, Name, Level, Description, PassScore) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type.getCode());
            ps.setString(2, type.getName());
            ps.setString(3, type.getLevel());
            ps.setString(4, type.getDescription());
            ps.setDouble(5, type.getPassScore());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(CertificateType type) throws SQLException {
        String sql = "UPDATE CertificateType SET Name = ?, Level = ?, Description = ?, PassScore = ? WHERE CertificateTypeID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type.getName());
            ps.setString(2, type.getLevel());
            ps.setString(3, type.getDescription());
            ps.setDouble(4, type.getPassScore());
            ps.setInt(5, type.getCertificateTypeID());
            return ps.executeUpdate() > 0;
        }
    }
    public String delete(int certificateTypeID) throws SQLException {
        // 1. Kiểm tra xem có khóa học nào thuộc loại chứng chỉ này không
        String checkCourseSql = "SELECT COUNT(*) FROM Course WHERE CertificateTypeID = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkCourseSql)) {
            ps.setInt(1, certificateTypeID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return "Không thể xóa: Loại chứng chỉ này đang được gắn với các Khóa học (Course).";
                }
            }
        }

        // 2. Kiểm tra xem có kỳ thi nào thuộc loại chứng chỉ này không
        String checkExamSql = "SELECT COUNT(*) FROM Exam WHERE CertificateTypeID = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkExamSql)) {
            ps.setInt(1, certificateTypeID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return "Không thể xóa: Loại chứng chỉ này đang được sử dụng trong các Kỳ thi (Exam).";
                }
            }
        }

        // 3. Kiểm tra xem đã có chứng chỉ nào được cấp theo loại này chưa
        String checkCertSql = "SELECT COUNT(*) FROM Certificate WHERE CertificateTypeID = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkCertSql)) {
            ps.setInt(1, certificateTypeID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return "Không thể xóa: Đã có chứng chỉ được cấp thuộc loại chứng chỉ này.";
                }
            }
        }

        // 4. Đủ điều kiện -> Tiến hành xóa
        String deleteSql = "DELETE FROM CertificateType WHERE CertificateTypeID = ?";
        try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
            ps.setInt(1, certificateTypeID);
            return ps.executeUpdate() > 0 ? "SUCCESS" : "Xóa loại chứng chỉ thất bại.";
        }
    }
}
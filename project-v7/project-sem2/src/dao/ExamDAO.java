package dao;

import model.Exam;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamDAO {

    public List<Exam> getAllOpenExams() {
        List<Exam> list = new ArrayList<>();
        String sql = "SELECT e.*, ct.Name AS CertName " +
                "FROM Exam e LEFT JOIN CertificateType ct ON e.CertificateTypeID = ct.CertificateTypeID " +
                "WHERE e.Status IN ('Upcoming', 'Open') AND (e.RegistrationDeadline IS NULL OR e.RegistrationDeadline >= CURDATE())";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Exam e = new Exam();
                e.setExamID(rs.getInt("ExamID"));
                e.setExamCode(rs.getString("ExamCode"));
                e.setExamName(rs.getString("ExamName"));
                e.setCertificateTypeID(rs.getInt("CertificateTypeID"));
                e.setCertificateTypeName(rs.getString("CertName"));
                e.setExamDate(rs.getDate("ExamDate") != null ? rs.getDate("ExamDate").toLocalDate() : null);
                Date dl = rs.getDate("RegistrationDeadline");
                e.setRegistrationDeadline(dl != null ? dl.toLocalDate() : null);
                e.setLocation(rs.getString("Location"));
                e.setExamFee(rs.getDouble("ExamFee"));
                e.setStatus(rs.getString("Status"));
                list.add(e);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public Exam getById(int examID) {
        String sql = "SELECT * FROM Exam WHERE ExamID = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Exam e = new Exam();
                e.setExamID(rs.getInt("ExamID"));
                e.setExamName(rs.getString("ExamName"));
                e.setExamDate(rs.getDate("ExamDate").toLocalDate());
                Date dl = rs.getDate("RegistrationDeadline");
                e.setRegistrationDeadline(dl != null ? dl.toLocalDate() : null);
                e.setExamFee(rs.getDouble("ExamFee"));
                return e;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public java.util.List<Exam> findAll() {
        java.util.List<Exam> list = new java.util.ArrayList<>();
        String sql = "SELECT e.*, ct.Name AS CertName FROM Exam e " +
                "LEFT JOIN CertificateType ct ON e.CertificateTypeID = ct.CertificateTypeID " +
                "ORDER BY e.ExamDate DESC";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Exam e = new Exam();
                e.setExamID(rs.getInt("ExamID"));
                e.setExamCode(rs.getString("ExamCode"));
                e.setExamName(rs.getString("ExamName"));
                e.setCertificateTypeID(rs.getInt("CertificateTypeID"));
                e.setCertificateTypeName(rs.getString("CertName"));
                java.sql.Date d = rs.getDate("ExamDate");
                e.setExamDate(d != null ? d.toLocalDate() : null);
                java.sql.Date dl = rs.getDate("RegistrationDeadline");
                e.setRegistrationDeadline(dl != null ? dl.toLocalDate() : null);
                e.setLocation(rs.getString("Location"));
                e.setExamFee(rs.getDouble("ExamFee"));
                e.setStatus(rs.getString("Status"));
                list.add(e);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public int insert(Exam e) {
        String sql = "INSERT INTO Exam (ExamCode, ExamName, CertificateTypeID, ExamDate, " +
                "RegistrationDeadline, Location, ExamFee, Status) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getExamCode());
            ps.setString(2, e.getExamName());
            ps.setInt(3, e.getCertificateTypeID());
            ps.setDate(4, java.sql.Date.valueOf(e.getExamDate()));
            ps.setDate(5,
                    e.getRegistrationDeadline() != null ? java.sql.Date.valueOf(e.getRegistrationDeadline()) : null);
            ps.setString(6, e.getLocation());
            ps.setDouble(7, e.getExamFee());
            ps.setString(8, e.getStatus());
            if (ps.executeUpdate() > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    public boolean update(Exam e) {
        String sql = "UPDATE Exam SET ExamCode=?, ExamName=?, CertificateTypeID=?, ExamDate=?, " +
                "RegistrationDeadline=?, Location=?, ExamFee=?, Status=? WHERE ExamID=?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getExamCode());
            ps.setString(2, e.getExamName());
            ps.setInt(3, e.getCertificateTypeID());
            ps.setDate(4, java.sql.Date.valueOf(e.getExamDate()));
            ps.setDate(5,
                    e.getRegistrationDeadline() != null ? java.sql.Date.valueOf(e.getRegistrationDeadline()) : null);
            ps.setString(6, e.getLocation());
            ps.setDouble(7, e.getExamFee());
            ps.setString(8, e.getStatus());
            ps.setInt(9, e.getExamID());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean delete(int examID) {
        String sql = "DELETE FROM Exam WHERE ExamID = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examID);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
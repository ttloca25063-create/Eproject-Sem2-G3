package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SkillTypeDAO {
    private final Connection conn;

    public SkillTypeDAO(Connection conn) {
        this.conn = conn;
    }

    public boolean insertSkill(String code, String name, BigDecimal minPassScore) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM SkillType WHERE code = ? OR name = ?";
        try (PreparedStatement check = conn.prepareStatement(checkSql)) {
            check.setString(1, code);
            check.setString(2, name);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return false;
                }
            }
        }
        String sql = "INSERT INTO SkillType (code, name, minPassScore) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setString(2, name);
            ps.setBigDecimal(3, minPassScore);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateSkill(int skillTypeID, String name, BigDecimal minPassScore) throws SQLException {
        String sql = "UPDATE SkillType SET name = ?, minPassScore = ? WHERE skillTypeID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setBigDecimal(2, minPassScore);
            ps.setInt(3, skillTypeID);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteSkill(int skillTypeID) throws SQLException {
        String checkUsage = "SELECT COUNT(*) FROM TeacherSkill WHERE skillTypeID = ?";
        try (PreparedStatement check = conn.prepareStatement(checkUsage)) {
            check.setInt(1, skillTypeID);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Skill is currently linked to teachers or exams.");
                }
            }
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM SkillType WHERE skillTypeID = ?")) {
            ps.setInt(1, skillTypeID);
            return ps.executeUpdate() > 0;
        }
    }
}
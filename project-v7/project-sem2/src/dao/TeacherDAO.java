package dao;

import model.Teacher;
import util.DBConnection;
import util.PasswordUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TeacherDAO {

    public List<Teacher> getAllTeachers() {
        List<Teacher> list = new ArrayList<>();
        String sql = "SELECT t.TeacherID, t.UserID, t.TeacherCode, u.FullName, u.Email, u.Phone, " +
                     "t.Degree, t.Experience, t.HireDate, t.Status, " +
                     "GROUP_CONCAT(st.Name SEPARATOR ', ') AS Skills " +
                     "FROM Teacher t " +
                     "JOIN User u ON t.UserID = u.UserID " +
                     "LEFT JOIN TeacherSkill ts ON t.TeacherID = ts.TeacherID " +
                     "LEFT JOIN SkillType st ON ts.SkillTypeID = st.SkillTypeID " +
                     "GROUP BY t.TeacherID ORDER BY t.TeacherID DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Date hireDate = rs.getDate("HireDate");
                list.add(new Teacher(
                    rs.getInt("TeacherID"),
                    rs.getInt("UserID"),
                    rs.getString("TeacherCode"),
                    rs.getString("FullName"),
                    rs.getString("Email"),
                    rs.getString("Phone"),
                    rs.getString("Degree"),
                    rs.getString("Experience"),
                    hireDate != null ? hireDate.toLocalDate() : null,
                    rs.getString("Skills") != null ? rs.getString("Skills") : "None",
                    rs.getString("Status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean createTeacherWithSkills(Teacher teacher, String username, String password, List<Integer> skillTypeIDs, String proficiencyLevel) {
        if (skillTypeIDs == null || skillTypeIDs.isEmpty()) return false;

        String userSql = "INSERT INTO User (RoleID, Username, PasswordHash, FullName, Email, Phone, Status, CreatedAt) " +
                         "VALUES ((SELECT RoleID FROM Role WHERE RoleName = 'Staff' LIMIT 1), ?, ?, ?, ?, ?, 'Active', NOW())";
        String teacherSql = "INSERT INTO Teacher (UserID, TeacherCode, Degree, Experience, HireDate, Status) VALUES (?, ?, ?, ?, ?, ?)";
        String skillSql = "INSERT INTO TeacherSkill (TeacherID, SkillTypeID, ProficiencyLevel) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int newUserID = 0;
            try (PreparedStatement psUser = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                psUser.setString(1, username);
                psUser.setString(2, password);
                psUser.setString(3, teacher.getFullName());
                psUser.setString(4, PasswordUtil.emptyToNull(teacher.getEmail()));
                psUser.setString(5, PasswordUtil.emptyToNull(teacher.getPhone()));
                psUser.executeUpdate();
                try (ResultSet rsUser = psUser.getGeneratedKeys()) {
                    if (rsUser.next()) newUserID = rsUser.getInt(1);
                }
            }

            int newTeacherID = 0;
            try (PreparedStatement psTeacher = conn.prepareStatement(teacherSql, Statement.RETURN_GENERATED_KEYS)) {
                psTeacher.setInt(1, newUserID);
                psTeacher.setString(2, teacher.getTeacherCode());
                psTeacher.setString(3, teacher.getDegree());
                psTeacher.setString(4, teacher.getExperience());
                psTeacher.setDate(5, teacher.getHireDate() != null ? Date.valueOf(teacher.getHireDate()) : new Date(System.currentTimeMillis()));
                psTeacher.setString(6, teacher.getStatus());
                psTeacher.executeUpdate();
                try (ResultSet rsTeacher = psTeacher.getGeneratedKeys()) {
                    if (rsTeacher.next()) newTeacherID = rsTeacher.getInt(1);
                }
            }

            try (PreparedStatement psSkill = conn.prepareStatement(skillSql)) {
                for (Integer skillID : skillTypeIDs) {
                    psSkill.setInt(1, newTeacherID);
                    psSkill.setInt(2, skillID);
                    psSkill.setString(3, proficiencyLevel);
                    psSkill.addBatch();
                }
                psSkill.executeBatch();
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

    public Set<Integer> getTeacherSkillIDs(int teacherID) {
        Set<Integer> ids = new HashSet<>();
        String sql = "SELECT SkillTypeID FROM TeacherSkill WHERE TeacherID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teacherID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("SkillTypeID"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }

    public boolean updateTeacherWithSkills(Teacher teacher, List<Integer> skillTypeIDs, String proficiencyLevel) {
        if (skillTypeIDs == null || skillTypeIDs.isEmpty()) return false;
        String userSql = "UPDATE User SET FullName = ?, Email = ?, Phone = ?, UpdatedAt = NOW() WHERE UserID = ?";
        String teacherSql = "UPDATE Teacher SET Degree = ?, Experience = ?, Status = ? WHERE TeacherID = ?";
        String delSkillsSql = "DELETE FROM TeacherSkill WHERE TeacherID = ?";
        String insSkillSql = "INSERT INTO TeacherSkill (TeacherID, SkillTypeID, ProficiencyLevel) VALUES (?, ?, ?)";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psU = conn.prepareStatement(userSql)) {
                psU.setString(1, teacher.getFullName());
                psU.setString(2, PasswordUtil.emptyToNull(teacher.getEmail()));
                psU.setString(3, PasswordUtil.emptyToNull(teacher.getPhone()));
                psU.setInt(4, teacher.getUserID());
                psU.executeUpdate();
            }

            try (PreparedStatement psT = conn.prepareStatement(teacherSql)) {
                psT.setString(1, teacher.getDegree());
                psT.setString(2, teacher.getExperience());
                psT.setString(3, teacher.getStatus());
                psT.setInt(4, teacher.getTeacherID());
                psT.executeUpdate();
            }

            try (PreparedStatement psD = conn.prepareStatement(delSkillsSql)) {
                psD.setInt(1, teacher.getTeacherID());
                psD.executeUpdate();
            }

            try (PreparedStatement psS = conn.prepareStatement(insSkillSql)) {
                for (Integer skillID : skillTypeIDs) {
                    psS.setInt(1, teacher.getTeacherID());
                    psS.setInt(2, skillID);
                    psS.setString(3, proficiencyLevel);
                    psS.addBatch();
                }
                psS.executeBatch();
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

    public boolean deleteTeacher(int teacherID) {
        String checkSql = "SELECT COUNT(*) FROM Class WHERE TeacherID = ? AND Status != 'Closed'";
        String deleteSql = "UPDATE Teacher SET Status = 'Inactive' WHERE TeacherID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setInt(1, teacherID);
            ResultSet rs = checkPs.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return false;
            }
            try (PreparedStatement delPs = conn.prepareStatement(deleteSql)) {
                delPs.setInt(1, teacherID);
                return delPs.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

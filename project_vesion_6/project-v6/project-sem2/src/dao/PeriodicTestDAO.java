package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PeriodicTestDAO {
    private final Connection conn;

    public PeriodicTestDAO(Connection conn) {
        this.conn = conn;
    }

    public boolean createPeriodicTest(int classID, int skillTypeID, String testName, Date testDate, BigDecimal maxScore) throws SQLException {
        String sql = "INSERT INTO PeriodicTest (classID, skillTypeID, testName, testDate, maxScore) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classID);
            ps.setInt(2, skillTypeID);
            ps.setString(3, testName);
            ps.setDate(4, testDate);
            ps.setBigDecimal(5, maxScore);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean saveStudentTestScore(int testID, int studentID, BigDecimal score, String note) throws SQLException {
        String sql = "INSERT INTO TestScore (TestID, StudentID, Score, Note) VALUES (?, ?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE Score = VALUES(Score), Note = VALUES(Note)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, testID);
            ps.setInt(2, studentID);
            ps.setBigDecimal(3, score);
            ps.setString(4, note);
            return ps.executeUpdate() > 0;
        }
    }

    public List<String[]> getScoresByTest(int testID) throws SQLException {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT s.StudentID, s.StudentCode, u.FullName, ts.Score, COALESCE(ts.Note, '') AS Note "
                   + "FROM TestScore ts "
                   + "JOIN Student s ON ts.StudentID = s.StudentID "
                   + "LEFT JOIN User u ON s.UserID = u.UserID "
                   + "WHERE ts.TestID = ? ORDER BY s.StudentCode";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, testID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[]{
                        String.valueOf(rs.getInt("StudentID")),
                        rs.getString("StudentCode"),
                        rs.getString("FullName") != null ? rs.getString("FullName") : "",
                        rs.getBigDecimal("Score").toString(),
                        rs.getString("Note")
                    });
                }
            }
        }
        return list;
    }

    public List<String[]> getCourseGradebook(Integer courseID) throws SQLException {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT co.CourseName, c.ClassName, s.StudentCode, COALESCE(u.FullName, '') AS FullName, "
                   + "COUNT(ts.TestID) AS TestsTaken, "
                   + "COALESCE(GROUP_CONCAT(CONCAT(pt.TestName, ': ', ts.Score) ORDER BY pt.TestDate SEPARATOR ' | '), 'No scores') AS ScoreDetails, "
                   + "COALESCE(ROUND(AVG(ts.Score), 2), 0.00) AS AvgScore "
                   + "FROM ClassEnrollment ce "
                   + "JOIN Class c ON ce.ClassID = c.ClassID "
                   + "JOIN Course co ON c.CourseID = co.CourseID "
                   + "JOIN Student s ON ce.StudentID = s.StudentID "
                   + "LEFT JOIN User u ON s.UserID = u.UserID "
                   + "LEFT JOIN PeriodicTest pt ON pt.ClassID = c.ClassID "
                   + "LEFT JOIN TestScore ts ON ts.TestID = pt.TestID AND ts.StudentID = s.StudentID "
                   + (courseID != null && courseID > 0 ? "WHERE co.CourseID = ? " : "")
                   + "GROUP BY co.CourseID, c.ClassID, s.StudentID "
                   + "ORDER BY co.CourseName, c.ClassName, s.StudentCode";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (courseID != null && courseID > 0) {
                ps.setInt(1, courseID);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[]{
                        rs.getString("CourseName"),
                        rs.getString("ClassName"),
                        rs.getString("StudentCode"),
                        rs.getString("FullName"),
                        String.valueOf(rs.getInt("TestsTaken")),
                        rs.getString("ScoreDetails"),
                        rs.getBigDecimal("AvgScore").toString()
                    });
                }
            }
        }
        return list;
    }
}

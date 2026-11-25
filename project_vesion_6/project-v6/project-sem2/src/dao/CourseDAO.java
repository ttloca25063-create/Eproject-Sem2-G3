package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CourseDAO {
    private final Connection conn;

    public CourseDAO(Connection conn) {
        this.conn = conn;
    }

    public boolean insertCourse(String courseCode, int certificateTypeID, String courseName, int duration, BigDecimal tuitionFee, Date startDate, Date endDate) throws SQLException {
        String sql = "INSERT INTO Course (courseCode, certificateTypeID, courseName, duration, tuitionFee, startDate, endDate, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, 'Active')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, courseCode);
            ps.setInt(2, certificateTypeID);
            ps.setString(3, courseName);
            ps.setInt(4, duration);
            ps.setBigDecimal(5, tuitionFee);
            ps.setDate(6, startDate);
            ps.setDate(7, endDate);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean insertCourse(int certificateTypeID, String courseName, int duration, BigDecimal tuitionFee, Date startDate, Date endDate) throws SQLException {
        String generatedCode = "C-" + System.currentTimeMillis();
        return insertCourse(generatedCode, certificateTypeID, courseName, duration, tuitionFee, startDate, endDate);
    }

    public boolean updateCourse(int courseID, String courseCode, int certificateTypeID, String courseName, int duration, BigDecimal tuitionFee, Date startDate, Date endDate) throws SQLException {
        String sql = "UPDATE Course SET courseCode = ?, certificateTypeID = ?, courseName = ?, duration = ?, tuitionFee = ?, startDate = ?, endDate = ? WHERE courseID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, courseCode);
            ps.setInt(2, certificateTypeID);
            ps.setString(3, courseName);
            ps.setInt(4, duration);
            ps.setBigDecimal(5, tuitionFee);
            ps.setDate(6, startDate);
            ps.setDate(7, endDate);
            ps.setInt(8, courseID);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean archiveCourse(int courseID) throws SQLException {
        String checkClass = "SELECT COUNT(*) FROM Class WHERE courseID = ? AND status = 'Open'";
        try (PreparedStatement check = conn.prepareStatement(checkClass)) {
            check.setInt(1, courseID);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Cannot archive course with active open classes.");
                }
            }
        }
        try (PreparedStatement ps = conn.prepareStatement("UPDATE Course SET status = 'Archived' WHERE courseID = ?")) {
            ps.setInt(1, courseID);
            return ps.executeUpdate() > 0;
        }
    }
}
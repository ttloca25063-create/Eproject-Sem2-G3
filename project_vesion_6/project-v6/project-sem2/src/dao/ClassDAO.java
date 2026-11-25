package dao;

import model.ClassRoom;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClassDAO {
    private Connection conn;

    public ClassDAO(Connection conn) {
        this.conn = conn;
    }

    // 1. Create a new class
    public boolean createClass(ClassRoom cls) throws SQLException {
        String sql = "INSERT INTO Class (ClassCode, ClassName, CourseID, TeacherID, StartDate, EndDate, MaxStudents, Room, Schedule, Status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cls.getClassCode());
            ps.setString(2, cls.getClassName());
            ps.setInt(3, cls.getCourseID());
            if (cls.getTeacherID() != null) ps.setInt(4, cls.getTeacherID()); else ps.setNull(4, Types.INTEGER);
            ps.setDate(5, cls.getStartDate() != null ? Date.valueOf(cls.getStartDate()) : null);
            ps.setDate(6, cls.getEndDate() != null ? Date.valueOf(cls.getEndDate()) : null);
            ps.setInt(7, cls.getMaxStudents());
            ps.setString(8, cls.getRoom());
            ps.setString(9, cls.getSchedule());
            ps.setString(10, cls.getStatus());
            return ps.executeUpdate() > 0;
        }
    }

    // 2. Update a class
    public boolean updateClass(ClassRoom cls) throws SQLException {
        String sql = "UPDATE Class SET ClassName = ?, CourseID = ?, TeacherID = ?, StartDate = ?, EndDate = ?, " +
                     "MaxStudents = ?, Room = ?, Schedule = ?, Status = ? WHERE ClassID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cls.getClassName());
            ps.setInt(2, cls.getCourseID());
            if (cls.getTeacherID() != null) ps.setInt(3, cls.getTeacherID()); else ps.setNull(3, Types.INTEGER);
            ps.setDate(4, cls.getStartDate() != null ? Date.valueOf(cls.getStartDate()) : null);
            ps.setDate(5, cls.getEndDate() != null ? Date.valueOf(cls.getEndDate()) : null);
            ps.setInt(6, cls.getMaxStudents());
            ps.setString(7, cls.getRoom());
            ps.setString(8, cls.getSchedule());
            ps.setString(9, cls.getStatus());
            ps.setInt(10, cls.getClassID());
            return ps.executeUpdate() > 0;
        }
    }

    // 3. Search classes
    public List<ClassRoom> searchClasses(String keyword, String status) throws SQLException {
        List<ClassRoom> list = new ArrayList<>();
        String sql = "SELECT * FROM Class WHERE (ClassCode LIKE ? OR ClassName LIKE ?)";
        if (status != null && !status.equals("All")) {
            sql += " AND Status = ?";
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            if (status != null && !status.equals("All")) {
                ps.setString(3, status);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ClassRoom(
                    rs.getInt("ClassID"),
                    rs.getString("ClassCode"),
                    rs.getString("ClassName"),
                    rs.getInt("CourseID"),
                    (Integer) rs.getObject("TeacherID"),
                    rs.getDate("StartDate") != null ? rs.getDate("StartDate").toLocalDate() : null,
                    rs.getDate("EndDate") != null ? rs.getDate("EndDate").toLocalDate() : null,
                    rs.getInt("MaxStudents"),
                    rs.getString("Room"),
                    rs.getString("Schedule"),
                    rs.getString("Status")
                ));
            }
        }
        return list;
    }

    // 4. Get student list for a class
    public List<String> getStudentsInClass(int classID) throws SQLException {
        List<String> students = new ArrayList<>();
        String sql = "SELECT s.StudentCode, u.FullName, ce.PaymentStatus, ce.Status " +
                     "FROM ClassEnrollment ce " +
                     "JOIN Student s ON ce.StudentID = s.StudentID " +
                     "JOIN User u ON s.UserID = u.UserID " +
                     "WHERE ce.ClassID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                students.add(rs.getString("StudentCode") + " - " + rs.getString("FullName") +
                             " [" + rs.getString("PaymentStatus") + "]");
            }
        }
        return students;
        
        
    }
    
    public String deleteClass(int classID) throws SQLException {
        // 1. Kiểm tra trạng thái lớp
        String checkStatusSql = "SELECT Status FROM Class WHERE ClassID = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkStatusSql)) {
            ps.setInt(1, classID);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return "Class does not exist.";
                }
                String status = rs.getString("Status");
                if ("Ongoing".equalsIgnoreCase(status) || "Completed".equalsIgnoreCase(status)) {
                    return "Cannot delete a class that is Ongoing or Completed.";
                }
            }
        }

        // 2. Kiểm tra học viên đã ghi danh trong lớp
        String checkEnrollSql = "SELECT COUNT(*) FROM ClassEnrollment WHERE ClassID = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkEnrollSql)) {
            ps.setInt(1, classID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return "Cannot delete class: There are students enrolled in this class.";
                }
            }
        }

        // 3. Kiểm tra lịch giảng dạy đã xếp cho lớp
        String checkScheduleSql = "SELECT COUNT(*) FROM TeachingSchedule WHERE ClassID = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkScheduleSql)) {
            ps.setInt(1, classID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return "Cannot delete class: Teaching schedules exist for this class.";
                }
            }
        }

        // 4. Kiểm tra bài kiểm tra định kỳ của lớp
        String checkTestSql = "SELECT COUNT(*) FROM PeriodicTest WHERE ClassID = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkTestSql)) {
            ps.setInt(1, classID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return "Cannot delete class: Periodic tests exist for this class.";
                }
            }
        }

        // 5. Đủ điều kiện -> Tiến hành xóa
        String deleteSql = "DELETE FROM Class WHERE ClassID = ?";
        try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
            ps.setInt(1, classID);
            return ps.executeUpdate() > 0 ? "SUCCESS" : "Failed to delete class.";
        }
    }
}

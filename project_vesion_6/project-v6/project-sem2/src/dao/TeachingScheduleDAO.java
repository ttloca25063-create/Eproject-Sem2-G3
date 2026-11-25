package dao;

import model.TeachingSchedule;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeachingScheduleDAO {

    public List<TeachingSchedule> getAllSchedules() {
        List<TeachingSchedule> list = new ArrayList<>();
        String sql = "SELECT ts.*, c.ClassName, u.FullName AS TeacherName, st.Name AS SkillName " +
                     "FROM TeachingSchedule ts " +
                     "JOIN Class c ON ts.ClassID = c.ClassID " +
                     "JOIN Teacher t ON ts.TeacherID = t.TeacherID " +
                     "JOIN User u ON t.UserID = u.UserID " +
                     "JOIN SkillType st ON ts.SkillTypeID = st.SkillTypeID " +
                     "ORDER BY ts.TeachingDate DESC, ts.StartTime ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                TeachingSchedule s = new TeachingSchedule();
                s.setScheduleID(rs.getInt("ScheduleID"));
                s.setClassID(rs.getInt("ClassID"));
                s.setClassName(rs.getString("ClassName"));
                s.setTeacherID(rs.getInt("TeacherID"));
                s.setTeacherName(rs.getString("TeacherName"));
                s.setSkillTypeID(rs.getInt("SkillTypeID"));
                s.setSkillName(rs.getString("SkillName"));
                s.setTeachingDate(rs.getDate("TeachingDate").toLocalDate());
                s.setStartTime(rs.getTime("StartTime").toLocalTime());
                s.setEndTime(rs.getTime("EndTime").toLocalTime());
                s.setRoom(rs.getString("Room"));
                s.setStatus(rs.getString("Status"));
                list.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public String assignTeachingSchedule(TeachingSchedule schedule) {
        if (!schedule.getEndTime().isAfter(schedule.getStartTime())) {
            return "End time must be after start time.";
        }

        String checkSkillSql = "SELECT COUNT(*) FROM TeacherSkill WHERE TeacherID = ? AND SkillTypeID = ?";
        String checkTeacherConflictSql = "SELECT COUNT(*) FROM TeachingSchedule WHERE TeacherID = ? AND TeachingDate = ? " +
                                         "AND Status != 'Cancelled' AND ScheduleID != ? " +
                                         "AND (StartTime < ? AND EndTime > ?)";
        String checkRoomConflictSql = "SELECT COUNT(*) FROM TeachingSchedule WHERE Room = ? AND TeachingDate = ? " +
                                      "AND Status != 'Cancelled' AND ScheduleID != ? " +
                                      "AND (StartTime < ? AND EndTime > ?)";
        String insertSql = "INSERT INTO TeachingSchedule (ClassID, TeacherID, SkillTypeID, TeachingDate, StartTime, EndTime, Room, Status) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, 'Scheduled')";

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement psSkill = conn.prepareStatement(checkSkillSql)) {
                psSkill.setInt(1, schedule.getTeacherID());
                psSkill.setInt(2, schedule.getSkillTypeID());
                ResultSet rsSkill = psSkill.executeQuery();
                if (rsSkill.next() && rsSkill.getInt(1) == 0) {
                    return "Teacher is not qualified for the selected skill type!";
                }
            }

            try (PreparedStatement psT = conn.prepareStatement(checkTeacherConflictSql)) {
                psT.setInt(1, schedule.getTeacherID());
                psT.setDate(2, Date.valueOf(schedule.getTeachingDate()));
                psT.setInt(3, schedule.getScheduleID());
                psT.setTime(4, Time.valueOf(schedule.getEndTime()));
                psT.setTime(5, Time.valueOf(schedule.getStartTime()));
                ResultSet rsT = psT.executeQuery();
                if (rsT.next() && rsT.getInt(1) > 0) {
                    return "Schedule Conflict: Teacher already has another class at this time!";
                }
            }

            try (PreparedStatement psR = conn.prepareStatement(checkRoomConflictSql)) {
                psR.setString(1, schedule.getRoom());
                psR.setDate(2, Date.valueOf(schedule.getTeachingDate()));
                psR.setInt(3, schedule.getScheduleID());
                psR.setTime(4, Time.valueOf(schedule.getEndTime()));
                psR.setTime(5, Time.valueOf(schedule.getStartTime()));
                ResultSet rsR = psR.executeQuery();
                if (rsR.next() && rsR.getInt(1) > 0) {
                    return "Room Conflict: Room " + schedule.getRoom() + " is already occupied!";
                }
            }

            try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                psIns.setInt(1, schedule.getClassID());
                psIns.setInt(2, schedule.getTeacherID());
                psIns.setInt(3, schedule.getSkillTypeID());
                psIns.setDate(4, Date.valueOf(schedule.getTeachingDate()));
                psIns.setTime(5, Time.valueOf(schedule.getStartTime()));
                psIns.setTime(6, Time.valueOf(schedule.getEndTime()));
                psIns.setString(7, schedule.getRoom());
                return psIns.executeUpdate() > 0 ? "SUCCESS" : "Database insert failed.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error: " + e.getMessage();
        }
    }

    public boolean deleteSchedule(int scheduleID) {
        String sql = "DELETE FROM TeachingSchedule WHERE ScheduleID = ? AND Status != 'Completed'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, scheduleID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

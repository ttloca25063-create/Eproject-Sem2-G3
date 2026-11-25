package dao;

import model.Permission;
import model.User;
import util.DBConnection;
import util.PasswordUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserDAO {

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT u.*, COALESCE(r.RoleName, 'User') AS RoleName FROM User u LEFT JOIN Role r ON u.RoleID = r.RoleID ORDER BY u.UserID DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User u = new User();
                u.setUserID(rs.getInt("UserID"));
                u.setRoleID(rs.getInt("RoleID"));
                u.setRoleName(rs.getString("RoleName"));
                u.setUsername(rs.getString("Username"));
                u.setFullName(rs.getString("FullName"));
                u.setEmail(rs.getString("Email"));
                u.setPhone(rs.getString("Phone"));
                Date dob = rs.getDate("DateOfBirth");
                if (dob != null) u.setDateOfBirth(dob.toLocalDate());
                u.setGender(rs.getString("Gender"));
                u.setAddress(rs.getString("Address"));
                u.setStatus(rs.getString("Status"));
                list.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean isUsernameExists(String username, int excludeUserID) {
        String sql = "SELECT COUNT(*) FROM User WHERE Username = ? AND UserID != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, excludeUserID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean createUser(User user) {
        String sql = "INSERT INTO User (RoleID, Username, PasswordHash, FullName, Email, Phone, DateOfBirth, Gender, Address, Status, CreatedAt) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, user.getRoleID());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getFullName());
            ps.setString(5, PasswordUtil.emptyToNull(user.getEmail()));
            ps.setString(6, PasswordUtil.emptyToNull(user.getPhone()));
            ps.setDate(7, user.getDateOfBirth() != null ? Date.valueOf(user.getDateOfBirth()) : null);
            ps.setString(8, PasswordUtil.emptyToNull(user.getGender()));
            ps.setString(9, PasswordUtil.emptyToNull(user.getAddress()));
            ps.setString(10, user.getStatus());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUserAndRole(User user) {
        String sql = "UPDATE User SET RoleID = ?, FullName = ?, Email = ?, Phone = ?, DateOfBirth = ?, Gender = ?, Address = ?, Status = ?, UpdatedAt = NOW() WHERE UserID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, user.getRoleID());
            ps.setString(2, user.getFullName());
            ps.setString(3, PasswordUtil.emptyToNull(user.getEmail()));
            ps.setString(4, PasswordUtil.emptyToNull(user.getPhone()));
            ps.setDate(5, user.getDateOfBirth() != null ? Date.valueOf(user.getDateOfBirth()) : null);
            ps.setString(6, PasswordUtil.emptyToNull(user.getGender()));
            ps.setString(7, PasswordUtil.emptyToNull(user.getAddress()));
            ps.setString(8, user.getStatus());
            ps.setInt(9, user.getUserID());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deactivateOrDeleteUser(int targetUserID, int currentLoggedInUserID) {
        if (targetUserID == currentLoggedInUserID) return false;
        String sql = "UPDATE User SET Status = 'Inactive', UpdatedAt = NOW() WHERE UserID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, targetUserID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Permission> getAllPermissions() {
        List<Permission> list = new ArrayList<>();
        String sql = "SELECT PermissionID, PermissionName, Module FROM Permission ORDER BY PermissionID";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Permission(
                    rs.getInt("PermissionID"),
                    rs.getString("PermissionName"),
                    rs.getString("Module")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Set<Integer> getPermissionIDsByRole(int roleID) {
        Set<Integer> ids = new HashSet<>();
        String sql = "SELECT PermissionID FROM RolePermission WHERE RoleID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("PermissionID"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }

    public boolean updateRolePermissions(int roleID, List<Integer> permissionIDs) {
        String deleteSql = "DELETE FROM RolePermission WHERE RoleID = ?";
        String insertSql = "INSERT INTO RolePermission (RoleID, PermissionID) VALUES (?, ?)";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement delPs = conn.prepareStatement(deleteSql)) {
                delPs.setInt(1, roleID);
                delPs.executeUpdate();
            }
            try (PreparedStatement insPs = conn.prepareStatement(insertSql)) {
                for (Integer permID : permissionIDs) {
                    insPs.setInt(1, roleID);
                    insPs.setInt(2, permID);
                    insPs.addBatch();
                }
                insPs.executeBatch();
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
}

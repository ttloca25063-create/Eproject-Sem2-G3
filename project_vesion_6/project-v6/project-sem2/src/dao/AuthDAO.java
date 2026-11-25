package dao;

import model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuthDAO {
    private final Connection conn;

    public AuthDAO(Connection conn) {
        this.conn = conn;
    }

    public User login(String username, String passwordHash) throws SQLException {
        String sql = "SELECT * FROM `User` WHERE username = ? AND passwordHash = ? AND status = 'Active'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserID(rs.getInt("userID"));
                    user.setRoleID(rs.getInt("roleID"));
                    user.setUsername(rs.getString("username"));
                    user.setFullName(rs.getString("fullName"));
                    user.setEmail(rs.getString("email"));
                    user.setStatus(rs.getString("status"));
                    return user;
                }
            }
        }
        return null;
    }

    public List<String> getPermissionsByRole(int roleID) throws SQLException {
        List<String> permissions = new ArrayList<>();
        String sql = "SELECT p.permissionName FROM Permission p "
                   + "JOIN RolePermission rp ON p.permissionID = rp.permissionID "
                   + "WHERE rp.roleID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    permissions.add(rs.getString("permissionName"));
                }
            }
        }
        return permissions;
    }
}
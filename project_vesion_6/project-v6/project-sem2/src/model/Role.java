package model;

import java.util.ArrayList;
import java.util.List;

public class Role {
    private int roleID;
    private String roleName;
    private String description;
    private List<Permission> permissions = new ArrayList<>();

    public Role() {}

    public Role(int roleID, String roleName, String description) {
        this.roleID = roleID;
        this.roleName = roleName;
        this.description = description;
    }

    public int getRoleID() { return roleID; }
    public void setRoleID(int roleID) { this.roleID = roleID; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // Methods as per Class Diagram
    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }
}

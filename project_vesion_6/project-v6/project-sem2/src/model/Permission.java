package model;

public class Permission {
    private int permissionID;
    private String permissionName;
    private String module;

    public Permission() {}

    public Permission(int permissionID, String permissionName, String module) {
        this.permissionID = permissionID;
        this.permissionName = permissionName;
        this.module = module;
    }

    public int getPermissionID() { return permissionID; }
    public void setPermissionID(int permissionID) { this.permissionID = permissionID; }
    public String getPermissionName() { return permissionName; }
    public void setPermissionName(String permissionName) { this.permissionName = permissionName; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
}

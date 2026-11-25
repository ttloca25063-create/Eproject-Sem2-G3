package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class User {
    private int userID;
    private int roleID;
    private String roleName;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() {}

    public User(int userID, int roleID, String roleName, String username, String passwordHash,
                String fullName, String email, String phone, LocalDate dateOfBirth, String gender,
                String address, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.userID = userID;
        this.roleID = roleID;
        this.roleName = roleName;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.address = address;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getUserID() { return userID; }
    public void setUserID(int userID) { this.userID = userID; }
    public int getRoleID() { return roleID; }
    public void setRoleID(int roleID) { this.roleID = roleID; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Methods as per Class Diagram
    public boolean login() {
        return "Active".equalsIgnoreCase(this.status);
    }

    public boolean logout() {
        return true;
    }

    public boolean changePassword(String newPasswordHash) {
        if (newPasswordHash != null && !newPasswordHash.isEmpty()) {
            this.passwordHash = newPasswordHash;
            this.updatedAt = LocalDateTime.now();
            return true;
        }
        return false;
    }
}
